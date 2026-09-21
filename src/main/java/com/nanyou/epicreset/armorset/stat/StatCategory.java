package com.nanyou.epicreset.armorset.stat;

import java.util.EnumSet;
import java.util.Set;

public enum StatCategory {

    MELEE_DMG,
    RANGED_DMG,
    THROW_DMG,
    CRIT_RATE,
    CRIT_DMG,
    ARMOR_PIERCE,
    DAMAGE_REDUCE,
    LIFE_STEAL,
    KNOCKBACK_RESIST,
    ATTACK_SPEED,
    ATTACK_RANGE,
    MOVE_SPEED,
    BURN_DMG,
    DOT_BLEED,
    KNOCKBACK_POWER,
    MAX_HEALTH,
    HEAL_POWER,
    EVASION,
    ALL_DMG;

    public static final Set<StatCategory> REDUCE_CATEGORIES = EnumSet.of(
            DAMAGE_REDUCE,
            ARMOR_PIERCE
    );

    public static final Set<StatCategory> RATE_CATEGORIES = EnumSet.of(
            CRIT_RATE,
            LIFE_STEAL,
            EVASION
    );

    public boolean isReduceCategory() { return REDUCE_CATEGORIES.contains(this); }
    public boolean isRateCategory() { return RATE_CATEGORIES.contains(this); }
}