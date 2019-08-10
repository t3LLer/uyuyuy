/*
 * Copyright (c) 2018, Joris K <kjorisje@gmail.com>
 * Copyright (c) 2018, Lasse <cronick@zytex.dk>
 * Copyright (c) 2019, ermalsh <github.com/ermalsh>
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
package net.runelite.client.plugins.simpleclick;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

import static net.runelite.client.plugins.simpleclick.SimpleClickThread.isBusy;

@Singleton
public class SimpleClickOverlay extends Overlay {
    private final SimpleClickPlugin plugin;
    private final SimpleClickConfig config;

    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private SimpleClickOverlay(final SimpleClickPlugin plugin, final SimpleClickConfig config) {
        setPosition(OverlayPosition.TOP_LEFT);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Check Thread States (Not using Helper in this Plugin)
        SimpleClickThread.getThreadState();

        // Run Tasks
        SimpleClickTaskMan.runTasks(config, plugin);

        panelComponent.setPreferredSize(new Dimension(145, 0));
        panelComponent.getChildren().clear();

        // Show if thread is busy on the overlay
        Color c = Color.GREEN;
        if (isBusy()) {
            c = Color.RED;
        }
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Simple")
                .color(c)
                .build());

        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

        // Current Min Click Time
        tableComponent.addRow("currentMin:", Integer.toString(plugin.getCurrentMin()));

        // Current Max Click Time
        tableComponent.addRow("currentMax:", Integer.toString(plugin.getCurrentMax()));

        panelComponent.getChildren().add(tableComponent);

        return panelComponent.render(graphics);
    }
}
