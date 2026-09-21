package com.nanyou.epicreset.affix;

import com.nanyou.epicreset.armorset.stat.StatCategory;

import java.util.Map;

public record AffixDefinition(
        String id,
        String translationKey,
        double minValue,
        double maxValue,
        Map<StatCategory, Double> statWeights
) {
}
