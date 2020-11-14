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
package org.spongepowered.common.state;

import net.minecraft.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.state.StateProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class SpongeBlockStateMatcher extends AbstractStateMatcher<@NonNull BlockState, BlockType> {

    public SpongeBlockStateMatcher(final BlockType type,
            final Collection<StateProperty<@NonNull ?>> requiredProperties,
            final HashMap<StateProperty<@NonNull ?>, Object> properties,
            final Collection<KeyValueMatcher<?>> keyValueMatchers) {
        super(type, requiredProperties, properties, keyValueMatchers);
    }

    @Override
    public boolean matches(@NonNull final BlockState state) {
        return this.isValid((net.minecraft.block.BlockState) state);
    }

    @Override
    @NonNull
    public List<BlockState> getCompatibleStates() {
        if (this.compatibleStates == null) {
            final Block blockType = (Block) this.type;
            this.compatibleStates = blockType.getStateContainer().getValidStates()
                    .stream()
                    .filter(this::isValid)
                    .map(x -> (BlockState) x)
                    .collect(Collectors.toList());
        }
        return this.compatibleStates;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        // TODO: what to do here?
        final DataContainer container = DataContainer.createNew();
        return container;

    }

}
