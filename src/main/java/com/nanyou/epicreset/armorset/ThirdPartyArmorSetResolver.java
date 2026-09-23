package com.nanyou.epicreset.armorset;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ThirdPartyArmorSetResolver {

    private static final String MOD_ID = "epic-reset";
    public static final String IDENTIFY_NBT_KEY = "epicreset_set_identified";
    private static final Map<String, TierInfo> TIER_CACHE = new ConcurrentHashMap<>();

    private static final List<StatCategory> CORE_POOL = List.of(
            StatCategory.MELEE_DMG,
            StatCategory.RANGED_DMG,
            StatCategory.CRIT_RATE,
            StatCategory.MAX_HEALTH,
            StatCategory.CRIT_DMG,
            StatCategory.ATTACK_SPEED,
            StatCategory.LIFE_STEAL,
            StatCategory.ARMOR
    );

    private static final double[] STYLE_WARRIOR   = {3.0, 0.3, 1.8, 1.5, 2.5, 1.2, 1.0, 1.8};
    private static final double[] STYLE_KNIGHT    = {2.5, 0.3, 1.2, 3.0, 1.8, 1.0, 1.5, 2.8};
    private static final double[] STYLE_MAGE      = {0.3, 3.0, 1.5, 1.0, 2.5, 1.5, 1.0, 0.5};
    private static final double[] STYLE_PRIEST    = {0.3, 2.5, 1.0, 2.5, 1.2, 1.5, 2.5, 1.2};
    private static final double[] STYLE_WARLOCK   = {0.3, 2.8, 1.5, 1.0, 2.0, 1.2, 1.0, 0.5};
    private static final double[] STYLE_ARCHER    = {0.3, 3.0, 2.0, 1.0, 1.5, 2.5, 1.0, 0.5};
    private static final double[] STYLE_RANGER    = {0.5, 2.8, 1.5, 1.2, 1.5, 1.8, 2.5, 0.7};
    private static final double[] STYLE_ASSASSIN  = {1.5, 1.0, 3.0, 1.0, 3.0, 1.8, 2.8, 0.5};
    private static final double[] STYLE_TANK      = {1.0, 1.0, 1.0, 3.0, 1.0, 1.0, 1.5, 3.0};
    private static final double[] STYLE_GUARDIAN  = {1.0, 1.0, 1.0, 3.0, 1.0, 1.0, 2.5, 2.8};
    private static final double[] STYLE_BALANCED  = {1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5};

    private static final String[] WARRIOR_KW  = {"warrior", "berserker", "fighter", "champion",
            "destroyer", "onslaught", "slayer", "dark_lord", "golden_horns", "战斗", "战士", "角盔"};
    private static final String[] KNIGHT_KW   = {"knight", "justicar", "soldier", "paladin_warrior", "骑士", "审判官"};
    private static final String[] MAGE_KW     = {"wizard", "mage", "sorcerer", "arcane", "magic", "spell",
            "frozen", "frost", "fire_robe", "tempest", "storm", "法师", "巫师", "奥秘", "魔法"};
    private static final String[] PRIEST_KW   = {"priest", "prior", "holy", "absolution", "牧师", "修院长", "神圣", "赦罪"};
    private static final String[] WARLOCK_KW  = {"warlock", "witch", "dark_magic", "void_robe", "术士", "女巫"};
    private static final String[] ARCHER_KW   = {"archer", "bow", "crossbow", "sharpshooter", "弓箭", "射手"};
    private static final String[] RANGER_KW   = {"ranger", "hunter", "stalker", "bounty", "polar", "游侠", "猎人", "裂隙猎手"};
    private static final String[] ASSASSIN_KW = {"assassin", "rogue", "shadow", "abyssal", "deathmantle",
            "riftstalker", "刺客", "死神", "暗影"};
    private static final String[] TANK_KW     = {"guardian", "fortress", "divine", "lightbringer", "守护", "圣骑", "神性"};
    private static final String[] GUARDIAN_KW = {"guard", "avatar", "holy_guard", "守卫", "神性长袍"};

    private ThirdPartyArmorSetResolver() {}

    public static boolean requiresIdentification(ThirdPartyArmorTier tier) {
        return tier == ThirdPartyArmorTier.EPIC || tier == ThirdPartyArmorTier.MYTHIC;
    }

    public static boolean isPieceIdentified(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var tag = stack.getTag();
        if (tag == null) return false;
        return tag.getBoolean(IDENTIFY_NBT_KEY);
    }

    public static void markPieceIdentified(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        stack.getOrCreateTag().putBoolean(IDENTIFY_NBT_KEY, true);
    }

    public static Optional<SetInfo> getSetInfo(ItemStack stack, Player player) {
        if (player == null) return Optional.empty();
        EquipmentSlot slot = armorSlotFor(stack);
        if (slot == null) return Optional.empty();

        ArmorSetManager manager = ArmorSetManager.getInstance();
        Optional<String> groupKey = resolveGroupKey(stack, slot, manager);
        if (groupKey.isEmpty()) return Optional.empty();

        int equipped = 0;
        for (EquipmentSlot armorSlot : ArmorSetManager.getArmorSlots()) {
            if (groupKey.equals(resolveGroupKey(player.getItemBySlot(armorSlot), armorSlot, manager))) {
                equipped++;
            }
        }
        TierInfo tierInfo = getTierInfo(groupKey.get(), manager);
        return Optional.of(new SetInfo(groupKey.get(), equipped, tierInfo.tier(), tierInfo.armorTotal(), tierInfo.totalPieces(), tierInfo.twoPieceMods(), tierInfo.fourPieceMods(), tierInfo.slotPieces()));
    }

    public static Map<StatCategory, Double> collectSetBonusMods(Player player, ArmorSetManager manager) {
        Map<String, Integer> groupCounts = new HashMap<>(4);
        for (EquipmentSlot slot : ArmorSetManager.getArmorSlots()) {
            ItemStack stack = player.getItemBySlot(slot);
            Optional<String> groupKey = resolveGroupKey(stack, slot, manager);
            groupKey.ifPresent(key -> groupCounts.merge(key, 1, Integer::sum));
        }

        EnumMap<StatCategory, Double> result = new EnumMap<>(StatCategory.class);
        for (Map.Entry<String, Integer> entry : groupCounts.entrySet()) {
            TierInfo tierInfo = getTierInfo(entry.getKey(), manager);
            if (entry.getValue() >= 2) mergeInto(result, tierInfo.twoPieceMods());
            if (entry.getValue() >= 4) mergeInto(result, tierInfo.fourPieceMods());
        }
        return result;
    }

    private static boolean isArmorPart(String path) {
        return path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots")
            || path.endsWith("_vest") || path.endsWith("_wings") || path.endsWith("_goggles")
            || path.endsWith("_head") || path.endsWith("_chest") || path.endsWith("_legs") || path.endsWith("_feet");
    }

    private static boolean isArmorItem(Item item) { return item instanceof ArmorItem; }

    private static boolean hasNetheriteTraits(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) return false;
        try {
            var material = armorItem.getMaterial();
            if (material != null && material.getToughness() >= 3.0f && material.getKnockbackResistance() > 0.0f) return true;
        } catch (Throwable ignored) {}
        return false;
    }

    private static ThirdPartyArmorTier determineTier(String fullGroupKey, Map<EquipmentSlot, Item> pieces, double armorTotal) {
        String lower = fullGroupKey.toLowerCase();
        if (lower.contains("nether") || lower.contains("dragon") || lower.contains("mythic")
            || lower.contains("divine") || lower.contains("fiery")) return ThirdPartyArmorTier.MYTHIC;
        for (Item item : pieces.values()) {
            if (hasNetheriteTraits(item.getDefaultInstance())) return ThirdPartyArmorTier.MYTHIC;
        }
        if (lower.contains("diamond") || lower.contains("emerald") || lower.contains("ruby")
            || lower.contains("amethyst") || lower.contains("crystal") || lower.contains("epic")) return ThirdPartyArmorTier.EPIC;
        if (lower.contains("iron") || lower.contains("gold") || lower.contains("steel")
            || lower.contains("copper") || lower.contains("obsidian") || lower.contains("silver")
            || lower.contains("heavy")) return ThirdPartyArmorTier.LEGENDARY;
        if (armorTotal >= 20) return ThirdPartyArmorTier.MYTHIC;
        if (armorTotal >= 15) return ThirdPartyArmorTier.EPIC;
        if (armorTotal >= 12) return ThirdPartyArmorTier.LEGENDARY;
        return ThirdPartyArmorTier.RARE;
    }

    public static TierInfo getTierInfo(String groupKey, ArmorSetManager manager) {
        return TIER_CACHE.computeIfAbsent(groupKey, key -> {
            int namespaceIndex = key.indexOf(':');
            String namespace = "minecraft";
            String pathPrefix = key;
            if (namespaceIndex != -1) {
                namespace = key.substring(0, namespaceIndex);
                pathPrefix = key.substring(namespaceIndex + 1);
            }
            boolean isVanilla = "minecraft".equals(namespace);

            Map<EquipmentSlot, Item> pieces = new EnumMap<>(EquipmentSlot.class);
            double armorTotal = 0;

            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                if (id == null || !id.getNamespace().equals(namespace)) continue;
                String path = id.getPath();
                if (!isArmorPart(path) || !isArmorItem(item)) continue;
                if (!path.equals(pathPrefix) && !path.startsWith(pathPrefix + "_")) continue;

                EquipmentSlot slot = armorSlotFor(item.getDefaultInstance());
                if (slot == null) continue;
                pieces.putIfAbsent(slot, item);
                armorTotal += getArmorValue(item.getDefaultInstance());
            }

            ThirdPartyArmorTier tier = determineTier(key, pieces, armorTotal);
            return createTierInfo(tier, key, isVanilla, pieces.size(), pieces);
        });
    }

    private static TierInfo createTierInfo(ThirdPartyArmorTier tier, String groupKey, boolean isVanilla, int totalPieces, Map<EquipmentSlot, Item> slotPieces) {
        int twoPieceCount, fourPieceCount;
        double healthCap;
        double armorCap;
        double spikeMultiplier;

        switch (tier) {
            case RARE -> {
                twoPieceCount = 2; fourPieceCount = 2;
                healthCap = isVanilla ? 6 : 8;
                armorCap = isVanilla ? 2 : 3;
                spikeMultiplier = isVanilla ? 1.10 : 1.20;
            }
            case LEGENDARY -> {
                twoPieceCount = 3; fourPieceCount = 3;
                healthCap = isVanilla ? 9 : 12;
                armorCap = isVanilla ? 3 : 4;
                spikeMultiplier = isVanilla ? 1.12 : 1.22;
            }
            case EPIC -> {
                twoPieceCount = 3; fourPieceCount = 4;
                healthCap = isVanilla ? 12 : 16;
                armorCap = isVanilla ? 4 : 6;
                spikeMultiplier = isVanilla ? 1.15 : 1.28;
            }
            case MYTHIC -> {
                twoPieceCount = 4; fourPieceCount = 3;
                healthCap = isVanilla ? 15 : 20;
                armorCap = isVanilla ? 5 : 8;
                spikeMultiplier = isVanilla ? 1.15 : 1.35;
            }
            default -> {
                twoPieceCount = 3; fourPieceCount = 3;
                healthCap = 10; armorCap = 4; spikeMultiplier = 1.15;
            }
        }

        String lowerKey = groupKey.toLowerCase();
        double[] weights = pickStyle(lowerKey).clone();

        Random rng = new Random(groupKey.hashCode());
        for (int i = 0; i < weights.length; i++) {
            weights[i] *= (0.85 + rng.nextDouble() * 0.30);
        }

        record IndexedWeight(int idx, double weight) {}
        List<IndexedWeight> indexed = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            indexed.add(new IndexedWeight(i, weights[i]));
        }
        indexed.sort((a, b) -> Double.compare(b.weight(), a.weight()));
        Set<Integer> spikeIndices = new HashSet<>();
        spikeIndices.add(indexed.get(0).idx());
        if (indexed.size() > 1) spikeIndices.add(indexed.get(1).idx());

        double maxWeight = indexed.get(0).weight();

        record Entry(StatCategory cat, double value, boolean isSpike) {}
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < CORE_POOL.size(); i++) {
            StatCategory cat = CORE_POOL.get(i);
            double fillRatio = Math.pow(weights[i] / maxWeight, 0.7);
            fillRatio = 0.3 + fillRatio * 0.7;

            boolean isSpike = spikeIndices.contains(i);
            if (isSpike) fillRatio = Math.min(1.0, fillRatio * spikeMultiplier);

            double value;
            if (cat == StatCategory.MAX_HEALTH) {
                value = Math.max(1, Math.round(healthCap * fillRatio));
            } else if (cat == StatCategory.ARMOR) {
                value = Math.max(1, Math.round(armorCap * fillRatio));
            } else if (cat == StatCategory.ATTACK_SPEED) {
                double cap = (isVanilla ? 0.15 : 0.30) * getTierMultiplier(tier);
                value = Math.round(cap * fillRatio * 10000d) / 10000d;
            } else {
                double cap = (isVanilla ? 0.045 : 0.075) * getTierMultiplier(tier);
                value = Math.round(cap * fillRatio * 10000d) / 10000d;
            }
            entries.add(new Entry(cat, value, isSpike));
        }

        entries.sort((a, b) -> {
            if (a.isSpike() != b.isSpike()) return a.isSpike() ? -1 : 1;
            return Double.compare(b.value(), a.value());
        });

        Map<StatCategory, Double> twoPieceMods = new EnumMap<>(StatCategory.class);
        Map<StatCategory, Double> fourPieceMods = new EnumMap<>(StatCategory.class);

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (i % 2 == 0) {
                if (twoPieceMods.size() < twoPieceCount) twoPieceMods.put(e.cat(), e.value());
            } else {
                if (fourPieceMods.size() < fourPieceCount) fourPieceMods.put(e.cat(), e.value());
            }
            if (twoPieceMods.size() >= twoPieceCount && fourPieceMods.size() >= fourPieceCount) break;
        }

        for (Entry e : entries) {
            if (twoPieceMods.size() >= twoPieceCount && fourPieceMods.size() >= fourPieceCount) break;
            if (!twoPieceMods.containsKey(e.cat()) && !fourPieceMods.containsKey(e.cat())) {
                if (twoPieceMods.size() < twoPieceCount) twoPieceMods.put(e.cat(), e.value());
                else if (fourPieceMods.size() < fourPieceCount) fourPieceMods.put(e.cat(), e.value());
            }
        }

        return new TierInfo(tier, 0, totalPieces, twoPieceMods, fourPieceMods, slotPieces);
    }

    private static double getTierMultiplier(ThirdPartyArmorTier tier) {
        return switch (tier) {
            case RARE -> 0.5;
            case LEGENDARY -> 0.7;
            case EPIC -> 0.85;
            case MYTHIC -> 1.0;
        };
    }

    private static double[] pickStyle(String lowerKey) {
        String cleaned = lowerKey.replace("netherite_", "").replace("nether_", "");
        int warrior = countMatches(cleaned, WARRIOR_KW);
        int knight = countMatches(cleaned, KNIGHT_KW);
        int mage = countMatches(cleaned, MAGE_KW);
        int priest = countMatches(cleaned, PRIEST_KW);
        int warlock = countMatches(cleaned, WARLOCK_KW);
        int archer = countMatches(cleaned, ARCHER_KW);
        int ranger = countMatches(cleaned, RANGER_KW);
        int assassin = countMatches(cleaned, ASSASSIN_KW);
        int tank = countMatches(cleaned, TANK_KW);
        int guardian = countMatches(cleaned, GUARDIAN_KW);

        int max = 0;
        for (int c : new int[]{warrior, knight, mage, priest, warlock, archer, ranger, assassin, tank, guardian}) {
            if (c > max) max = c;
        }
        if (max == 0) return STYLE_BALANCED;

        if (priest == max) return STYLE_PRIEST;
        if (warlock == max) return STYLE_WARLOCK;
        if (mage == max) return STYLE_MAGE;
        if (archer == max) return STYLE_ARCHER;
        if (ranger == max) return STYLE_RANGER;
        if (assassin == max) return STYLE_ASSASSIN;
        if (knight == max) return STYLE_KNIGHT;
        if (warrior == max) return STYLE_WARRIOR;
        if (guardian == max) return STYLE_GUARDIAN;
        return STYLE_TANK;
    }

    private static int countMatches(String text, String[] keywords) {
        int count = 0;
        for (String k : keywords) {
            if (text.contains(k.toLowerCase())) count++;
        }
        return count;
    }

    public static double getArmorValue(ItemStack stack) {
        double[] armor = {0d};
        if (stack.getItem() instanceof ArmorItem armorItem) {
            EquipmentSlot slot = armorSlotFor(stack);
            if (slot != null) {
                var defaultModifiers = armorItem.getDefaultAttributeModifiers(slot);
                for (var entry : defaultModifiers.entries()) {
                    try {
                        var key = BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey());
                        if (key == null) continue;
                        String keyStr = key.toString();
                        if (keyStr.contains("armor") && !keyStr.contains("toughness")
                                && entry.getValue().getOperation() == AttributeModifier.Operation.ADDITION) {
                            armor[0] += entry.getValue().getAmount();
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }
        return armor[0];
    }

    private static Optional<String> resolveGroupKey(ItemStack stack, EquipmentSlot wornSlot, ArmorSetManager manager) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        Item item = stack.getItem();
        if (manager.getSetByItem(item) != null) return Optional.empty();

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || MOD_ID.equals(id.getNamespace())) return Optional.empty();

        String path = id.getPath();
        Optional<String> setName = stripArmorSuffix(path, wornSlot);
        if (setName.isEmpty()) return Optional.empty();

        return Optional.of(id.getNamespace() + ":" + setName.get());
    }

    private static EquipmentSlot armorSlotFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (EquipmentSlot slot : ArmorSetManager.getArmorSlots()) {
            if (stripArmorSuffix(path, slot).isPresent()) return slot;
        }
        return null;
    }

    private static Optional<String> stripArmorSuffix(String path, EquipmentSlot slot) {
        for (String suffix : suffixesFor(slot)) {
            if (path.endsWith(suffix) && path.length() > suffix.length()) {
                return Optional.of(path.substring(0, path.length() - suffix.length()));
            }
        }
        return Optional.empty();
    }

    private static String[] suffixesFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> new String[]{"_helmet", "_headpiece", "_hat", "_hood", "_goggles", "_helm", "_crown", "_mask", "_head"};
            case CHEST -> new String[]{"_chestplate", "_chestpiece", "_tunic", "_robe", "_vest", "_jacket", "_coat", "_chest", "_body"};
            case LEGS -> new String[]{"_leggings", "_legplates", "_pants", "_wings", "_greaves", "_cuisses", "_legs", "_leg"};
            case FEET -> new String[]{"_boots", "_shoes", "_sabatons", "_slippers", "_feet", "_foot"};
            default -> new String[0];
        };
    }

    private static void mergeInto(Map<StatCategory, Double> target, Map<StatCategory, Double> mods) {
        for (Map.Entry<StatCategory, Double> entry : mods.entrySet()) {
            target.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
    }

    public record TierInfo(ThirdPartyArmorTier tier, double armorTotal, int totalPieces, Map<StatCategory, Double> twoPieceMods, Map<StatCategory, Double> fourPieceMods, Map<EquipmentSlot, Item> slotPieces) {
    }

    public record SetInfo(String groupKey, int equipped, ThirdPartyArmorTier tier, double armorTotal, int totalPieces, Map<StatCategory, Double> twoPieceMods, Map<StatCategory, Double> fourPieceMods, Map<EquipmentSlot, Item> slotPieces) {
    }
}