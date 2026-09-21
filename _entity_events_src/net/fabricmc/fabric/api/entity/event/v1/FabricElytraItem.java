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

package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1770;
import net.minecraft.class_1799;
import net.minecraft.class_5712;

/**
 * An interface that can be implemented on an item to provide custom elytra flight when it is worn in the {@link class_1304#field_6174} slot.
 *
 * <p>To disable cape rendering when this item is worn (like the vanilla elytra item), have a look at {@code LivingEntityFeatureRenderEvents}.
 */
public interface FabricElytraItem {
	/**
	 * Try to use this custom elytra.
	 *
	 * @param entity     the entity
	 * @param chestStack the stack currently worn in the chest slot, will always be of this item
	 * @param tickElytra true to tick the elytra, false to only perform the check; vanilla-like elytras can use {@link #doVanillaElytraTick} to handle ticking
	 * @return true to enable elytra flight for the entity
	 */
	default boolean useCustomElytra(class_1309 entity, class_1799 chestStack, boolean tickElytra) {
		if (class_1770.method_7804(chestStack)) {
			if (tickElytra) {
				doVanillaElytraTick(entity, chestStack);
			}

			return true;
		}

		return false;
	}

	/**
	 * A helper to perform the default vanilla elytra tick logic: damage the elytra every 20 ticks, and send a game event every 10 ticks.
	 */
	default void doVanillaElytraTick(class_1309 entity, class_1799 chestStack) {
		int nextRoll = entity.method_6003() + 1;

		if (!entity.method_37908().field_9236 && nextRoll % 10 == 0) {
			if ((nextRoll / 10) % 2 == 0) {
				chestStack.method_7970(1, entity, class_1304.field_6174);
			}

			entity.method_32876(class_5712.field_28158);
		}
	}
}
