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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.BlockStateArgument;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Optional;

public class SpongeBlockStateBuilder extends AbstractDataBuilder<BlockState> implements BlockState.Builder {

    private BlockState blockState = BlockTypes.STONE.get().getDefaultState();

    public SpongeBlockStateBuilder() {
        super(BlockState.class, 1);
    }

    @Override
    public BlockState.Builder blockType(final BlockType blockType) {
        this.blockState = Objects.requireNonNull(blockType).getDefaultState();
        return this;
    }


    @Override
    public <V> SpongeBlockStateBuilder add(final Key<? extends Value<V>> key, final V value) {
        Objects.requireNonNull(key, "key");
        this.blockState = this.blockState.with(key, value).orElse(this.blockState);
        return this;
    }

    @Override
    public SpongeBlockStateBuilder from(final BlockState holder) {
        this.blockState = holder;
        return this;
    }

    @Override
    public SpongeBlockStateBuilder reset() {
        this.blockState = BlockTypes.STONE.get().getDefaultState();
        return this;
    }

    @Override
    public BlockState build() {
        return this.blockState;
    }

    @Override
    protected Optional<BlockState> buildContent(final DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Block.BLOCK_STATE)) {
            return Optional.empty();
        }
        DataUtil.checkDataExists(container, Constants.Block.BLOCK_STATE);
        try {
            return container.getString(Constants.Block.BLOCK_STATE).flatMap(BlockStateSerializerDeserializer::deserialize);
        } catch (final Exception e) {
            throw new InvalidDataException("Could not retrieve a blockstate!", e);
        }
    }

    @Override
    public BlockState.Builder fromString(final String id) {
        this.blockState = BlockStateSerializerDeserializer.deserialize(id)
                .orElseThrow(() -> new IllegalArgumentException("The provided state is not valid."));
        return this;
    }

}
