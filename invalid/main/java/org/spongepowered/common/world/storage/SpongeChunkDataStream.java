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
package org.spongepowered.common.world.storage;

import com.google.common.collect.Sets;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.RegionFile;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.storage.ChunkDataStream;
import org.spongepowered.common.accessor.world.chunk.storage.RegionFileAccessor;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Set;

public class SpongeChunkDataStream implements ChunkDataStream {

    private static class RegionFileItr {

        private final RegionFile file;
        public int index;

        public RegionFileItr(final RegionFile regionFile) {
            this.file = regionFile;
        }

        public int getNext() {
            int index = this.index;
            final int[] offsets = ((RegionFileAccessor) this.file).accessor$getOffsets();
            while (index != -1 && index < offsets.length && offsets[index] == 0) {
                index++;
            }
            if (index >= offsets.length) {
                return -1;
            }
            return index;
        }

        public DataInputStream getStreamAt(final int index) throws IOException {
            final int x = index & 31;
            final int z = index >>> 5;
            return this.file.func_222666_a(new ChunkPos(x, z));
        }

    }

    private final Set<Path> openedFiles = Sets.newHashSet();
    private RegionFileItr regionFileItr;
    private final Path worldDir;

    public SpongeChunkDataStream(final Path worldDir) {
        this.worldDir = worldDir;
    }

    private boolean itrAvailable() {
        if (this.regionFileItr != null) {
            return true;
        }
        final Iterable<Path> files = WorldStorageUtil.listRegionFiles(this.worldDir);
        for (final Path file : files) {
            if (!this.openedFiles.contains(file)) {
                this.regionFileItr = new RegionFileItr(WorldStorageUtil.getRegionFile(file));
                this.openedFiles.add(file);
                return true;
            }
        }
        return false;
    }

    private int getNextIndex() {
        int next;
        while (this.itrAvailable()) {
            if ((next = this.regionFileItr.getNext()) == -1) {
                this.regionFileItr = null;
            } else {
                return next;
            }
        }
        return -1;
    }

    @Override
    public DataContainer next() throws IOException {
        final int next = this.getNextIndex();
        if (next == -1) {
            throw new NoSuchElementException();
        }
        this.regionFileItr.index = next + 1;
        final DataInputStream stream = this.regionFileItr.getStreamAt(next);
        try {
            return WorldStorageUtil.readDataFromRegion(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return this.getNextIndex() != -1;
    }

    @Override
    public int available() {
        // Advance to the end of the stream, counting along the way. Store
        // previous state and reset to it afterwards.
        final RegionFileItr currentItr = this.regionFileItr;
        final Set<Path> currentOpenedFiles = Sets.newHashSet(this.openedFiles);
        int count = 0;
        int index = this.getNextIndex();
        while (index != -1) {
            count++;
            this.regionFileItr.index = index + 1;
            index = this.getNextIndex();
        }
        this.regionFileItr = currentItr;
        this.openedFiles.clear();
        this.openedFiles.addAll(currentOpenedFiles);
        return count;
    }

    @Override
    public void reset() {
        this.regionFileItr = null;
        this.openedFiles.clear();
    }

}
