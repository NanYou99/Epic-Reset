package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;

import static com.nanyou.epicreset.armorset.set.SetStatHelper.of;

public final class ChainmailArmorSet {

    public static final String ID = "chainmail";

    public static ArmorSet create() {
        return ArmorSet.builder(ID)
                .piece(ArmorItem.Type.HELMET, Items.CHAINMAIL_HELMET, "armorset.chainmail.helmet",
                        of(StatCategory.ATTACK_SPEED, 0.02))
                .piece(ArmorItem.Type.CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, "armorset.chainmail.chestplate",
                        of(StatCategory.KNOCKBACK_RESIST, 0.03))
                .piece(ArmorItem.Type.LEGGINGS, Items.CHAINMAIL_LEGGINGS, "armorset.chainmail.leggings",
                        of(StatCategory.ARMOR_PIERCE, 0.01))
                .piece(ArmorItem.Type.BOOTS, Items.CHAINMAIL_BOOTS, "armorset.chainmail.boots",
                        of(StatCategory.ATTACK_SPEED, 0.02))
                .twoPiece("armorset.chainmail.two", (p, c) -> {
                        }, of(
                                StatCategory.ATTACK_SPEED, 0.08,
                                StatCategory.KNOCKBACK_RESIST, 0.10
                        ))
                .fourPiece("armorset.chainmail.four", (player, count) -> {
                    if (player.tickCount % 60 == 0
                            && player.getDeltaMovement().horizontalDistanceSqr() > 0.001
                            && player.getHealth() < player.getMaxHealth() - 0.01f) {
                        player.heal(1f);
                    }
                }, of(
                        StatCategory.ATTACK_SPEED, 0.12,
                        StatCategory.CRIT_RATE, 0.06,
                        StatCategory.ARMOR_PIERCE, 0.05
                ))
                .build();
    }
}
