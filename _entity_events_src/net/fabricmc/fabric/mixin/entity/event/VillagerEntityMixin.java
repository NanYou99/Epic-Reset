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

package net.fabricmc.fabric.mixin.entity.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1646;
import net.minecraft.class_1937;
import net.minecraft.class_3988;

@Mixin(class_1646.class)
abstract class VillagerEntityMixin extends class_3988 {
	VillagerEntityMixin(class_1299<? extends class_3988> entityType, class_1937 world) {
		super(entityType, world);
	}

	@ModifyArg(
			method = "onStruckByLightning",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V")
	)
	private class_1297 afterWitchConversion(class_1297 converted) {
		ServerLivingEntityEvents.MOB_CONVERSION.invoker().onConversion(this, (class_1308) converted, false);
		return converted;
	}
}
