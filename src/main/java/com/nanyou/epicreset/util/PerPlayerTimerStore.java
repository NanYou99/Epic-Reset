package com.nanyou.epicreset.util;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PerPlayerTimerStore {

    private static final ConcurrentHashMap<UUID, Object2LongMap<String>> LAST_TICKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ObjectSet<String>> FLAGS = new ConcurrentHashMap<>();

    private PerPlayerTimerStore() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var online = new ObjectOpenHashSet<UUID>();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) online.add(p.getUUID());
            LAST_TICKS.keySet().removeIf(u -> !online.contains(u));
            FLAGS.keySet().removeIf(u -> !online.contains(u));
        });
    }

    public static boolean cooldownPassed(ServerPlayer p, String key, int ticks) {
        UUID u = p.getUUID();
        long now = p.serverLevel().getGameTime();
        Object2LongMap<String> m = LAST_TICKS.get(u);
        long last = (m == null) ? 0L : m.getLong(key);
        return now - last < ticks;
    }

    public static void writeLast(ServerPlayer p, String key) {
        UUID u = p.getUUID();
        Object2LongMap<String> m = LAST_TICKS.computeIfAbsent(u, k -> new Object2LongOpenHashMap<>());
        m.put(key, p.serverLevel().getGameTime());
    }

    public static void setFlag(ServerPlayer p, String key, boolean value) {
        UUID u = p.getUUID();
        if (value) {
            ObjectSet<String> s = FLAGS.computeIfAbsent(u, k -> new ObjectOpenHashSet<>());
            s.add(key);
        } else {
            ObjectSet<String> s = FLAGS.get(u);
            if (s != null) s.remove(key);
        }
    }

    public static boolean consumeFlag(ServerPlayer p, String key) {
        ObjectSet<String> s = FLAGS.get(p.getUUID());
        return s != null && s.remove(key);
    }
}
