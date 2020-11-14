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
package org.spongepowered.common.world.volume.buffer.block;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.block.ImmutableBlockVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

public class ArrayImmutableBlockBuffer extends AbstractBlockBuffer implements ImmutableBlockVolume {

    private static final BlockState AIR = BlockTypes.AIR.get().getDefaultState();

    private final Palette<BlockState> palette;
    private final ArrayMutableBlockBuffer.BackingData data;

    /**
     * Does not clone!
     *  @param palette The palette
     * @param data The backing data
     * @param start The start block position
     * @param size The block size
     */
    ArrayImmutableBlockBuffer(
        final Palette<BlockState> palette, final ArrayMutableBlockBuffer.BackingData data, final Vector3i start, final Vector3i size) {
        super(start, size);
        this.data = data;
        this.palette = palette;
    }

    public ArrayImmutableBlockBuffer(final Palette<BlockState> palette, final Vector3i start, final Vector3i size, final char[] blocks) {
        super(start, size);
        this.data = new ArrayMutableBlockBuffer.CharBackingData(blocks.clone());
        this.palette = palette;
    }

    @Override
    public Palette<BlockState> getPalette() {
        return GlobalPalette.getBlockPalette();
    }

    @Override
    public BlockState getBlock(final int x, final int y, final int z) {
        this.checkRange(x, y, z);
        return this.palette.get(this.data.get(this.getIndex(x, y, z))).orElse(ArrayImmutableBlockBuffer.AIR);
    }

    @Override
    public FluidState getFluid(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).getFluidState();
    }

    @Override
    public int getHighestYAt(final int x, final int z) {
        return 0;
    }

    /**
     * This method doesn't clone the array passed into it. INTERNAL USE ONLY.
     * Make sure your code doesn't leak the reference if you're using it.
     *
     * @param blocks The blocks to store
     * @param start The start of the volume
     * @param size The size of the volume
     * @return A new buffer using the same array reference
     */
    public static ImmutableBlockVolume newWithoutArrayClone(final Palette<BlockState> palette, final Vector3i start, final Vector3i size, final char[] blocks) {
        return new ArrayImmutableBlockBuffer(palette, new ArrayMutableBlockBuffer.CharBackingData(blocks), start, size);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ArrayImmutableBlockBuffer that = (ArrayImmutableBlockBuffer) o;
        return this.palette.equals(that.palette) &&
               this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.palette, this.data);
    }

    @Override
    public VolumeStream<ImmutableBlockVolume, BlockState> getBlockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return null;
    }
}
