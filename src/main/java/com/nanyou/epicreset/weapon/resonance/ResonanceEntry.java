package com.nanyou.epicreset.weapon.resonance;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record ResonanceEntry(
        MaterialKey mat,
        WeaponType type,
        Map<StatCategory, Double> mods,
        Function<ServerPlayer, Map<StatCategory, Double>> extraDynamicMods,
        BiConsumer<ServerPlayer, LivingEntity> onHit,
        BiConsumer<ServerPlayer, DamageSource> onPlayerHurt
) {
    public static ResonanceEntry of(MaterialKey mat, WeaponType type,
                                    Map<StatCategory, Double> mods) {
        return new ResonanceEntry(mat, type, mods, p -> Map.of(), (p, t) -> {}, (p, s) -> {});
    }

    public static ResonanceEntry of(MaterialKey mat, WeaponType type,
                                    Map<StatCategory, Double> mods,
                                    BiConsumer<ServerPlayer, LivingEntity> onHit) {
        return new ResonanceEntry(mat, type, mods, p -> Map.of(), onHit == null ? (p, t) -> {} : onHit, (p, s) -> {});
    }
}
