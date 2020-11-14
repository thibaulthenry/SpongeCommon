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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.state.StateMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public final class SpongeBlockStateMatcherBuilder extends AbstractStateMatcherBuilder<@NonNull BlockState, @NonNull BlockType> {

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
    public StateMatcher.Builder<@NonNull BlockState, @NonNull BlockType> from(@NonNull final StateMatcher<@NonNull BlockState> value) {
        if (!(value instanceof SpongeBlockStateMatcher)) {
            throw new IllegalArgumentException("BlockStateMatcher must be a SpongeBlockStateMatcher");
        }
        return super.from(value);
    }

}
