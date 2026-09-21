package com.nanyou.epicreset.weapon.resonance;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import com.nanyou.epicreset.util.PerPlayerTimerStore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;

import static com.nanyou.epicreset.armorset.stat.StatCategory.*;

public final class ResonanceRegistry {
    private ResonanceRegistry() {}

    private static Map<StatCategory, Double> m(Object... kvs) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        for (int i = 0; i < kvs.length; i += 2) m.put((StatCategory) kvs[i], (Double) kvs[i+1]);
        return m;
    }

    public static void registerAll() {
        ResonanceManager mgr = ResonanceManager.getInstance();

        mgr.register(ResonanceEntry.of(MaterialKey.LEATHER, WeaponType.SWORD,
                m(LIFE_STEAL, 0.01, ATTACK_RANGE, 0.20)));
        mgr.register(ResonanceEntry.of(MaterialKey.LEATHER, WeaponType.AXE,
                m(MELEE_DMG, 0.06)));

        mgr.register(ResonanceEntry.of(MaterialKey.CHAINMAIL, WeaponType.SWORD,
                m(ATTACK_SPEED, 0.03, MOVE_SPEED, 0.03)));
        mgr.register(ResonanceEntry.of(MaterialKey.CHAINMAIL, WeaponType.AXE,
                m(MELEE_DMG, 0.0)));

        mgr.register(ResonanceEntry.of(MaterialKey.IRON, WeaponType.SWORD,
                m(MELEE_DMG, 0.05),
                (p, t) -> PerPlayerTimerStore.setFlag(p, "epicreset:iron_sword_bonus_sweep", true)));
        mgr.register(ResonanceEntry.of(MaterialKey.IRON, WeaponType.AXE,
                m(ARMOR_PIERCE, 0.04)));

        mgr.register(ResonanceEntry.of(MaterialKey.GOLDEN, WeaponType.SWORD,
                m(LIFE_STEAL, 0.03)));
        mgr.register(ResonanceEntry.of(MaterialKey.GOLDEN, WeaponType.AXE,
                m(DAMAGE_REDUCE, -0.10)));

        mgr.register(ResonanceEntry.of(MaterialKey.DIAMOND, WeaponType.SWORD,
                m(MELEE_DMG, 0.03),
                (p, t) -> PerPlayerTimerStore.setFlag(p, "epicreset:diamond_sword_counter_ready", true)));
        mgr.register(ResonanceEntry.of(MaterialKey.DIAMOND, WeaponType.AXE,
                m(MELEE_DMG, 0.10)));

        mgr.register(ResonanceEntry.of(MaterialKey.NETHERITE, WeaponType.SWORD,
                m(MELEE_DMG, 0.08, BURN_DMG, 0.02)));
        mgr.register(ResonanceEntry.of(MaterialKey.NETHERITE, WeaponType.AXE,
                m(BURN_DMG, 0.03, DAMAGE_REDUCE, -0.08)));
    }

    public static Map<StatCategory, Double> intrinsicSword(Item item) {
        if (item == Items.WOODEN_SWORD) return m(MELEE_DMG, 0.02, CRIT_RATE, 0.01);
        if (item == Items.STONE_SWORD) return m(MELEE_DMG, 0.03, ARMOR_PIERCE, 0.01);
        if (item == Items.IRON_SWORD) return m(MELEE_DMG, 0.04, CRIT_DMG, 0.04);
        if (item == Items.GOLDEN_SWORD) return m(CRIT_RATE, 0.04, CRIT_DMG, 0.06);
        if (item == Items.DIAMOND_SWORD) return m(MELEE_DMG, 0.05, LIFE_STEAL, 0.02);
        if (item == Items.NETHERITE_SWORD) return m(MELEE_DMG, 0.07, CRIT_RATE, 0.03, ARMOR_PIERCE, 0.03);
        return Map.of();
    }

    public static Map<StatCategory, Double> intrinsicAxe(Item item) {
        if (item == Items.WOODEN_AXE) return m(MELEE_DMG, 0.03, KNOCKBACK_POWER, 0.20);
        if (item == Items.STONE_AXE) return m(MELEE_DMG, 0.04, ARMOR_PIERCE, 0.02);
        if (item == Items.IRON_AXE) return m(MELEE_DMG, 0.06, CRIT_DMG, 0.05);
        if (item == Items.GOLDEN_AXE) return m(CRIT_RATE, 0.03, CRIT_DMG, 0.08);
        if (item == Items.DIAMOND_AXE) return m(MELEE_DMG, 0.07, KNOCKBACK_RESIST, 0.03);
        if (item == Items.NETHERITE_AXE) return m(MELEE_DMG, 0.09, ARMOR_PIERCE, 0.04);
        return Map.of();
    }
}
