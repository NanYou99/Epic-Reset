package com.nanyou.epicreset.armorset;

import net.minecraft.network.chat.TextColor;

public enum ThirdPartyArmorTier {
    // 护甲值 < 12 -> 稀有（蓝色）
    RARE(11, TextColor.fromRgb(0x5555FF), 2, 1.0d),
    
    // 护甲值 12 ~ 14 -> 传说（黄绿色）
    LEGENDARY(14, TextColor.fromRgb(0xAAFF55), 3, 1.6d),
    
    // 护甲值 15 ~ 19 -> 史诗（紫红色）
    EPIC(19, TextColor.fromRgb(0xFF55FF), 4, 2.2d),
    
    // 护甲值 >= 20 -> 神话（炽热金红色）
    MYTHIC(Integer.MAX_VALUE, TextColor.fromRgb(0xFF4400), 5, 3.0d);

    private final int maximumArmor;
    private final TextColor color;
    private final int wordCount;
    private final double multiplier;

    ThirdPartyArmorTier(int maximumArmor, TextColor color, int wordCount, double multiplier) {
        this.maximumArmor = maximumArmor;
        this.color = color;
        this.wordCount = wordCount;
        this.multiplier = multiplier;
    }

    public TextColor getColor() { return color; }
    public int getWordCount() { return wordCount; }
    public double getMultiplier() { return multiplier; }

    public String getTranslationKey() {
        return "armorset.tooltip.third_party.tier." + name().toLowerCase();
    }

    /**
     * 品阶升级：稀有→传说→史诗→神话（封顶在神话，不越界）
     */
    public ThirdPartyArmorTier next() {
        int nextOrdinal = Math.min(ordinal() + 1, MYTHIC.ordinal());
        return values()[nextOrdinal];
    }

    public static ThirdPartyArmorTier fromArmorTotal(double armorTotal) {
        for (ThirdPartyArmorTier tier : values()) {
            if (armorTotal <= tier.maximumArmor) return tier;
        }
        return MYTHIC;
    }
}