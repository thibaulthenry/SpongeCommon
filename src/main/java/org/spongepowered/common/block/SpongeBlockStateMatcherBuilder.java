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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.state.StateMatcher;
import org.spongepowered.api.state.StateProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SpongeBlockStateMatcherBuilder implements StateMatcher.Builder<@NonNull BlockState, @NonNull BlockType> {

    @Nullable private BlockType type;
    private final Collection<StateProperty<@NonNull ?>> requiredProperties = new ArrayList<>();
    private final Map<StateProperty<@NonNull ?>, Object> properties = new HashMap<>();
    private final Collection<KeyValueMatcher<?>> keyValueMatchers = new ArrayList<>();

    @Override
    public StateMatcher.@NonNull Builder<@NonNull BlockState, @NonNull BlockType> type(@NonNull final BlockType type) {
        this.type = type;
        return this;
    }

    @Override
    public StateMatcher.@NonNull Builder<@NonNull BlockState, @NonNull BlockType> supportsStateProperty(@NonNull final StateProperty<@NonNull ?> stateProperty) {
        this.requiredProperties.add(stateProperty);
        return this;
    }

    @Override
    public <V extends Comparable<V>> StateMatcher.@NonNull Builder<@NonNull BlockState, @NonNull BlockType> stateProperty(
            @NonNull final StateProperty<@NonNull V> stateProperty, @NonNull final V value) {
        this.properties.put(stateProperty, value);
        return this;
    }

    @Override
    public StateMatcher.@NonNull Builder<@NonNull BlockState, @NonNull BlockType> matcher(@NonNull final KeyValueMatcher<?> matcher) {

        return null;
    }

    @Override
    @NonNull
    public StateMatcher<@NonNull BlockState> build() throws IllegalStateException {
        if (this.type == null) {
            throw new IllegalStateException("BlockType cannot be null");
        }
        return new SpongeBlockStateMatcher(this.type, new ArrayList<>(this.requiredProperties),
                new HashMap<>(this.properties),
                new ArrayList<>(this.keyValueMatchers));
    }

    @Override
    @NonNull
    public Optional<StateMatcher<@NonNull BlockState>> build(@NonNull final DataView container) throws InvalidDataException {
        return Optional.empty();
    }

    @Override
    public StateMatcher.@NonNull Builder<@NonNull BlockState, @NonNull BlockType> reset() {
        this.type = null;
        this.properties.clear();
        return this;
    }

    @Override
    public StateMatcher.Builder<@NonNull BlockState, @NonNull BlockType> from(@NonNull final StateMatcher<@NonNull BlockState> value) {
        if (!(value instanceof SpongeBlockStateMatcher)) {
            throw new IllegalArgumentException("BlockStateMatcher must be a SpongeBlockStateMatcher");
        }
        this.type = ((SpongeBlockStateMatcher) value).type;
        this.properties.clear();
        this.properties.putAll(((SpongeBlockStateMatcher) value).properties);
        return this;
    }

}
