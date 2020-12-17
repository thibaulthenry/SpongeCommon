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
package org.spongepowered.common.config.tracker;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class EntityTrackerCategory {

    @Setting("auto-populate")
    @Comment("If 'true', newly discovered entities will be added to this config with default settings.")
    public boolean autoPopulate = false;

    @Setting
    @Comment("Per-mod entity id mappings for controlling tracking behavior")
    public final Map<String, EntityTrackerModCategory> mods = new HashMap<>();

    public EntityTrackerCategory() {
        this.mods.put("minecraft", new EntityTrackerModCategory("minecraft"));
    }

    @ConfigSerializable
    public static final class EntityTrackerModCategory {

        @Setting
        @Comment("If 'false', all tracking for this mod will be ignored.")
        public boolean enabled = true;

        @Setting(TrackerConfig.BLOCK_BULK_CAPTURE)
        @Comment("Set to true to perform block bulk capturing during entity ticks. (Default: true)")
        public final Map<String, Boolean> blockBulkCapture = new HashMap<>();

        @Setting(TrackerConfig.BLOCK_EVENT_CREATION)
        @Comment("Set to true to create and fire block events during entity ticks. (Default: true)")
        public final Map<String, Boolean> blockEventCreation = new HashMap<>();

        @Setting(TrackerConfig.ENTITY_BULK_CAPTURE)
        @Comment("Set to true to perform entity bulk capturing during entity ticks. (Default: true)")
        public final Map<String, Boolean> entityBulkCapture = new HashMap<>();

        @Setting(TrackerConfig.ENTITY_EVENT_CREATION)
        @Comment("Set to true to create and fire entity events during entity ticks. (Default: true)")
        public final Map<String, Boolean> entityEventCreation = new HashMap<>();

        public EntityTrackerModCategory() {
        }

        public EntityTrackerModCategory(final String name) {
            if (name.equals("minecraft")) {
                // These entities don't modify the world or spawn any drops
                // Skipping bulk capturing should be transparent to plugins
                this.blockBulkCapture.put("item", false);
                this.blockBulkCapture.put("experience_orb", false);
                this.blockBulkCapture.put("leash_hitch", false);
                this.blockBulkCapture.put("painting", false);
                this.blockBulkCapture.put("armor_stand", false);
                this.blockBulkCapture.put("llama_spit", false);
            }
        }
    }
}
