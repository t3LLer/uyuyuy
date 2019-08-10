/*
 * Copyright (c) 2019, Hermetism <https://github.com/Hermetism>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.helpers;

import com.github.hermetism.naturalmouse.api.MouseMotionFactory;
import com.github.hermetism.naturalmouse.support.*;
import com.github.hermetism.naturalmouse.util.FlowTemplates;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.runelite.client.plugins.helpers.HelperWidget.WidgetCombatEnabled;


@Slf4j
@PluginDescriptor(
        name = "Infinity Helper",
        type = PluginType.SKILLING
)
public class HelperPlugin extends Plugin {

    @Inject
    private Client client;
    @Inject
    private KeyManager keyManager;
    @Inject
    private HelperWidget overlaytwo;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private HelperConfig helperConfig;
    @Inject
    private HelperOverlay helperOverlay;
    @Inject
    private HelperRegion regionOverlay;
    @Inject
    private EventBus eventBus;

    private static final String CONFIG_GROUP = "helpers";
    public static int baseMove;
    public static int baseMoveFast;
    private static int baseReact;
    private static int baseReactVar;

    @Override
    protected void startUp() {

        updateConfig();
        addSubscriptions();
        HelperThread.getThreadStates();

        overlayManager.add(overlaytwo);
        overlayManager.add(helperOverlay);
        overlayManager.add(regionOverlay);
        WidgetCombatEnabled = helperConfig.widgetEnableComabat();
        log.info("[x_o] Started Helper Plugin");
    }

    private void addSubscriptions() {
        eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
        eventBus.subscribe(GameObjectSpawned.class, this, this::onGameObjectSpawned);
        eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
        eventBus.subscribe(GameObjectDespawned.class, this, this::onGameObjectDespawned);
        eventBus.subscribe(GameObjectChanged.class, this, this::onGameObjectChanged);
        eventBus.subscribe(InteractingChanged.class, this, overlaytwo::onInteractingChanged);
    }


    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlaytwo);
        overlayManager.remove(helperOverlay);
        overlayManager.remove(regionOverlay);
        log.info("[x_o] ShutDown Helper Plugin");
    }

    @Schedule(
            period = 300,
            unit = ChronoUnit.SECONDS
    )
    public void updateMotionFactory() {
        log.info("[x_o] Attempting to reset motionFactory and update Randoms");
        updateMouseMotionFactory();
    }

    @Getter
    public static MouseMotionFactory helperMotionFactory;
    public static MouseMotionFactory helperMotionFactoryFast;

    static void updateMouseMotionFactory() {
        MouseMotionFactory factory = new MouseMotionFactory();
        List<Flow> flows = new ArrayList<>();


        int randomRandom = HelperInput.getRandomNumberInRange(1, 3);
        switch (randomRandom) {
            case 1:
                flows.add(new Flow(FlowTemplates.variatingFlow()));
                break;

            case 2:
                flows.add(new Flow(FlowTemplates.slowStartupFlow()));
                break;

            case 3:
                flows.add(new Flow(FlowTemplates.slowStartup2Flow()));
                break;
        }

        randomRandom = HelperInput.getRandomNumberInRange(1, 4);
        switch (randomRandom) {

            case 1:
                flows.add(new Flow(FlowTemplates.random()));
                break;

            case 2:
                flows.add(new Flow(FlowTemplates.randomFlowLow()));
                break;

            case 3:
                flows.add(new Flow(FlowTemplates.randomFlowHigh()));
                break;

//            case 4:
//                 lowest is only good for actions like clicking on the next inv slot etc.
//                flows.add(new Flow(FlowTemplates.randomFlowLowest()));
//                break;

            case 4:
                flows.add(new Flow(FlowTemplates.randomFlowWide()));
                break;
        }

        randomRandom = HelperInput.getRandomNumberInRange(1, 3);
        switch (randomRandom) {
            case 1:
                flows.add(new Flow(FlowTemplates.jaggedFlow()));
                break;

            case 2:
                flows.add(new Flow(FlowTemplates.interruptedFlow()));
                break;

            case 3:
                flows.add(new Flow(FlowTemplates.interruptedFlow2()));
                break;
        }

        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(12));
        factory.setNoiseProvider(new DefaultNoiseProvider(2.3D));
        manager.setMouseMovementBaseTimeMs(baseMove);
        factory.getNature().setReactionTimeBaseMs(baseReact);
        factory.getNature().setReactionTimeVariationMs(baseReactVar);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        overshootManager.setOvershoots(0);

        factory.setSpeedManager(manager);
        helperMotionFactory = factory;

        manager.setMouseMovementBaseTimeMs(baseMoveFast);
        factory.setSpeedManager(manager);
        helperMotionFactoryFast = factory;


        log.info("[x_o] Helper Motion Factory has been Updated");
    }

    @Provides
    HelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HelperConfig.class);
    }

    public void updateConfig(){
        baseMove = helperConfig.baseMove();
        baseMoveFast = helperConfig.baseMoveFast();
        baseReact = helperConfig.baseReact();
        baseReactVar = helperConfig.baseReactVar();
        updateMouseMotionFactory();
        WidgetCombatEnabled = helperConfig.widgetEnableComabat();
    }

    public void onConfigChanged(ConfigChanged event) {
        if (!CONFIG_GROUP.equals(event.getGroup())) {
            return;
        }

        updateConfig();
    }

    @Getter
    private static final Set<GameObject> boothObjects = new HashSet<>();


    public void onGameObjectSpawned(final GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();
        Booth booth = Booth.findBooth(gameObject.getId());
        if (booth != null) {
            boothObjects.add(gameObject);
        }
    }

    public void onGameStateChanged(final GameStateChanged event) {
        if (event.getGameState() != GameState.LOGGED_IN) {
            boothObjects.clear();
        }
    }

    public void onGameObjectDespawned(final GameObjectDespawned event) {
        boothObjects.remove(event.getGameObject());
    }


    public void onGameObjectChanged(final GameObjectChanged event) {
        boothObjects.remove(event.getGameObject());
    }
}
