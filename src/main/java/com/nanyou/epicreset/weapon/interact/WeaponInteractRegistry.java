package com.nanyou.epicreset.weapon.interact;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import com.nanyou.epicreset.weapon.resonance.MaterialKey;
import com.nanyou.epicreset.weapon.resonance.WeaponType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumMap;
import java.util.Map;

import static com.nanyou.epicreset.armorset.stat.StatCategory.*;

public final class WeaponInteractRegistry {
    private WeaponInteractRegistry() {}

    private static Map<StatCategory, Double> m(Object... kvs) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        for (int i = 0; i < kvs.length; i += 2) m.put((StatCategory) kvs[i], (Double) kvs[i+1]);
        return m;
    }

    public static void registerAll() {
        WeaponInteractManager mgr = WeaponInteractManager.getInstance();

        /* ===== Intrinsics (弓/弩/三叉戟/盾牌 自带) ===== */
        mgr.registerIntrinsic(WeaponType.BOW, m(RANGED_DMG, 0.05));
        mgr.registerIntrinsic(WeaponType.CROSSBOW, m(RANGED_DMG, 0.08, ARMOR_PIERCE, 0.04));
        mgr.registerIntrinsic(WeaponType.TRIDENT, m(MELEE_DMG, 0.04, THROW_DMG, 0.06, ARMOR_PIERCE, 0.02));
        mgr.registerIntrinsic(WeaponType.SHIELD, m(DAMAGE_REDUCE, 0.03, KNOCKBACK_RESIST, 0.05));

        /* ===== BOW × 6 mat ===== */
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.BOW, MaterialKey.LEATHER,
                m(MOVE_SPEED, 0.05)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.BOW, MaterialKey.CHAINMAIL,
                m(CRIT_RATE, 0.05)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.BOW, MaterialKey.IRON,
                m(MOVE_SPEED, -0.01)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.BOW, MaterialKey.GOLDEN,
                m(CRIT_DMG, 0.12)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.BOW, MaterialKey.DIAMOND,
                m(RANGED_DMG, 0.06)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.BOW, MaterialKey.NETHERITE,
                m(RANGED_DMG, 0.08, BURN_DMG, 0.02)));

        /* ===== CROSSBOW × 6 mat ===== */
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.CROSSBOW, MaterialKey.LEATHER,
                m(MOVE_SPEED, 0.04)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.CROSSBOW, MaterialKey.CHAINMAIL,
                m(KNOCKBACK_RESIST, -0.05)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.CROSSBOW, MaterialKey.IRON,
                m(DOT_BLEED, 0.02)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.CROSSBOW, MaterialKey.GOLDEN,
                m(CRIT_DMG, 0.05)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.CROSSBOW, MaterialKey.DIAMOND,
                m(DAMAGE_REDUCE, 0.03)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.CROSSBOW, MaterialKey.NETHERITE,
                m(BURN_DMG, 0.02, RANGED_DMG, 0.04)));

        /* ===== TRIDENT × 6 mat ===== */
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.TRIDENT, MaterialKey.LEATHER,
                m(MOVE_SPEED, 0.05, THROW_DMG, 0.02)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.TRIDENT, MaterialKey.CHAINMAIL,
                m(MOVE_SPEED, -0.02)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.TRIDENT, MaterialKey.IRON,
                m(THROW_DMG, 0.06)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.TRIDENT, MaterialKey.GOLDEN,
                m(CRIT_DMG, 0.15)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.TRIDENT, MaterialKey.DIAMOND,
                m(DAMAGE_REDUCE, 0.04)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.TRIDENT, MaterialKey.NETHERITE,
                m(BURN_DMG, 0.03, MELEE_DMG, 0.10, THROW_DMG, 0.10)));

        /* ===== SHIELD × 6 mat ===== */
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.SHIELD, MaterialKey.LEATHER,
                m(MOVE_SPEED, 0.02)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.SHIELD, MaterialKey.CHAINMAIL,
                m(MOVE_SPEED, 0.0)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.SHIELD, MaterialKey.IRON,
                m(MELEE_DMG, 0.07)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.SHIELD, MaterialKey.GOLDEN,
                m(CRIT_RATE, 0.06)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.SHIELD, MaterialKey.DIAMOND,
                m(DAMAGE_REDUCE, 0.08)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.SHIELD, MaterialKey.NETHERITE,
                m(DAMAGE_REDUCE, 0.05, BURN_DMG, 0.02)));

        /* ===== MACE × 6 mat ===== */
        mgr.register(new WeaponInteractEntry(WeaponType.MACE, MaterialKey.LEATHER, m(),
                (p, t) -> t.knockback(0.35f, p.getX() - t.getX(), p.getZ() - t.getZ()),
                (p, t) -> {}, (p, t) -> {}, (p, f) -> {}));
        mgr.register(new WeaponInteractEntry(WeaponType.MACE, MaterialKey.CHAINMAIL, m(),
                (p, t) -> p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                        40, 0, false, false, true)),
                (p, t) -> {}, (p, t) -> {}, (p, f) -> {}));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.MACE, MaterialKey.IRON, m()));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.MACE, MaterialKey.GOLDEN,
                m(CRIT_DMG, 0.18)));
        mgr.register(WeaponInteractEntry.statOnly(WeaponType.MACE, MaterialKey.DIAMOND, m()));
        mgr.register(new WeaponInteractEntry(WeaponType.MACE, MaterialKey.NETHERITE, m(),
                (p, t) -> MaceInteractEffects.applyArmorBreak(t, 80),
                (p, t) -> {}, MaceInteractEffects::applyShockwave, (p, f) -> {}));

        MaceInteractEffects.register();
    }
}
