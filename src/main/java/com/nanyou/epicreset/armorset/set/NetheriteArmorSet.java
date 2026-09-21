package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;

import static com.nanyou.epicreset.armorset.set.SetStatHelper.of;

public final class NetheriteArmorSet {

    public static final String ID = "netherite";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET, Items.NETHERITE_HELMET, "armorset.netherite.helmet",
                        of(StatCategory.CRIT_RATE, 0.03))
                .piece(ArmorItem.Type.CHESTPLATE, Items.NETHERITE_CHESTPLATE, "armorset.netherite.chestplate",
                        of(StatCategory.DAMAGE_REDUCE, 0.05))
                .piece(ArmorItem.Type.LEGGINGS, Items.NETHERITE_LEGGINGS, "armorset.netherite.leggings",
                        of(StatCategory.MELEE_DMG, 0.04))
                .piece(ArmorItem.Type.BOOTS, Items.NETHERITE_BOOTS, "armorset.netherite.boots",
                        of(StatCategory.ARMOR_PIERCE, 0.03))
                .twoPiece("armorset.netherite.two", (p, c) -> {
                        }, of(
                                StatCategory.MELEE_DMG, 0.18,
                                StatCategory.DAMAGE_REDUCE, 0.16
                        ))
                // ⭐ 4件套：删掉了抗火 buff，只保留数值加成
                .fourPiece("armorset.netherite.four", (player, count) -> {
                    // 空 lambda，不再添加抗火效果
                }, of(
                        StatCategory.MELEE_DMG, 0.25,
                        StatCategory.DAMAGE_REDUCE, 0.24,
                        StatCategory.CRIT_RATE, 0.12,
                        StatCategory.CRIT_DMG, 0.30,
                        StatCategory.LIFE_STEAL, 0.06,
                        StatCategory.ARMOR_PIERCE, 0.12,
                        StatCategory.KNOCKBACK_RESIST, 0.35
                ))
                .build();
    }
}