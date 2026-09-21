package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;

import static com.nanyou.epicreset.armorset.set.SetStatHelper.of;

public final class GoldenArmorSet {

    public static final String ID = "golden";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET, Items.GOLDEN_HELMET, "armorset.golden.helmet",
                        of(StatCategory.CRIT_RATE, 0.03))
                .piece(ArmorItem.Type.CHESTPLATE, Items.GOLDEN_CHESTPLATE, "armorset.golden.chestplate",
                        of(StatCategory.CRIT_DMG, 0.04))
                .piece(ArmorItem.Type.LEGGINGS, Items.GOLDEN_LEGGINGS, "armorset.golden.leggings",
                        of(StatCategory.CRIT_RATE, 0.02))
                .piece(ArmorItem.Type.BOOTS, Items.GOLDEN_BOOTS, "armorset.golden.boots",
                        of(StatCategory.ARMOR_PIERCE, 0.02))
                .twoPiece("armorset.golden.two", (p, c) -> {
                        }, of(
                                StatCategory.CRIT_RATE, 0.10,
                                StatCategory.CRIT_DMG, 0.15
                        ))
                .fourPiece("armorset.golden.four", (p, c) -> {
                        }, of(
                                StatCategory.CRIT_RATE, 0.14,
                                StatCategory.CRIT_DMG, 0.25,
                                StatCategory.ARMOR_PIERCE, 0.08
                        ))
                .build();
    }
}
