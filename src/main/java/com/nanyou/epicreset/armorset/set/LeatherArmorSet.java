package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;

public final class LeatherArmorSet {

    public static final String ID = "leather";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET, Items.LEATHER_HELMET, "armorset.leather.helmet",
                        of(StatCategory.DAMAGE_REDUCE, 0.01))
                .piece(ArmorItem.Type.CHESTPLATE, Items.LEATHER_CHESTPLATE, "armorset.leather.chestplate",
                        of(StatCategory.DAMAGE_REDUCE, 0.02))
                .piece(ArmorItem.Type.LEGGINGS, Items.LEATHER_LEGGINGS, "armorset.leather.leggings",
                        of(StatCategory.CRIT_RATE, 0.01))
                .piece(ArmorItem.Type.BOOTS, Items.LEATHER_BOOTS, "armorset.leather.boots")
                .twoPiece("armorset.leather.two", (player, count) -> {
                }, of(StatCategory.DAMAGE_REDUCE, 0.05))
                .fourPiece("armorset.leather.four", (player, count) -> {
                }, of(
                        StatCategory.DAMAGE_REDUCE, 0.08,
                        StatCategory.CRIT_RATE, 0.04,
                        StatCategory.LIFE_STEAL, 0.02
                ))
                .build();
    }

    private static Map<StatCategory, Double> of(StatCategory k1, double v1) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        m.put(k1, v1);
        return m;
    }

    private static Map<StatCategory, Double> of(StatCategory k1, double v1,
                                                StatCategory k2, double v2) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        m.put(k1, v1); m.put(k2, v2);
        return m;
    }

    private static Map<StatCategory, Double> of(StatCategory k1, double v1,
                                                StatCategory k2, double v2,
                                                StatCategory k3, double v3) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        m.put(k1, v1); m.put(k2, v2); m.put(k3, v3);
        return m;
    }

    static Map<StatCategory, Double> of(Object... kvs) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        for (int i = 0; i < kvs.length; i += 2) {
            m.put((StatCategory) kvs[i], (Double) kvs[i + 1]);
        }
        return m;
    }
}
