package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;

import static com.nanyou.epicreset.armorset.set.SetStatHelper.of;

public final class IronArmorSet {

    public static final String ID = "iron";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET, Items.IRON_HELMET, "armorset.iron.helmet",
                        of(StatCategory.CRIT_DMG, 0.03))
                .piece(ArmorItem.Type.CHESTPLATE, Items.IRON_CHESTPLATE, "armorset.iron.chestplate",
                        of(StatCategory.DAMAGE_REDUCE, 0.03))
                .piece(ArmorItem.Type.LEGGINGS, Items.IRON_LEGGINGS, "armorset.iron.leggings",
                        of(StatCategory.MELEE_DMG, 0.03))
                .piece(ArmorItem.Type.BOOTS, Items.IRON_BOOTS, "armorset.iron.boots",
                        of(StatCategory.LIFE_STEAL, 0.01))
                .twoPiece("armorset.iron.two", (p, c) -> {
                        }, of(
                                StatCategory.MELEE_DMG, 0.10,
                                StatCategory.DAMAGE_REDUCE, 0.07
                        ))
                .fourPiece("armorset.iron.four", (p, c) -> {
                        }, of(
                                StatCategory.MELEE_DMG, 0.15,
                                StatCategory.DAMAGE_REDUCE, 0.10,
                                StatCategory.CRIT_DMG, 0.12,
                                StatCategory.LIFE_STEAL, 0.03
                        ))
                .build();
    }
}
