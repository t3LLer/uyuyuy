/*
 * Copyright (c) 2017, Tyler <https://github.com/tylerthardy>
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
package net.runelite.client.config;

import net.runelite.api.Constants;
import net.runelite.client.ui.ContainableFrame;
import net.runelite.client.ui.FontManager;

import java.awt.*;

@ConfigGroup("runelite")
public interface RuneLightConfig extends Config {

    @ConfigItem(
            keyName = "enableOpacity",
            name = "Enable opacity",
            description = "Enables opacity for the whole window.<br>NOTE: This only stays enabled if your pc supports this!",
            position = 0,
            group = "Rune Lite Extended"
    )
    default boolean enableOpacity() {
        return false;
    }

    @Range(
            min = 15,
            max = 100
    )
    @ConfigItem(
            keyName = "opacityPercentage",
            name = "Opacity percentage",
            description = "Changes the opacity of the window if opacity is enabled",
            position = 1,
            group = "Rune Lite Extended"
    )
    default int opacityPercentage() {
        return 100;
    }

    @ConfigItem(
            keyName = "keyboardPin",
            name = "Keyboard bank pin",
            description = "Enables you to type your bank pin",
            position = 2,
            group = "Rune Lite Extended"
    )
    default boolean keyboardPin() {
        return false;
    }

    @ConfigItem(
            keyName = "enablePlugins",
            name = "Enable loading of external plugins",
            description = "Enable loading of external plugins",
            position = 10,
            group = "Rune Lite Extended"
    )
    default boolean enablePlugins() {
        return false;
    }

    @ConfigItem(
            keyName = "gameSize",
            name = "Game size",
            description = "The game will resize to this resolution upon starting the client",
            position = 10,
            group = "Game"
    )
    default Dimension gameSize() {
        return Constants.GAME_FIXED_SIZE;
    }

    @ConfigItem(
            keyName = "automaticResizeType",
            name = "Resize type",
            description = "Choose how the window should resize when opening and closing panels",
            position = 11,
            group = "Game"
    )
    default ExpandResizeType automaticResizeType() {
        return ExpandResizeType.KEEP_GAME_SIZE;
    }

    @ConfigItem(
            keyName = "lockWindowSize",
            name = "Lock window size",
            description = "Determines if the window resizing is allowed or not",
            position = 12,
            group = "Game"
    )
    default boolean lockWindowSize() {
        return false;
    }

    @ConfigItem(
            keyName = "containInScreen2",
            name = "Contain in screen",
            description = "Makes the client stay contained in the screen when attempted to move out of it.<br>Note: 'Always' only works if custom chrome is enabled.",
            position = 13,
            group = "Game"
    )
    default ContainableFrame.Mode containInScreen() {
        return ContainableFrame.Mode.RESIZING;
    }

    @ConfigItem(
            keyName = "rememberScreenBounds",
            name = "Remember client position",
            description = "Save the position and size of the client after exiting",
            position = 14,
            group = "Game"
    )
    default boolean rememberScreenBounds() {
        return true;
    }

    @ConfigItem(
            keyName = "uiEnableCustomChrome",
            name = "Enable custom window chrome",
            description = "Use Runelite's custom window title and borders.",
            warning = "Please restart your client after changing this setting",
            position = 15,
            group = "Game"
    )
    default boolean enableCustomChrome() {
        return true;
    }

    @ConfigItem(
            keyName = "gameAlwaysOnTop",
            name = "Enable client always on top",
            description = "The game will always be on the top of the screen",
            position = 16,
            group = "Window"
    )
    default boolean gameAlwaysOnTop() {
        return false;
    }

    @ConfigItem(
            keyName = "warningOnExit",
            name = "Display warning on exit",
            description = "Toggles a warning popup when trying to exit the client",
            position = 17,
            group = "Window"
    )
    default WarningOnExit warningOnExit() {
        return WarningOnExit.LOGGED_IN;
    }

    @ConfigItem(
            keyName = "usernameInTitle",
            name = "Show display name in title",
            description = "Toggles displaying of local player's display name in client title",
            position = 18,
            group = "Window"
    )
    default boolean usernameInTitle() {
        return true;
    }

    @ConfigItem(
            keyName = "notificationTray",
            name = "Enable tray notifications",
            description = "Enables tray notifications",
            position = 20,
            group = "Notifications"
    )
    default boolean enableTrayNotifications() {
        return true;
    }

    @ConfigItem(
            keyName = "notificationRequestFocus",
            name = "Request focus on notification",
            description = "Toggles window focus request",
            position = 21,
            group = "Notifications"
    )
    default boolean requestFocusOnNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "notificationSound",
            name = "Enable sound on notifications",
            description = "Enables the playing of a beep sound when notifications are displayed",
            position = 22,
            group = "Notifications"
    )
    default boolean enableNotificationSound() {
        return true;
    }

    @ConfigItem(
            keyName = "notificationGameMessage",
            name = "Enable game message notifications",
            description = "Puts a notification message in the chatbox",
            position = 23,
            group = "Notifications"
    )
    default boolean enableGameMessageNotification() {
        return false;
    }

    @ConfigItem(
            keyName = "notificationFlash",
            name = "Enable flash notification",
            description = "Flashes the game frame as a notification",
            position = 24,
            group = "Notifications"
    )
    default FlashNotification flashNotification() {
        return FlashNotification.DISABLED;
    }

    @ConfigItem(
            keyName = "notificationFocused",
            name = "Send notifications when focused",
            description = "Toggles all notifications for when the client is focused",
            position = 25,
            group = "Notifications"
    )
    default boolean sendNotificationsWhenFocused() {
        return false;
    }

    @ConfigItem(
            keyName = "clientFont",
            name = "Font",
            description = "Configure what font is used for the client and runelite added overlays",
            position = 29,
            group = "Font"
    )
    default Font clientFont() {
        return FontManager.getRunescapeFont();
    }

    @ConfigItem(
            keyName = "fontType",
            name = "Dynamic Overlay Font",
            description = "Configures what font type is used for in-game overlays such as player name, ground items, etc.",
            position = 30,
            group = "Font"
    )
    default FontType fontType() {
        return FontType.SMALL;
    }

    @ConfigItem(
            keyName = "tooltipFontType",
            name = "Tooltip Font",
            description = "Configures what font type is used for in-game tooltips such as food stats, NPC names, etc.",
            position = 31,
            group = "Font"
    )
    default FontType tooltipFontType() {
        return FontType.SMALL;
    }

    @ConfigItem(
            keyName = "interfaceFontType",
            name = "Interface Overlay Font",
            description = "Configures what font type is used for in-game interface overlays such as panels, opponent info, clue scrolls etc.",
            position = 32,
            group = "Font"
    )
    default FontType interfaceFontType() {
        return FontType.REGULAR;
    }

    @ConfigItem(
            keyName = "menuEntryShift",
            name = "Require Shift for overlay menu",
            description = "Overlay right-click menu will require shift to be added",
            position = 33,
            group = "Game"
    )
    default boolean menuEntryShift() {
        return true;
    }

    @ConfigItem(
            keyName = "infoBoxVertical",
            name = "Display infoboxes vertically",
            description = "Toggles the infoboxes to display vertically",
            position = 40,
            group = "Info Boxes"
    )
    default boolean infoBoxVertical() {
        return false;
    }

    @ConfigItem(
            keyName = "infoBoxWrap",
            name = "Infobox wrap count",
            description = "Configures the amount of infoboxes shown before wrapping",
            position = 41,
            group = "Info Boxes"
    )
    default int infoBoxWrap() {
        return 4;
    }

    @ConfigItem(
            keyName = "infoBoxSize",
            name = "Infobox size (px)",
            description = "Configures the size of each infobox in pixels",
            position = 42,
            group = "Info Boxes"
    )
    default int infoBoxSize() {
        return 35;
    }

    @Range(max = 100, min = 0)
    @ConfigItem(
            keyName = "volume",
            name = "Runelite Volume",
            description = "Sets the volume of custom Runelite sounds (not the client sounds)",
            position = 43,
            group = "Info Boxes"
    )
    default int volume() {
        return 100;
    }

}
