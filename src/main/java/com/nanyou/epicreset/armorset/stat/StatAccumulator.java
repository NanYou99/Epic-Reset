package com.nanyou.epicreset.armorset.stat;

import com.google.common.collect.ImmutableMap;

import java.util.EnumMap;
import java.util.Map;

public final class StatAccumulator {

    private final EnumMap<StatCategory, Double> values = new EnumMap<>(StatCategory.class);

    public void add(StatCategory cat, double pct) {
        if (pct == 0d) return;
        values.merge(cat, pct, Double::sum);
    }

    public StatAccumulator merge(Map<StatCategory, Double> other) {
        if (other == null) return this;
        for (var e : other.entrySet()) {
            add(e.getKey(), e.getValue());
        }
        return this;
    }

    public double getRawSum(StatCategory cat) {
        return values.getOrDefault(cat, 0d);
    }

    public boolean isEmpty() { return values.isEmpty(); }

    public ImmutableMap<StatCategory, Double> mulReduce() {
        ImmutableMap.Builder<StatCategory, Double> out = ImmutableMap.builder();
        for (var entry : values.entrySet()) {
            StatCategory cat = entry.getKey();
            double rawSum = entry.getValue();
            
            if (cat == StatCategory.MAX_HEALTH) {
                // 修复：生命值固定数值加法，比如 +6 就是真加 6 点血
                out.put(cat, Math.max(0d, rawSum));
            } else if (cat.isReduceCategory()) {
                double reduced = 1d - reduceMulComplement(1d, rawSum);
                out.put(cat, clampReduce(reduced));
            } else if (cat.isRateCategory()) {
                double rate = reduceMulRate(0d, rawSum);
                out.put(cat, clamp01(rate));
            } else {
                double factor = 1d + rawSum;
                out.put(cat, Math.max(0d, factor));
            }
        }
        return out.build();
    }

    private static double reduceMulComplement(double startOne, double pctSum) {
        double mul = 1d;
        int terms = estimateTerms(pctSum);
        double per = pctSum / terms;
        for (int i = 0; i < terms; i++) mul *= (1d - per);
        return mul;
    }

    private static double reduceMulRate(double startZero, double pctSum) {
        double mul = 1d;
        int terms = estimateTerms(pctSum);
        double per = pctSum / terms;
        for (int i = 0; i < terms; i++) mul *= (1d - per);
        return 1d - mul;
    }

    private static int estimateTerms(double pctSumAbs) {
        double approx = Math.max(1, (int) Math.round(Math.abs(pctSumAbs) / 0.05 + 0.5));
        return (int) Math.min(20, Math.max(1, approx));
    }

    private static double clampReduce(double v) { return Math.max(0d, Math.min(0.95d, v)); }
    private static double clamp01(double v) { return Math.max(0d, Math.min(0.999d, v)); }
}