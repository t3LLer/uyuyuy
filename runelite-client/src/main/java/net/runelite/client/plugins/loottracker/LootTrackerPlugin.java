/*
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.loottracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.*;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.localstorage.LTItemEntry;
import net.runelite.client.plugins.loottracker.localstorage.LTRecord;
import net.runelite.client.plugins.loottracker.localstorage.LootRecordWriter;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.StackFormatter;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.loot.GameItem;
import net.runelite.http.api.loot.LootRecord;
import net.runelite.http.api.loot.LootRecordType;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Loot Tracker",
        description = "Tracks loot from monsters and minigames",
        tags = {"drops"},
        enabledByDefault = false
)
@Slf4j
@Singleton
public class LootTrackerPlugin extends Plugin {
    // Activity/Event loot handling
    private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed ([0-9]+) ([a-z]+) Treasure Trails.");
    private static final int THEATRE_OF_BLOOD_REGION = 12867;

    private static final Pattern BOSS_NAME_NUMBER_PATTERN = Pattern.compile("Your (.*) kill count is: ([0-9]*).");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");

    // Herbiboar loot handling
    private static final String HERBIBOAR_LOOTED_MESSAGE = "You harvest herbs from the herbiboar, whereupon it escapes.";
    private static final String HERBIBOAR_EVENT = "Herbiboar";

    // Wintertodt loot handling
    private static final Pattern WINTERTODT_NUMBER_PATTERN = Pattern.compile("Your subdued Wintertodt count is: ([0-9]*).");
    private static final String WINTERTODT_EVENT = "Wintertodt";

    // Hespori loot handling
    private static final String HESPORI_LOOTED_MESSAGE = "You have successfully cleared this patch for new crops.";
    private static final String HESPORI_EVENT = "Hespori";
    private static final int HESPORI_REGION = 5021;

    // Gauntlet loot handling
    private static final String GAUNTLET_LOOTED_MESSAGE = "You open the chest.";
    private static final String GAUNTLET_EVENT = "The Gauntlet";
    private static final int GAUNTLET_LOBBY_REGION = 12127;

    // Chest loot handling
    private static final String CHEST_LOOTED_MESSAGE = "You find some treasure in the chest!";
    private static final Pattern LARRAN_LOOTED_PATTERN = Pattern.compile("You have opened Larran's (big|small) chest .*");
    private static final Map<Integer, String> CHEST_EVENT_TYPES = ImmutableMap.of(
            5179, "Brimstone Chest",
            11573, "Crystal Chest",
            12093, "Larran's big chest",
            13113, "Larran's small chest",
            13151, "Elven Crystal Chest"
    );
    private static final File LOOT_RECORDS_FILE = new File(RuneLite.RUNELITE_DIR, "lootRecords.json");
    private static final Set<Integer> RESPAWN_REGIONS = ImmutableSet.of(
            12850, // Lumbridge
            11828, // Falador
            12342, // Edgeville
            11062 // Camelot
    );

    private static final Set<String> PET_MESSAGES = ImmutableSet.of("You have a funny feeling like you're being followed",
            "You feel something weird sneaking into your backpack",
            "You have a funny feeling like you would have been followed");

    // Instant for showing session loot. this gets set on plugin startup

    public static final Instant SESSION_START_TIME = Instant.now();

    @Inject
    public Client client;
    @VisibleForTesting
    private Collection<LootRecord> lootRecords = new ArrayList<>();
    private boolean pvpDeath = false;

    // Last man standing map regions
    private static final Set<Integer> LAST_MAN_STANDING_REGIONS = ImmutableSet.of(13658, 13659, 13914, 13915, 13916);

    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private ItemManager itemManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private SpriteManager spriteManager;
    @Inject
    private LootTrackerConfig config;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ScheduledExecutorService executor;
    @Inject
    private EventBus eventBus;
    @Inject
    private LootRecordWriter writer;
    private LootTrackerPanel panel;
    private NavigationButton navButton;
    private String eventType;
    private boolean chestLooted;

    private List<String> ignoredItems = new ArrayList<>();
    private List<String> ignoredNPCs = new ArrayList<>();
    private Multiset<Integer> inventorySnapshot;

    private Map<String, Integer> killCountMap = new HashMap<>();
    private boolean gotPet = false;

    private static Collection<ItemStack> stack(Collection<ItemStack> items) {
        final List<ItemStack> list = new ArrayList<>();

        for (final ItemStack item : items) {
            int quantity = 0;
            for (final ItemStack i : list) {
                if (i.getId() == item.getId()) {
                    quantity = i.getQuantity();
                    list.remove(i);
                    break;
                }
            }
            if (quantity > 0) {
                list.add(new ItemStack(item.getId(), item.getQuantity() + quantity, item.getLocation()));
            } else {
                list.add(item);
            }
        }

        return list;
    }

    private static Collection<GameItem> toGameItems(Collection<ItemStack> items) {
        return items.stream()
                .map(item -> new GameItem(item.getId(), item.getQuantity()))
                .collect(Collectors.toList());
    }

    private String getIgnoredItems;
    private boolean saveLoot;
    private boolean chestLootChat;
    private LootRecordSortType sortType;
    private boolean whitelistEnabled;
    private String getWhitelist;
    private boolean blacklistEnabled;
    private String getBlacklist;
    private boolean sendLootValueMessages;

    @Provides
    LootTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LootTrackerConfig.class);
    }

    private void onLocalPlayerDeath(LocalPlayerDeath event) {
        if (client.getVar(Varbits.IN_WILDERNESS) == 1 || WorldType.isPvpWorld(client.getWorldType())) {
            deathInventorySnapshot();
            pvpDeath = true;
        }
    }

    private void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("loottracker")) {
            updateConfig();

            if (event.getKey().equals("ignoredItems")) {
                ignoredItems = Text.fromCSV(this.getIgnoredItems);
                SwingUtilities.invokeLater(panel::updateIgnoredRecords);
            }
            if (event.getKey().equals("ignoredNPCs")) {
                ignoredNPCs = Text.fromCSV(config.getIgnoredNPCs());
                SwingUtilities.invokeLater(panel::updateIgnoredRecords);
            }
            if (event.getKey().equals("sortType")) {
                panel.setLootRecordSortType(this.sortType);
                SwingUtilities.invokeLater(panel::rebuild);
            }
        }

    }

    @Override
    protected void startUp() throws Exception {

        addSubscriptions();

        ignoredItems = Text.fromCSV(config.getIgnoredItems());
        ignoredNPCs = Text.fromCSV(config.getIgnoredNPCs());
        updateConfig();
        panel = new LootTrackerPanel(this, itemManager, config);
        spriteManager.getSpriteAsync(SpriteID.TAB_INVENTORY, 0, panel::loadHeaderIcon);

        final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Loot Tracker")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        LOOT_RECORDS_FILE.createNewFile();
        // BufferedReader bufferedReader = Files.newBufferedReader(LOOT_RECORDS_FILE.toPath());


        clientThread.invokeLater(() ->
        {
            switch (client.getGameState()) {
                case STARTING:
                case UNKNOWN:
                    return false;
            }

            executor.submit(() ->
            {
                try {
                    lootRecords.addAll(RuneLiteAPI.GSON.fromJson(new FileReader(LOOT_RECORDS_FILE),
                            new TypeToken<ArrayList<LootRecord>>() {
                            }.getType()));
                } catch (IOException | NullPointerException e) {
                    log.info("Couldn't load any locally stored loots.");
                }
                if (lootRecords.size() > 0) {
                    log.info("Loaded {} locally stored loot records", lootRecords.size());
                }

                Collection<LootRecord> finalLootRecords = lootRecords;
                clientThread.invokeLater(() ->
                {
                    Collection<LootTrackerRecord> records = convertToLootTrackerRecord(finalLootRecords);
                    SwingUtilities.invokeLater(() -> panel.addRecords(records));
                });
            });
            return true;
        });

    }

    @Override
    protected void shutDown() {
        eventBus.unregister(this);
        clientToolbar.removeNavigation(navButton);
        lootRecords = new ArrayList<>();
        chestLooted = false;
    }

    private void addSubscriptions() {
        eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
        eventBus.subscribe(LocalPlayerDeath.class, this, this::onLocalPlayerDeath);
        eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
        eventBus.subscribe(NpcLootReceived.class, this, this::onNpcLootReceived);
        eventBus.subscribe(PlayerSpawned.class, this, this::onPlayerSpawned);
        eventBus.subscribe(PlayerLootReceived.class, this, this::onPlayerLootReceived);
        eventBus.subscribe(WidgetLoaded.class, this, this::onWidgetLoaded);
        eventBus.subscribe(ChatMessage.class, this, this::onChatMessage);
        eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
        eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
    }

    private void onGameStateChanged(final GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            chestLooted = false;
        }

        if (event.getGameState() == GameState.LOGGING_IN) {
            clientThread.invokeLater(() ->
            {
                switch (client.getGameState()) {
                    case LOGGED_IN:
                        break;
                    case LOGGING_IN:
                    case LOADING:
                        return false;
                    default:
                        // Quit running if any other state
                        return true;
                }

                String name = client.getLocalPlayer().getName();
                if (name != null) {
                    writer.setPlayerUsername(name);
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    private void onNpcLootReceived(final NpcLootReceived npcLootReceived) {
        final NPC npc = npcLootReceived.getNpc();
        final Collection<ItemStack> items = npcLootReceived.getItems();
        final String name = npc.getName();
        final int combat = npc.getCombatLevel();
        final LootTrackerItem[] entries = buildEntries(stack(items));
        String localUsername = client.getLocalPlayer().getName();

        final int killCount = killCountMap.getOrDefault(name.toUpperCase(), -1);

        if (this.whitelistEnabled) {
            final String configNpcs = this.getWhitelist.toLowerCase();
            List<String> whitelist = Text.fromCSV(configNpcs);
            if (!whitelist.contains(name.toLowerCase())) {
                return;
            }
        } else if (this.blacklistEnabled) {
            final String configNpcs = this.getBlacklist.toLowerCase();
            List<String> blacklist = Text.fromCSV(configNpcs);
            if (blacklist.contains(name.toLowerCase())) {
                return;
            }
        }

        if (gotPet) {
            ItemStack pet = handlePet(name);
            if (pet == null) {
                log.warn("Error finding pet for npc name: {}", name);
            } else {
                items.add(pet);
            }
        }

        SwingUtilities.invokeLater(() -> panel.add(name, localUsername, combat, entries));
        LootRecord lootRecord = new LootRecord(name, localUsername, LootRecordType.NPC,
                toGameItems(items), Instant.now());

        saveLocalLootRecord(lootRecord);


        LTRecord record = new LTRecord(npc.getId(), npc.getName(), combat, killCount, convertToLTItemEntries(items));
        writer.addLootTrackerRecord(record);
    }

    private void onPlayerSpawned(PlayerSpawned event) {
        if (event.getPlayer().equals(client.getLocalPlayer())) {
            SwingUtilities.invokeLater(() -> panel.rebuild());
        }
    }

    private void onPlayerLootReceived(final PlayerLootReceived playerLootReceived) {
        // Ignore Last Man Standing player loots
        if (isAtLMS()) {
            return;
        }
        if (this.sendLootValueMessages) {
            if (WorldType.isDeadmanWorld(client.getWorldType()) || WorldType.isHighRiskWorld(client.getWorldType()) ||
                    WorldType.isPvpWorld(client.getWorldType()) || client.getVar(Varbits.IN_WILDERNESS) == 1) {
                final String totalValue = StackFormatter.quantityToRSStackSize(playerLootReceived.getItems().stream()
                        .mapToInt(itemStack -> itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity()).sum());

                chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(
                        new ChatMessageBuilder().append("The total value of your loot is " + totalValue + " GP.")
                                .build()).build());
            }
        }
        final Player player = playerLootReceived.getPlayer();
        final Collection<ItemStack> items = playerLootReceived.getItems();
        final String name = player.getName();
        final int combat = player.getCombatLevel();
        final LootTrackerItem[] entries = buildEntries(stack(items));
        String localUsername = client.getLocalPlayer().getName();
        SwingUtilities.invokeLater(() -> panel.add(name, localUsername, combat, entries));
        LootRecord lootRecord = new LootRecord(name, localUsername, LootRecordType.PLAYER,
                toGameItems(items), Instant.now());

            saveLocalLootRecord(lootRecord);

        LTRecord record = new LTRecord(-1, name, combat, -1, convertToLTItemEntries(items));
        writer.addLootTrackerRecord(record);
    }

    private void onWidgetLoaded(WidgetLoaded event) {
        final ItemContainer container;
        switch (event.getGroupId()) {
            case (WidgetID.BARROWS_REWARD_GROUP_ID):
                eventType = "Barrows";
                container = client.getItemContainer(InventoryID.BARROWS_REWARD);
                break;
            case (WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID):
                if (chestLooted) {
                    return;
                }
                eventType = "Chambers of Xeric";
                container = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);
                chestLooted = true;
                break;
            case (WidgetID.THEATRE_OF_BLOOD_REWARD_GROUP_ID):
                if (chestLooted) {
                    return;
                }

                if (WorldPoint.fromLocalInstance(client, client.getLocalPlayer()
                        .getLocalLocation()).getRegionID() != THEATRE_OF_BLOOD_REGION) {
                    return;
                }

                eventType = "Theatre of Blood";
                container = client.getItemContainer(InventoryID.THEATRE_OF_BLOOD_CHEST);
                chestLooted = true;
                break;
            case (WidgetID.CLUE_SCROLL_REWARD_GROUP_ID):
                // event type should be set via ChatMessage for clue scrolls.
                // Clue Scrolls use same InventoryID as Barrows
                container = client.getItemContainer(InventoryID.BARROWS_REWARD);
                break;
            case (WidgetID.KINGDOM_GROUP_ID):
                eventType = "Kingdom of Miscellania";
                container = client.getItemContainer(InventoryID.KINGDOM_OF_MISCELLANIA);
                break;
            default:
                return;
        }

        if (container == null) {
            return;
        }

        if (!(event.getGroupId() == WidgetID.CLUE_SCROLL_REWARD_GROUP_ID) && this.chestLootChat) {
            Item[] items = container.getItems();
            long chestPrice = 0;
            for (Item item : items) {
                long itemStack = (long) itemManager.getItemPrice(item.getId()) * (long) item.getQuantity();
                chestPrice += itemStack;
            }

            final ChatMessageBuilder message = new ChatMessageBuilder()
                    .append(ChatColorType.HIGHLIGHT)
                    .append("Your loot is worth around ")
                    .append(StackFormatter.formatNumber(chestPrice))
                    .append(" coins.")
                    .append(ChatColorType.NORMAL);

            chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.ITEM_EXAMINE)
                    .runeLiteFormattedMessage(message.build())
                    .build());
        }

        if (event.getGroupId() == WidgetID.CHATBOX_GROUP_ID) {
            panel.rebuild();
        }

        // Convert container items to array of ItemStack
        final Collection<ItemStack> items = Arrays.stream(container.getItems())
                .filter(item -> item.getId() > 0)
                .map(item -> new ItemStack(item.getId(), item.getQuantity(), client.getLocalPlayer().getLocalLocation()))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            log.debug("No items to find for Event: {} | Container: {}", eventType, container);
            return;
        }

        final LootTrackerItem[] entries = buildEntries(stack(items));

        SwingUtilities.invokeLater(() -> panel.add(eventType, client.getLocalPlayer().getName(), -1, entries));
        LootRecord lootRecord = new LootRecord(eventType, client.getLocalPlayer().getName(), LootRecordType.EVENT,
                toGameItems(items), Instant.now());

        final int killCount = killCountMap.getOrDefault(eventType.toUpperCase(), -1);

        saveLocalLootRecord(lootRecord);


        LTRecord record = new LTRecord(-1, eventType, -1, killCount, convertToLTItemEntries(items));
        writer.addLootTrackerRecord(record);
    }

    private void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) {
            return;
        }

        final String message = event.getMessage();

        if (PET_MESSAGES.stream().anyMatch(message::contains)) {
            gotPet = true;
        }

        if (message.equals(CHEST_LOOTED_MESSAGE) || LARRAN_LOOTED_PATTERN.matcher(message).matches()) {
            final int regionID = client.getLocalPlayer().getWorldLocation().getRegionID();
            if (!CHEST_EVENT_TYPES.containsKey(regionID)) {
                return;
            }

            eventType = CHEST_EVENT_TYPES.get(regionID);
            takeInventorySnapshot();

            return;
        }

        if (message.equals(HERBIBOAR_LOOTED_MESSAGE)) {
            eventType = HERBIBOAR_EVENT;
            takeInventorySnapshot();

            return;
        }

        // Remove all tags
        final String chatMessage = Text.removeTags(message);

        final int regionID = client.getLocalPlayer().getWorldLocation().getRegionID();
        if (HESPORI_REGION == regionID && message.equals(HESPORI_LOOTED_MESSAGE)) {
            eventType = HESPORI_EVENT;
            takeInventorySnapshot();
            return;
        }

        if (GAUNTLET_LOBBY_REGION == regionID && message.equals(GAUNTLET_LOOTED_MESSAGE)) {
            eventType = GAUNTLET_EVENT;
            takeInventorySnapshot();
            return;
        }

        // Check if message is for a clue scroll reward
        final Matcher m = CLUE_SCROLL_PATTERN.matcher(chatMessage);
        if (m.find()) {
            final String type = m.group(2).toLowerCase();
            switch (type) {
                case "beginner":
                    eventType = "Clue Scroll (Beginner)";
                    break;
                case "easy":
                    eventType = "Clue Scroll (Easy)";
                    break;
                case "medium":
                    eventType = "Clue Scroll (Medium)";
                    break;
                case "hard":
                    eventType = "Clue Scroll (Hard)";
                    break;
                case "elite":
                    eventType = "Clue Scroll (Elite)";
                    break;
                case "master":
                    eventType = "Clue Scroll (Master)";
                    break;
            }

            int killCount = Integer.parseInt(m.group(1));
            killCountMap.put(eventType.toUpperCase(), killCount);
            return;
        }

        // Barrows KC
        if (chatMessage.startsWith("Your Barrows chest count is")) {
            Matcher n = NUMBER_PATTERN.matcher(chatMessage);
            if (n.find()) {
                killCountMap.put("BARROWS", Integer.valueOf(n.group()));
                return;
            }
        }

        // Raids KC
        if (chatMessage.startsWith("Your completed Chambers of Xeric count is")) {
            Matcher n = NUMBER_PATTERN.matcher(chatMessage);
            if (n.find()) {
                killCountMap.put("CHAMBERS OF XERIC", Integer.valueOf(n.group()));
                return;
            }
        }
        // Raids KC
        if (chatMessage.startsWith("Your completed Theatre of Blood count is")) {
            Matcher n = NUMBER_PATTERN.matcher(chatMessage);
            if (n.find()) {
                killCountMap.put("THEATRE OF BLOOD", Integer.valueOf(n.group()));
                return;
            }
        }
        // Handle wintertodt
        Matcher wintertodt = WINTERTODT_NUMBER_PATTERN.matcher(chatMessage);
        if (wintertodt.find()) {
            int killCount = Integer.parseInt(wintertodt.group(1));
            killCountMap.put(WINTERTODT_EVENT, killCount);
        }
        // Handle all other boss
        Matcher boss = BOSS_NAME_NUMBER_PATTERN.matcher(chatMessage);
        if (boss.find()) {
            String bossName = boss.group(1);
            int killCount = Integer.parseInt(boss.group(2));
            killCountMap.put(bossName.toUpperCase(), killCount);
        }
    }

    @SuppressWarnings("unchecked")
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (pvpDeath && RESPAWN_REGIONS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
            Multiset snapshot;
            snapshot = inventorySnapshot;
            deathInventorySnapshot();
            if (inventorySnapshot != snapshot) {
                inventorySnapshot = snapshot;
                ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
                if (inventorySnapshot != null) {
                    Multiset<Integer> currentInventory = HashMultiset.create();
                    if (inventory != null) {
                        Arrays.stream(Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems())
                                .forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));
                    }

                    final Multiset<Integer> diff = Multisets.difference(inventorySnapshot, currentInventory);

                    List<ItemStack> itemsLost = diff.entrySet().stream()
                            .map(e -> new ItemStack(e.getElement(), (-1 * e.getCount()), client.getLocalPlayer().getLocalLocation()))
                            .collect(Collectors.toList());

                    final LootTrackerItem[] entries = buildEntries(stack(itemsLost));
                    String name = "Death: " + client.getLocalPlayer().getName();
                    SwingUtilities.invokeLater(() -> panel.add(name, client.getLocalPlayer().getName(),
                            client.getLocalPlayer().getCombatLevel(), entries));
                    LootRecord lootRecord = new LootRecord(name, client.getLocalPlayer().getName(), LootRecordType.DEATH,
                            toGameItems(itemsLost), Instant.now());

                    saveLocalLootRecord(lootRecord);

                    pvpDeath = false;
                    inventorySnapshot = null;
                }
            }

        }
        if (CHEST_EVENT_TYPES.containsValue(eventType)
                || HERBIBOAR_EVENT.equals(eventType)
                || HESPORI_EVENT.equals(eventType)
                || GAUNTLET_EVENT.equals(eventType)
                || WINTERTODT_EVENT.equals(eventType)) {
            if (event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY)) {
                return;
            }

            processChestLoot(eventType, event.getItemContainer());
            eventType = null;
        }
    }

    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getActionParam1() != WidgetInfo.INVENTORY.getId()) {
            return;
        }

        int itemId = event.getIdentifier();

        if (itemId == -1) {
            return;
        }

        String option = event.getOption();
        ItemDefinition itemComposition = client.getItemDefinition(itemId);

        if (option.equals("Open") && itemComposition.getName().equals("Supply crate")) {
            eventType = WINTERTODT_EVENT;
            takeInventorySnapshot();
        }
    }

    private void saveLocalLootRecord(LootRecord lootRecord) {
        lootRecords.add(lootRecord);
        try {
            BufferedWriter bufferedWriter = Files.newBufferedWriter(LOOT_RECORDS_FILE.toPath());

            bufferedWriter.append(RuneLiteAPI.GSON.toJson(lootRecords));
            bufferedWriter.close();
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                try {
                    LOOT_RECORDS_FILE.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    void deleteLocalRecords() {
        try {
            lootRecords.clear();
            Files.deleteIfExists(LOOT_RECORDS_FILE.toPath());
        } catch (IOException e) {
            log.error("Error deleting local loot records file.");
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Takes a snapshot of the local player's inventory and equipment right before respawn.
     */
    private void deathInventorySnapshot() {
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        inventorySnapshot = HashMultiset.create();
        if (inventory != null) {

            Arrays.stream(inventory.getItems())
                    .forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
        }

        if (equipment != null) {
            Arrays.stream(equipment.getItems())
                    .forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
        }
    }

    private void takeInventorySnapshot() {
        final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer != null) {
            inventorySnapshot = HashMultiset.create();
            Arrays.stream(itemContainer.getItems())
                    .forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
        }
    }

    private void processChestLoot(String chestType, ItemContainer inventoryContainer) {
        if (inventorySnapshot != null) {
            Multiset<Integer> currentInventory = HashMultiset.create();
            Arrays.stream(inventoryContainer.getItems())
                    .forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

            final Multiset<Integer> diff = Multisets.difference(currentInventory, inventorySnapshot);

            List<ItemStack> items = diff.entrySet().stream()
                    .map(e -> new ItemStack(e.getElement(), e.getCount(), client.getLocalPlayer().getLocalLocation()))
                    .collect(Collectors.toList());

            if (gotPet) {
                ItemStack pet = null;
                switch (chestType) {
                    case HERBIBOAR_EVENT:
                        pet = handlePet("Herbiboar");
                        break;
                    case WINTERTODT_EVENT:
                        pet = handlePet("Wintertodt");
                        break;
                    case GAUNTLET_EVENT:
                        pet = handlePet("Gauntlet");
                        break;
                }

                if (pet == null) {
                    log.warn("Error finding pet for npc name: Herbiboar");
                } else {
                    items.add(pet);
                }
            }

            final LootTrackerItem[] entries = buildEntries(stack(items));
            SwingUtilities.invokeLater(() -> panel.add(chestType, client.getLocalPlayer().getName(), -1, entries));

            LootRecord lootRecord = new LootRecord(chestType, client.getLocalPlayer().getName(),
                    LootRecordType.EVENT, toGameItems(items), Instant.now());

            saveLocalLootRecord(lootRecord);


            LTRecord record = new LTRecord(-1, chestType, -1, -1, convertToLTItemEntries(items));
            writer.addLootTrackerRecord(record);

            inventorySnapshot = null;
        }
    }

    void toggleItem(String name, boolean ignore) {
        final Set<String> ignoredItemSet = new HashSet<>(ignoredItems);

        if (ignore) {
            ignoredItemSet.add(name);
        } else {
            ignoredItemSet.remove(name);
        }

        config.setIgnoredItems(Text.toCSV(ignoredItemSet));
        this.getIgnoredItems = Text.toCSV(ignoredItemSet);
        panel.updateIgnoredRecords();
    }

    boolean isIgnored(String name) {
        return ignoredItems.contains(name);
    }

    /**
     * Toggles the hidden status for a particular record
     *
     * @param name   - The String name of the record to toggle the hidden status of
     * @param ignore - true to ignore, false to remove
     */
    void toggleNPC(String name, boolean ignore) {
        final Set<String> ignoredNPCSet = new HashSet<>(ignoredNPCs);
        if (ignore) {
            ignoredNPCSet.add(name);
        } else {
            ignoredNPCSet.remove(name);
        }

        config.setIgnoredNPCs(Text.toCSV(ignoredNPCSet));
        panel.rebuild();
    }

    /**
     * Checks to see if a record name is in the list of ignored NPCs
     *
     * @param name - The String of the name to check
     * @return - true if it is being ignored, false otherwise
     */
    boolean isIgnoredNPC(String name) {
        return ignoredNPCs.contains(name);
    }

    @VisibleForTesting
    private LootTrackerItem buildLootTrackerItem(int itemId, int quantity) {
        final ItemDefinition itemDefinition = itemManager.getItemDefinition(itemId);
        final int realItemId = itemDefinition.getNote() != -1 ? itemDefinition.getLinkedNoteId() : itemId;
        final long price;
        // If it's a death we want to get a coin value for untradeables lost
        if (!itemDefinition.isTradeable() && quantity < 0) {
            price = (long) itemDefinition.getPrice() * (long) quantity;
        } else {
            price = (long) itemManager.getItemPrice(realItemId) * (long) quantity;
        }
        final boolean ignored = ignoredItems.contains(itemDefinition.getName());

        return new LootTrackerItem(
                itemId,
                itemDefinition.getName(),
                quantity,
                price,
                ignored);
    }

    private LootTrackerItem[] buildEntries(final Collection<ItemStack> itemStacks) {
        return itemStacks.stream()
                .map(itemStack -> buildLootTrackerItem(itemStack.getId(), itemStack.getQuantity()))
                .toArray(LootTrackerItem[]::new);
    }

    private Collection<LootTrackerRecord> convertToLootTrackerRecord(final Collection<LootRecord> records) {
        Collection<LootTrackerRecord> trackerRecords = new ArrayList<>();
        for (LootRecord record : records) {
            LootTrackerItem[] drops = record.getDrops().stream().map(itemStack ->
                    buildLootTrackerItem(itemStack.getId(), itemStack.getQty())
            ).toArray(LootTrackerItem[]::new);

            trackerRecords.add(new LootTrackerRecord(record.getEventId(), record.getUsername(),
                    "", drops, record.getTime()));
        }

        return trackerRecords;
    }

    private Collection<LTItemEntry> convertToLTItemEntries(Collection<ItemStack> stacks) {
        return stacks.stream().map(i ->
        {
            final ItemDefinition c = itemManager.getItemDefinition(i.getId());
            final int id = c.getNote() == -1 ? c.getId() : c.getLinkedNoteId();
            final int price = itemManager.getItemPrice(id);
            return new LTItemEntry(c.getName(), i.getId(), i.getQuantity(), price);
        }).collect(Collectors.toList());
    }

    /**
     * Is player at the Last Man Standing minigame
     */
    private boolean isAtLMS() {
        final int[] mapRegions = client.getMapRegions();

        for (int region : LAST_MAN_STANDING_REGIONS) {
            if (ArrayUtils.contains(mapRegions, region)) {
                return true;
            }
        }

        return false;
    }

    // Pet Handling
    private ItemStack handlePet(String name) {
        gotPet = false;

        int petID = getPetId(name);
        if (petID == -1) {
            return null;
        }

        return new ItemStack(petID, 1, client.getLocalPlayer().getLocalLocation());
    }

    private int getPetId(String name) {
        Pet pet = Pet.getByBossName(name);
        if (pet != null) {
            return pet.getPetID();
        }
        return -1;
    }

    private void updateConfig() {
        this.getIgnoredItems = config.getIgnoredItems();
        this.saveLoot = config.saveLoot();
        this.chestLootChat = config.chestLootChat();
        this.sortType = config.sortType();
        this.whitelistEnabled = config.whitelistEnabled();
        this.getWhitelist = config.getWhitelist();
        this.blacklistEnabled = config.blacklistEnabled();
        this.getBlacklist = config.getBlacklist();
        this.sendLootValueMessages = config.sendLootValueMessages();
    }
}
