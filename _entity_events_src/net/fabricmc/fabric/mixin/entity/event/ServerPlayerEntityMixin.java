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

import java.util.List;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3902;
import net.minecraft.class_5321;

@Mixin(class_3222.class)
abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
	@Shadow
	public abstract class_3218 getServerWorld();

	/**
	 * Minecraft by default does not call Entity#onKilledOther for a ServerPlayerEntity being killed.
	 * This is a Mojang bug.
	 * This is implements the method call on the server player entity and then calls the corresponding event.
	 */
	@Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getPrimeAdversary()Lnet/minecraft/entity/LivingEntity;"))
	private void callOnKillForPlayer(class_1282 source, CallbackInfo ci) {
		final class_1297 attacker = source.method_5529();

		// If the damage source that killed the player was an entity, then fire the event.
		if (attacker != null) {
			attacker.method_5874(this.getServerWorld(), (class_3222) (Object) this);
			ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity(this.getServerWorld(), attacker, (class_3222) (Object) this);
		}
	}

	@Inject(method = "onDeath", at = @At("TAIL"))
	private void notifyDeath(class_1282 source, CallbackInfo ci) {
		ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((class_3222) (Object) this, source);
	}

	/**
	 * This is called by {@code teleportTo}.
	 */
	@Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
	private void afterWorldChanged(class_3218 origin, CallbackInfo ci) {
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.invoker().afterChangeWorld((class_3222) (Object) this, origin, this.getServerWorld());
	}

	@Inject(method = "copyFrom", at = @At("TAIL"))
	private void onCopyFrom(class_3222 oldPlayer, boolean alive, CallbackInfo ci) {
		ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer(oldPlayer, (class_3222) (Object) this, alive);
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
	private Comparable<?> redirectSleepDirection(class_2680 state, class_2769<?> property, class_2338 pos) {
		class_2350 initial = state.method_28498(property) ? (class_2350) state.method_11654(property) : null;
		return EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((class_1309) (Object) this, pos, initial);
	}

	@Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;", shift = At.Shift.BY, by = 3), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void onTrySleepDirectionCheck(class_2338 pos, CallbackInfoReturnable<Either<class_1657.class_1658, class_3902>> info, @Nullable class_2350 sleepingDirection) {
		// This checks the result from the event call above.
		if (sleepingDirection == null) {
			info.setReturnValue(Either.left(class_1657.class_1658.field_7528));
		}
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
	private void onSetSpawnPoint(class_3222 player, class_5321<class_1937> dimension, class_2338 pos, float angle, boolean spawnPointSet, boolean sendMessage) {
		if (EntitySleepEvents.ALLOW_SETTING_SPAWN.invoker().allowSettingSpawn(player, pos)) {
			player.method_26284(dimension, pos, angle, spawnPointSet, sendMessage);
		}
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
	private boolean hasNoMonstersNearby(List<class_1588> monsters, class_2338 pos) {
		boolean vanillaResult = monsters.isEmpty();
		class_1269 result = EntitySleepEvents.ALLOW_NEARBY_MONSTERS.invoker().allowNearbyMonsters((class_1657) (Object) this, pos, vanillaResult);
		return result != class_1269.field_5811 ? result.method_23665() : vanillaResult;
	}

	@Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
	private boolean redirectDaySleepCheck(class_1937 world, class_2338 pos) {
		boolean day = world.method_8530();
		class_1269 result = EntitySleepEvents.ALLOW_SLEEP_TIME.invoker().allowSleepTime((class_1657) (Object) this, pos, !day);

		if (result != class_1269.field_5811) {
			return !result.method_23665(); // true from the event = night-like conditions, so we have to invert
		}

		return day;
	}
}
