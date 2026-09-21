package com.nanyou.epicreset.armorset.set;

import com.nanyou.epicreset.armorset.stat.StatCategory;

import java.util.EnumMap;
import java.util.Map;

final class SetStatHelper {
    private SetStatHelper() {}

    static Map<StatCategory, Double> of(Object... kvs) {
        EnumMap<StatCategory, Double> m = new EnumMap<>(StatCategory.class);
        for (int i = 0; i < kvs.length; i += 2) {
            m.put((StatCategory) kvs[i], (Double) kvs[i + 1]);
        }
        return m;
    }
}
