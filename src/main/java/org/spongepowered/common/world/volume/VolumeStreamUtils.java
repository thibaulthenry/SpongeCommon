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
package org.spongepowered.common.world.volume;

import net.minecraft.block.BlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.volume.game.ReadableRegion;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class VolumeStreamUtils {

    private VolumeStreamUtils() {}

    public static <R extends ReadableRegion<R>> BiFunction<R, ChunkPos, @Nullable Chunk> getChunkAccessorByStatus(
        final IWorldReader worldReader,
        final boolean shouldGenerate
    ) {
        return (world, chunkPos) -> {
            final ChunkStatus chunkStatus = shouldGenerate
                ? ChunkStatus.EMPTY
                : ChunkStatus.FULL;
            final @Nullable IChunk ichunk = worldReader.getChunk(chunkPos.x, chunkPos.z, chunkStatus, shouldGenerate);
            if (shouldGenerate) {
                Objects.requireNonNull(ichunk, "Chunk was expected to load fully and generate, but somehow got a null chunk!");
            }
            return (Chunk) ichunk;
        };
    }

    public static Function<Chunk, Stream<Map.Entry<BlockPos, BlockState>>> getBlockStatesForSections() {
        return chunk -> Arrays.stream(chunk.getSections()).flatMap(chunkSection -> {
            final ChunkSection section = chunkSection;
            final ChunkPos pos = chunk.getPos();
            return IntStream.range(0, 16)
                .mapToObj(x -> IntStream.range(0, 16)
                    .mapToObj(y -> IntStream.range(0, 16)
                        .mapToObj(z ->
                            {
                                final BlockPos blockPos = new BlockPos(x + pos.x << 4, y + section.getYLocation(), z + pos.z << 4);
                                return new AbstractMap.SimpleEntry<>(blockPos, section.getBlockState(x, y, z));
                            }
                        )))
                .flatMap(Function.identity())
                .flatMap(Function.identity());
        });
    }

    public static Function<Chunk, Stream<Map.Entry<BlockPos, Biome>>> getBiomesForChunkByPos() {
        return chunk -> Arrays.stream(chunk.getSections()).flatMap(
            chunkSection -> {
                final ChunkSection section = chunkSection;
                final ChunkPos pos = chunk.getPos();
                return IntStream.range(0, 16)
                    .mapToObj(x -> IntStream.range(0, 16)
                        .mapToObj(y -> IntStream.range(0, 16)
                            .mapToObj(z ->
                                {
                                    final BlockPos blockPos = new BlockPos(x + pos.x << 4, y + section.getYLocation(), z + pos.z << 4);
                                    final Biome biome = chunk.getBiomes() == null
                                        ? chunk.getWorld().getNoiseBiomeRaw(blockPos.getX(), blockPos.getY(), blockPos.getZ())
                                        : chunk.getBiomes().getNoiseBiome(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                                    return new AbstractMap.SimpleEntry<>(blockPos, biome);
                                }
                            )))
                    .flatMap(Function.identity())
                    .flatMap(Function.identity());
            });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <R extends ReadableRegion<R>, API, MC, Section, KeyReference> VolumeStream<R, API> generateStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options,
        final R ref,
        final BiConsumer<KeyReference, MC> identityFunction,
        final BiFunction<R, ChunkPos, Section> chunkAccessor,
        final BiFunction<BlockPos, MC, KeyReference> entityToKey,
        final Function<Section, Stream<Map.Entry<BlockPos, MC>>> entityAccessor,
        final BiFunction<KeyReference, R, Tuple<BlockPos, MC>> filteredPositionEntityAccessor
    ) {
        final WeakReference<R> worldRef = new WeakReference<>(ref);
        final Supplier<R> worldSupplier = () -> {
            final @Nullable R world = worldRef.get();
            return Objects.requireNonNull(world, "World de-referenced");
        };
        final BlockPos chunkMin = new BlockPos(min.getX() >> 4, 0, min.getZ() >> 4);
        final BlockPos chunkMax = new BlockPos(max.getX() >> 4, 0, max.getZ() >> 4);


        final Stream<VolumeElement<R, API>> volumeStreamBacker;

        final Stream<ChunkPos> chunkPosStream = IntStream.range(chunkMin.getX(), chunkMax.getX() + 1)
            .mapToObj(x -> IntStream.range(
                chunkMin.getZ(),
                chunkMax.getZ() + 1
                )
                    .mapToObj(z -> new ChunkPos(x, z))
            )
            .flatMap(Function.identity());

        final Function<Tuple<BlockPos, MC>, VolumeElement<R, API>> elementGenerator = (tuple) -> {
            final WeakReference<API> blockEntityRef = new WeakReference(tuple.getB());
            final Supplier<API> blockEntitySupplier = () -> {
                final @Nullable API api = blockEntityRef.get();
                return Objects.requireNonNull(api, "BlockEntity de-referenced in a VolumeStream");
            };
            final Vector3i blockEntityPos = VecHelper.toVector3i(tuple.getA());
            return VolumeElement.of(worldSupplier, blockEntitySupplier, blockEntityPos);
        };
        final BiConsumer<Map.Entry<BlockPos, MC>, Set<KeyReference>> entryConsumer = (entry, poses) -> {
            final BlockPos pos = entry.getKey();
            final Vector3i v = VecHelper.toVector3i(pos);
            if (v.compareTo(min) >= 0 && v.compareTo(max) <= 0) {
                final KeyReference keyRef = entityToKey.apply(pos, entry.getValue());
                poses.add(keyRef);
                identityFunction.accept(keyRef, entry.getValue());
            }
        };
        final Stream<KeyReference> filteredPosStream;
        if (options.loadingStyle().immediateLoading()) {
            final Set<KeyReference> availableTileEntityPositions = new LinkedHashSet<>();
            chunkPosStream
                .map(pos -> chunkAccessor.apply(ref, pos))
                .map(entityAccessor)
                .forEach((map) -> map.forEach(entry -> entryConsumer.accept(entry, availableTileEntityPositions)));
            filteredPosStream = availableTileEntityPositions.stream();
        } else {
            filteredPosStream = chunkPosStream
                .flatMap(chunkPos -> {
                    final Set<KeyReference> blockEntityPoses = new LinkedHashSet<>();
                    entityAccessor.apply(chunkAccessor.apply(ref, chunkPos))
                        .forEach(entry -> entryConsumer.accept(entry, blockEntityPoses));
                    return blockEntityPoses.stream();
                });
        }
        volumeStreamBacker = filteredPosStream.map(pos -> filteredPositionEntityAccessor.apply(pos, ref))
            .filter(tuple -> Objects.nonNull(tuple.getB()))
            .map(elementGenerator);
        return new SpongeVolumeStream<>(volumeStreamBacker, worldSupplier);
    }

}
