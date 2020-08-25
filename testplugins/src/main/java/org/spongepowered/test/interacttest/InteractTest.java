/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.test.interacttest;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("interacttest")
public class InteractTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public InteractTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.getEventManager().registerListeners(this.plugin, new InteractListener());
    }


    private static class InteractListener {
        @Listener
        public void onInteractItem(final InteractBlockEvent event) {
            Sponge.getServer().getBroadcastAudience().sendMessage(TextComponent.of("/*************"));
            Sponge.getServer().getBroadcastAudience().sendMessage(TextComponent.builder().append("/* Event: ").append(event.getClass().getSimpleName()).build());
            Sponge.getServer().sendMessage(TextComponent.builder().append("/ Cause: ").append(event.getCause().all().toString()).build());
            Sponge.getServer().sendMessage(TextComponent.builder().append("/ Context: ").append(event.getContext().toString()).build());
        }
        @Listener
        public void onInteractItem(final InteractItemEvent event) {
            Sponge.getServer().getBroadcastAudience().sendMessage(TextComponent.of("/*************"));
            Sponge.getServer().getBroadcastAudience().sendMessage(TextComponent.builder().append("/* Event: ").append(event.getClass().getSimpleName()).build());
            Sponge.getServer().sendMessage(TextComponent.builder().append("/ Cause: ").append(event.getCause().all().toString()).build());
            Sponge.getServer().sendMessage(TextComponent.builder().append("/ Context: ").append(event.getContext().toString()).build());
        }
        @Listener
        public void onInteractItem(final InteractEntityEvent event) {
            Sponge.getServer().getBroadcastAudience().sendMessage(TextComponent.of("/*************"));
            Sponge.getServer().getBroadcastAudience().sendMessage(TextComponent.builder().append("/* Event: ").append(event.getClass().getSimpleName()).build());
            Sponge.getServer().sendMessage(TextComponent.builder().append("/ Cause: ").append(event.getCause().all().toString()).build());
            Sponge.getServer().sendMessage(TextComponent.builder().append("/ Context: ").append(event.getContext().toString()).build());
        }
    }
}
