package com.nanyou.epicreset.weapon.interact;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import com.nanyou.epicreset.weapon.resonance.MaterialKey;
import com.nanyou.epicreset.weapon.resonance.WeaponType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.function.BiConsumer;

public record WeaponInteractEntry(
        WeaponType type,
        MaterialKey fullArmorMat,
        Map<StatCategory, Double> mods,
        BiConsumer<ServerPlayer, LivingEntity> onHit,
        BiConsumer<ServerPlayer, LivingEntity> onShoot,
        BiConsumer<ServerPlayer, LivingEntity> onChargedHit,
        BiConsumer<ServerPlayer, Float> onBlockSuccess
) {
    public static WeaponInteractEntry statOnly(WeaponType wt, MaterialKey mat,
                                               Map<StatCategory, Double> mods) {
        return new WeaponInteractEntry(wt, mat, mods,
                (p, t) -> {}, (p, t) -> {}, (p, t) -> {}, (p, f) -> {});
    }
}
