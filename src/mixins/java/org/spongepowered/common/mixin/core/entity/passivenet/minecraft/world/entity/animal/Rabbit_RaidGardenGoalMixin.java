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
package org.spongepowered.common.mixin.core.entity.passivenet.minecraft.world.entity.animal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.entity.GrieferBridge;

@Mixin(targets = "net/minecraft/world/entity/animal/Rabbit$RaidGardenGoal")
public abstract class Rabbit_RaidGardenGoalMixin extends MoveToBlockGoal {

    // @formatter:off
    @Shadow @Final private Rabbit rabbit;
    // @formatter:on

    public Rabbit_RaidGardenGoalMixin(final PathfinderMob entityCreature, final double a, final int b) {
        super(entityCreature, a, b);
    }

    /**
     * @author gabizou - April 13th, 2018
     * @reason Forge changes the gamerule method calls, so the old injection/redirect
     * would fail in forge environments. This changes the injection to a predictable
     * place where we still can forcibly call things but still cancel as needed.
     *
     * @param cir
     */
    @Inject(
        method = "canUse()Z",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private void impl$onCanGrief(final CallbackInfoReturnable<Boolean> cir) {
        if (this.nextStartTick <= 0) {
            if (!((GrieferBridge) this.rabbit).bridge$canGrief()) {
                cir.setReturnValue(false);
            }
        }
    }
}
