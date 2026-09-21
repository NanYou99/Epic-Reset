package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;

import static com.nanyou.epicreset.armorset.set.SetStatHelper.of;

public final class DiamondArmorSet {

    public static final String ID = "diamond";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET, Items.DIAMOND_HELMET, "armorset.diamond.helmet",
                        of(StatCategory.KNOCKBACK_RESIST, 0.05))
                .piece(ArmorItem.Type.CHESTPLATE, Items.DIAMOND_CHESTPLATE, "armorset.diamond.chestplate",
                        of(StatCategory.DAMAGE_REDUCE, 0.04))
                .piece(ArmorItem.Type.LEGGINGS, Items.DIAMOND_LEGGINGS, "armorset.diamond.leggings",
                        of(StatCategory.CRIT_DMG, 0.04))
                .piece(ArmorItem.Type.BOOTS, Items.DIAMOND_BOOTS, "armorset.diamond.boots",
                        of(StatCategory.LIFE_STEAL, 0.01))
                .twoPiece("armorset.diamond.two", (p, c) -> {
                        }, of(
                                StatCategory.DAMAGE_REDUCE, 0.14,
                                StatCategory.KNOCKBACK_RESIST, 0.20
                        ))
                .fourPiece("armorset.diamond.four", (p, c) -> {
                        }, of(
                                StatCategory.DAMAGE_REDUCE, 0.20,
                                StatCategory.KNOCKBACK_RESIST, 0.30,
                                StatCategory.CRIT_DMG, 0.18,
                                StatCategory.LIFE_STEAL, 0.04
                        ))
                .build();
    }
}
