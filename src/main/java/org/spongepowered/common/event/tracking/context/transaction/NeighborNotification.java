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
package org.spongepowered.common.event.tracking.context.transaction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.server.SpongeLocatableBlockBuilder;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class NeighborNotification extends GameTransaction<NotifyNeighborBlockEvent> {
    final BlockState original;
    final BlockPos notifyPos;
    final Block sourceBlock;
    final BlockPos sourcePos;
    // State definitions
    final BlockPos affectedPosition;
    final BlockState originalState;
    private final Supplier<ServerLevel> serverWorld;
    private Supplier<LocatableBlock> locatableBlock;

    NeighborNotification(final Supplier<ServerLevel> serverWorldSupplier,
        final BlockState notifyState, final BlockPos notifyPos,
        final Block sourceBlock, final BlockPos sourcePos
    ) {
        super(TransactionTypes.NEIGHBOR_NOTIFICATION.get(), ((org.spongepowered.api.world.server.ServerWorld) serverWorldSupplier.get()).getKey());
        this.affectedPosition = sourcePos;
        this.originalState = notifyState;
        this.serverWorld = serverWorldSupplier;
        this.notifyPos = notifyPos;
        this.sourceBlock = sourceBlock;
        this.sourcePos = sourcePos;
        this.original = serverWorldSupplier.get().getBlockState(sourcePos);
        // This is one way to have lazily initialized fields
        this.locatableBlock = () -> {
            final LocatableBlock locatableBlock = new SpongeLocatableBlockBuilder()
                .world(this.serverWorld)
                .position(this.sourcePos.getX(), this.sourcePos.getY(), this.sourcePos.getZ())
                .state((org.spongepowered.api.block.BlockState) this.original)
                .build();
            this.locatableBlock = () -> locatableBlock;
            return locatableBlock;
        };
    }

    @Override
    public String toString() {
        return new StringJoiner(
            ", ",
            org.spongepowered.common.event.tracking.context.transaction.NeighborNotification.class.getSimpleName() + "[",
            "]"
        )
            .add("notifyState=" + this.originalState)
            .add("notifyPos=" + this.notifyPos)
            .add("sourceBlock=" + this.sourceBlock)
            .add("sourcePos=" + this.sourcePos)
            .add("actualSourceState=" + this.originalState)
            .toString();
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable GameTransaction<@NonNull ?> parent
    ) {
        return Optional.of((context, frame) -> {
            frame.pushCause(this.locatableBlock.get());
        });
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {
        printer.add("NeighborNotification")
            .add(" %s : %s, %s", "Source Pos", this.sourceBlock, this.sourcePos)
            .add(" %s : %s, %s", "Notification", this.originalState, this.notifyPos);
    }

    @Override
    public Optional<NotifyNeighborBlockEvent> generateEvent(final PhaseContext<@NonNull ?> context,
        final @Nullable GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<NotifyNeighborBlockEvent>> transactions,
        final Cause currentCause,
        ImmutableMultimap.Builder<TransactionType, ? extends Event> transactionPostEventBuilder
    ) {
        // TODO - for all neighbor notifications in the transactions find the direction of notification being used and pump into map.

        return Optional.empty();
    }

    @Override
    public void restore() {

    }

    @Override
    public boolean markCancelledTransactions(final NotifyNeighborBlockEvent event,
        final ImmutableList<? extends GameTransaction<NotifyNeighborBlockEvent>> blockTransactions
    ) {
        return false;
    }

}
