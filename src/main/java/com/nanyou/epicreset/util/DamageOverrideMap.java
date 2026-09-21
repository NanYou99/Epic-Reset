package com.nanyou.epicreset.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.LivingEntity;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;

public final class DamageOverrideMap {

    private static final ConcurrentLinkedQueue<WeakEntry> OVERRIDE_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<WeakReference<LivingEntity>, Float> OVERRIDES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<WeakReference<LivingEntity>, Float> ARMORED = new ConcurrentHashMap<>();

    private DamageOverrideMap() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            OVERRIDES.clear();
            ARMORED.clear();
            Iterator<WeakEntry> it = OVERRIDE_QUEUE.iterator();
            while (it.hasNext()) {
                WeakEntry e = it.next();
                if (e.ref.get() == null) it.remove();
            }
        });
    }

    private static WeakReference<LivingEntity> keyOf(LivingEntity entity) {
        for (var ref : OVERRIDES.keySet()) {
            if (entity == ref.get()) return ref;
        }
        for (var ref : ARMORED.keySet()) {
            if (entity == ref.get()) return ref;
        }
        WeakReference<LivingEntity> ref = new WeakReference<>(entity);
        OVERRIDE_QUEUE.add(new WeakEntry(ref));
        return ref;
    }

    public static void setOverride(LivingEntity entity, float amount) {
        OVERRIDES.put(keyOf(entity), amount);
    }

    public static Float consumeOverride(LivingEntity entity) {
        Iterator<Map.Entry<WeakReference<LivingEntity>, Float>> it = OVERRIDES.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            LivingEntity v = e.getKey().get();
            if (v == null) { it.remove(); continue; }
            if (v == entity) {
                Float val = e.getValue();
                it.remove();
                return val;
            }
        }
        return null;
    }

    public static void setArmored(LivingEntity entity, float amount) {
        ARMORED.put(keyOf(entity), amount);
    }

    public static Float consumeArmored(LivingEntity entity) {
        Iterator<Map.Entry<WeakReference<LivingEntity>, Float>> it = ARMORED.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            LivingEntity v = e.getKey().get();
            if (v == null) { it.remove(); continue; }
            if (v == entity) {
                Float val = e.getValue();
                it.remove();
                return val;
            }
        }
        return null;
    }

    private static final class WeakEntry {
        final WeakReference<LivingEntity> ref;
        WeakEntry(WeakReference<LivingEntity> r) { this.ref = r; }
    }
}
