/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.entity.event.elytra;

import com.mojang.authlib.GameProfile;
import net.minecraft.class_1802;
import net.minecraft.class_2848;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(class_746.class)
abstract class ClientPlayerEntityMixin extends class_742 {
	ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
		super(world, profile);
		throw new AssertionError();
	}

	@Shadow
	@Final
	private class_634 networkHandler;

	/**
	 * Call {@link #method_23668()} even if the player is not wearing {@link class_1802#field_8833} to allow custom elytra flight.
	 */
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EquipmentSlot;CHEST:Lnet/minecraft/entity/EquipmentSlot;"), method = "tickMovement", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isClimbing()Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;checkFallFlying()Z")), allow = 1)
	void injectElytraStart(CallbackInfo info) {
		// Note that if fall flying is not ALLOWed, checkFallFlying will return false and nothing will happen.
		if (this.method_23668()) {
			networkHandler.method_52787(new class_2848(this, class_2848.class_2849.field_12982));
		}
	}
}
