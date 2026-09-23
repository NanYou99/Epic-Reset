package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;

public final class RockCrystalArmorSet {

    public static final String ID = "rock_crystal";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET,     ModItems.ROCK_CRYSTAL_HELMET,     "armorset.rock_crystal.helmet")
                .piece(ArmorItem.Type.CHESTPLATE, ModItems.ROCK_CRYSTAL_CHESTPLATE, "armorset.rock_crystal.chestplate")
                .piece(ArmorItem.Type.LEGGINGS,   ModItems.ROCK_CRYSTAL_LEGGINGS,   "armorset.rock_crystal.leggings")
                .piece(ArmorItem.Type.BOOTS,      ModItems.ROCK_CRYSTAL_BOOTS,      "armorset.rock_crystal.boots")
                .twoPiece("armorset.rock_crystal.two", (player, count) -> {
                    ensureEffect(player, MobEffects.NIGHT_VISION, 260, 0);
                    ensureEffect(player, MobEffects.DIG_SPEED,    240, 1);
                    player.removeEffect(MobEffects.DIG_SLOWDOWN);
                })
                .fourPiece("armorset.rock_crystal.four", (player, count) -> {
                    ensureEffect(player, MobEffects.NIGHT_VISION, 260, 0);
                    ensureEffect(player, MobEffects.DIG_SPEED,    240, 2);
                    ensureEffect(player, MobEffects.SLOW_FALLING, 240, 0);
                    ensureEffect(player, MobEffects.REGENERATION, 240, 0);
                    player.removeEffect(MobEffects.DIG_SLOWDOWN);
                })
                .build();
    }

    private static void ensureEffect(ServerPlayer player, MobEffect effect, int duration, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < 40 || current.getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, true));
        }
    }
}