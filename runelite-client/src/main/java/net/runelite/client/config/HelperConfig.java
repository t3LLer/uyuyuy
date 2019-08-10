/*
 * Copyright (c) 2018 Abex
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

@ConfigGroup("helpers")
public interface HelperConfig extends Config {

    // If you manually change these settings you might break an active plugin.

    @ConfigItem(
            keyName = "widgetEnableCombat",
            name = "NPC Combat Overlay",
            description = "Enables or Disables the NPC Combat Overlay"
    )
    default boolean widgetEnableComabat() {
        return true;
    }

    @ConfigItem(
            keyName = "playerWidgets",
            name = "Player Widgets",
            description = "Enables or Disables Player related Widgets"
    )
    default boolean playerWidgets() {
        return true;
    }

    @ConfigItem(
            keyName = "widgetNPCprompts",
            name = "NPC Widgets",
            description = "Enables or Disables NPC related Widgets"
    )
    default boolean widgetNPCprompts() {
        return true;
    }

    @ConfigItem(
            keyName = "widgetRunecrafting",
            name = "Runcrafting Widgets",
            description = "Enables or Disables Runecrafting related widgets"
    )
    default boolean widgetRunecrafting() {
        return true;
    }

    @ConfigItem(
            keyName = "widgetBanking",
            name = "Banking Widgets",
            description = "Enables or Disables Banking/Inventory related widgets"
    )
    default boolean widgetBanking() {
        return true;
    }

    @ConfigItem(
            keyName = "showOpponentCountInfo",
            name = "Opponent Info",
            description = "Show Number of and Name of Opponents"
    )
    default boolean showOpponentCountInfo() {
        return true;
    }

    @ConfigItem(
            keyName = "showDebug",
            name = "[D] Debug Info",
            description = "Show Widget Debug Info"
    )
    default boolean showDebug() {
        return false;
    }

    @ConfigItem(
            keyName = "showRegionInfo",
            name = "[D] Region Info",
            description = "Show Region Debug Info"
    )
    default boolean showRegionInfo() {
        return false;
    }

    @ConfigItem(
            keyName = "showWidgetInfo",
            name = "[D] Widget Info",
            description = "Show Widget Debug Info"
    )
    default boolean showWidgetInfo() {
        return false;
    }


    @ConfigItem(
            keyName = "showOpponentInfo",
            name = "[D] Opponent Info",
            description = "Show Opponent Debug Info"
    )
    default boolean showOpponentInfo() {
        return false;
    }


    @ConfigItem(
            keyName = "mouseSnapShot",
            name = "[D] Mouse Snapshot",
            description = "Show Mouse Snapshot Debug Info"
    )
    default boolean showMouseSnapShot() {
        return false;
    }


    @Range(
            max = 2000,
            min = 250
    )
    @ConfigItem(
            keyName = "basemovetimeSlow",
            name = "Base Move Time",
            description = "The base movement speed without random additions etc",
            group = "Movement"
    )
    default int baseMove() {
        return 250;
    }

    @Range(
            max = 500,
            min = 70
    )
    @ConfigItem(
            keyName = "basemovetimeFast",
            name = "Fast Base Time",
            description = "The base movement time for fast actions",
            group = "Movement"
    )
    default int baseMoveFast() {
        return 125;
    }

    @Range(
            max = 500,
            min = 1
    )
    @ConfigItem(
            keyName = "reactTime",
            name = "Base React Time",
            description = "The base reaction speed without random additions etc",
            group = "Movement"
    )
    default int baseReact() {
        return 45;
    }

    @Range(
            max = 500,
            min = 1
    )
    @ConfigItem(
            keyName = "reactTimeVariation",
            name = "React Time Variation",
            description = "The variation in your reaction speed",
            group = "Movement"
    )
    default int baseReactVar() {
        return 45;
    }
}
