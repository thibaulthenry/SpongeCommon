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
package org.spongepowered.common.block;

import net.minecraft.block.Block;
import net.minecraft.state.IProperty;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.state.StateMatcher;
import org.spongepowered.api.state.StateProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SpongeBlockStateMatcher implements StateMatcher<@NonNull BlockState> {

    @MonotonicNonNull private List<BlockState> compatibleStates;

    final BlockType type;
    final Map<StateProperty<@NonNull ?>, Object> properties;
    final Collection<KeyValueMatcher<?>> keyValueMatchers;
    final Collection<StateProperty<@NonNull ?>> requiredProperties;

    public SpongeBlockStateMatcher(final BlockType type,
            final Collection<StateProperty<@NonNull ?>> requiredProperties,
            final HashMap<StateProperty<@NonNull ?>, Object> properties,
            final Collection<KeyValueMatcher<?>> keyValueMatchers) {
        this.type = type;
        this.requiredProperties = requiredProperties;
        this.properties = properties;
        this.keyValueMatchers = keyValueMatchers;
    }

    @Override
    public boolean matches(@NonNull final BlockState state) {
        return !this.getCompatibleStates().isEmpty();
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

    private boolean isValid(final net.minecraft.block.BlockState blockState) {
        for (final Map.Entry<StateProperty<@NonNull ?>, Object> entry : this.properties.entrySet()) {
            final IProperty<?> property = (IProperty<?>) entry.getKey();
            if (!blockState.has(property) && !blockState.get(property).equals(entry.getValue())) {
                return false;
            }
        }
        for (final StateProperty<@NonNull ?> entry : this.requiredProperties) {
            final IProperty<?> property = (IProperty<?>) entry;
            if (!blockState.has(property)) {
                return false;
            }
        }
        final BlockState spongeBlockState = (BlockState) blockState;
        for (final KeyValueMatcher<?> valueMatcher : this.keyValueMatchers) {
            if (!this.matches(spongeBlockState, valueMatcher)) {
                return false;
            }
        }
        return true;
    }

    private <V> boolean matches(final BlockState blockState, final KeyValueMatcher<V> keyValueMatcher) {
        return blockState.get(keyValueMatcher.getKey()).map(keyValueMatcher::matches).orElse(false);
    }

}
