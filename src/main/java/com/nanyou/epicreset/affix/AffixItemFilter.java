package com.nanyou.epicreset.affix;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

final class AffixItemFilter {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("epic-reset-affix-items.json");
    private static final Overrides OVERRIDES = loadOverrides();

    private AffixItemFilter() {
    }

    static boolean isArmorWhitelisted(Item item) {
        return OVERRIDES.armorWhitelist().contains(BuiltInRegistries.ITEM.getKey(item));
    }

    static boolean isWeaponWhitelisted(Item item) {
        return OVERRIDES.weaponWhitelist().contains(BuiltInRegistries.ITEM.getKey(item));
    }

    static boolean isBlacklisted(Item item) {
        return OVERRIDES.blacklist().contains(BuiltInRegistries.ITEM.getKey(item));
    }

    private static Overrides loadOverrides() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                    writer.write("""
                            {
                              "armorWhitelist": [],
                              "weaponWhitelist": [],
                              "blacklist": []
                            }
                            """);
                }
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                return new Overrides(
                        readIds(root, "armorWhitelist"),
                        readIds(root, "weaponWhitelist"),
                        readIds(root, "blacklist"));
            }
        } catch (IOException | IllegalStateException ignored) {
            return new Overrides(Set.of(), Set.of(), Set.of());
        }
    }

    private static Set<ResourceLocation> readIds(JsonObject root, String key) {
        Set<ResourceLocation> ids = new HashSet<>();
        if (!root.has(key) || !root.get(key).isJsonArray()) return Set.of();
        JsonArray entries = root.getAsJsonArray(key);
        for (JsonElement entry : entries) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) continue;
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            if (id != null) ids.add(id);
        }
        return Set.copyOf(ids);
    }

    private record Overrides(
            Set<ResourceLocation> armorWhitelist,
            Set<ResourceLocation> weaponWhitelist,
            Set<ResourceLocation> blacklist) {
    }
}
