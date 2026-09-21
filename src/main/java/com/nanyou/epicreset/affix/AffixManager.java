package com.nanyou.epicreset.affix;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AffixManager {

    private static final String ROOT_KEY = "epicreset";
    private static final String AFFIXES_KEY = "affixes";
    private static final String IDENTIFIED_KEY = "identified";
    private static final String ID_KEY = "id";
    private static final String VALUE_KEY = "value";

    private static final TagKey<Item> ARMOR_TAG = itemTag("armor");
    private static final TagKey<Item> HORSE_ARMOR_TAG = itemTag("horse_armor");
    private static final Map<String, AffixDefinition> ARMOR_POOL = createArmorPool();
    private static final Map<String, AffixDefinition> WEAPON_POOL = createWeaponPool();

    private enum WeaponStyle { MELEE, RANGED, BOTH }

    private static final String[] RANGED_ID_KEYWORDS = {
            "bow", "crossbow", "gun", "rifle", "pistol", "musket", "launcher",
            "wand", "staff", "scepter", "sceptre", "rod", "cannon",
            "throwing", "javelin", "sling", "blowgun", "shuriken",
            "arrow", "bolt", "bullet", "ammo", "shot"
    };

    private static final String[] MELEE_ID_KEYWORDS = {
            "sword", "blade", "katana", "dagger", "spear", "axe", "mace",
            "hammer", "club", "scythe", "halberd", "glaive", "rapier",
            "scimitar", "saber", "knife", "shovel", "pickaxe", "hoe", "whip"
    };

    private AffixManager() {
    }

    public static boolean isEligibleEquipment(ItemStack stack) {
        Item item = stack.getItem();
        if (isBlacklisted(item, stack)) return false;
        return isArmorEquipment(item, stack) || isSupportedWeapon(item, stack);
    }

    /**
     * ⭐ 支持所有近战/远程武器（含 1.21 新锤子）
     */
    public static boolean isSupportedWeapon(Item item, ItemStack stack) {
        // 原版剑/斧/镐/铲/锄
        if (AffixItemFilter.isWeaponWhitelisted(item)
                || stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)) {
            return true;
        }
        // ⭐ 弓、弩、三叉戟、锤（1.21 新增）
        if (item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof MaceItem) {
            return true;
        }
        return false;
    }

    /**
     * ⭐ 判定武器风格
     */
    private static WeaponStyle detectWeaponStyle(ItemStack stack) {
        if (stack.isEmpty()) return WeaponStyle.MELEE;
        Item item = stack.getItem();

        try {
            if (item instanceof BowItem || item instanceof CrossbowItem) return WeaponStyle.RANGED;
            if (item instanceof SwordItem || item instanceof AxeItem
                    || item instanceof PickaxeItem || item instanceof ShovelItem
                    || item instanceof HoeItem) return WeaponStyle.MELEE;
            // ⭐ 锤子算近战
            if (item instanceof MaceItem) return WeaponStyle.MELEE;
            if (item instanceof TridentItem) return WeaponStyle.BOTH;
        } catch (Throwable ignored) {}

        try {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                String path = id.getPath().toLowerCase();
                for (String k : RANGED_ID_KEYWORDS) {
                    if (path.contains(k)) return WeaponStyle.RANGED;
                }
                for (String k : MELEE_ID_KEYWORDS) {
                    if (path.contains(k)) return WeaponStyle.MELEE;
                }
            }
        } catch (Throwable ignored) {}

        return WeaponStyle.MELEE;
    }

    private static boolean isAffixCompatible(AffixDefinition def, WeaponStyle style) {
        Map<StatCategory, Double> stats = def.statWeights();
        boolean hasMelee = stats.containsKey(StatCategory.MELEE_DMG);
        boolean hasRanged = stats.containsKey(StatCategory.RANGED_DMG);

        if (hasMelee && !hasRanged) {
            return style == WeaponStyle.MELEE || style == WeaponStyle.BOTH;
        }
        if (hasRanged && !hasMelee) {
            return style == WeaponStyle.RANGED || style == WeaponStyle.BOTH;
        }
        return true;
    }

    public static void assignLootAffixes(ItemStack stack, RandomSource random) {
        if (!isEligibleEquipment(stack) || hasAffixData(stack)) return;

        if (random.nextFloat() >= 0.70f) return;

        List<AffixDefinition> candidates;
        if (isArmorEquipment(stack.getItem(), stack)) {
            candidates = new ArrayList<>(ARMOR_POOL.values());
        } else {
            WeaponStyle style = detectWeaponStyle(stack);
            candidates = new ArrayList<>();
            for (AffixDefinition def : WEAPON_POOL.values()) {
                if (isAffixCompatible(def, style)) {
                    candidates.add(def);
                }
            }
        }
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        float roll = random.nextFloat();
        int count;
        if (roll < 0.70f) count = 1;
        else if (roll < 0.95f) count = 2;
        else count = 3;

        CompoundTag epicReset = new CompoundTag();
        epicReset.putBoolean(IDENTIFIED_KEY, false);
        ListTag affixes = new ListTag();
        for (int i = 0; i < Math.min(count, candidates.size()); i++) {
            AffixDefinition definition = candidates.get(i);
            CompoundTag entry = new CompoundTag();
            entry.putString(ID_KEY, definition.id());
            double rolled = definition.minValue()
                    + random.nextDouble() * (definition.maxValue() - definition.minValue());
            entry.putDouble(VALUE_KEY, roundFourDecimals(rolled));
            affixes.add(entry);
        }
        epicReset.put(AFFIXES_KEY, affixes);
        updateRoot(stack, epicReset);
    }

    public static void assignWorkbenchAffixes(ItemStack stack, RandomSource random) {
        if (!isEligibleEquipment(stack) || hasAffixData(stack) || random.nextFloat() >= 0.70f) return;
        assignLootAffixes(stack, random);
    }

    public static boolean hasAffixData(ItemStack stack) {
        if (isBlacklisted(stack.getItem(), stack)) return false;
        CompoundTag root = getEpicResetTag(stack);
        return root != null && root.contains(AFFIXES_KEY, Tag.TAG_LIST);
    }

    public static boolean isIdentified(ItemStack stack) {
        CompoundTag root = getEpicResetTag(stack);
        return root != null && root.getBoolean(IDENTIFIED_KEY);
    }

    public static boolean identify(ItemStack stack) {
        CompoundTag root = getEpicResetTag(stack);
        if (root == null || root.getBoolean(IDENTIFIED_KEY)) return false;
        root.putBoolean(IDENTIFIED_KEY, true);
        updateRoot(stack, root);
        return true;
    }

    public static List<AffixInstance> getAffixes(ItemStack stack) {
        CompoundTag root = getEpicResetTag(stack);
        if (root == null) return List.of();

        ListTag list = root.getList(AFFIXES_KEY, Tag.TAG_COMPOUND);
        List<AffixInstance> result = new ArrayList<>(list.size());
        Map<String, AffixDefinition> pool = isArmorEquipment(stack.getItem(), stack) ? ARMOR_POOL : WEAPON_POOL;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            AffixDefinition definition = pool.get(entry.getString(ID_KEY));
            if (definition != null) {
                result.add(new AffixInstance(definition, entry.getDouble(VALUE_KEY)));
            }
        }
        return List.copyOf(result);
    }

    public static Map<StatCategory, Double> collectArmorMods(Player player) {
        Map<StatCategory, Double> result = new java.util.EnumMap<>(StatCategory.class);
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            mergeStackMods(result, player.getItemBySlot(slot));
        }
        return result;
    }

    public static Map<StatCategory, Double> collectWeaponMods(ItemStack stack) {
        Map<StatCategory, Double> result = new java.util.EnumMap<>(StatCategory.class);
        mergeStackMods(result, stack);
        return result;
    }

    private static void mergeStackMods(Map<StatCategory, Double> result, ItemStack stack) {
        if (!isIdentified(stack)) return;
        for (AffixInstance instance : getAffixes(stack)) {
            for (Map.Entry<StatCategory, Double> stat : instance.definition().statWeights().entrySet()) {
                result.merge(stat.getKey(), instance.value() * stat.getValue(), Double::sum);
            }
        }
    }

    private static CompoundTag getEpicResetTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag customRoot = customData.copyTag();
        if (!customRoot.contains(ROOT_KEY, Tag.TAG_COMPOUND)) return null;
        return customRoot.getCompound(ROOT_KEY).copy();
    }

    private static boolean isArmorEquipment(Item item, ItemStack stack) {
        return AffixItemFilter.isArmorWhitelisted(item) || stack.is(ARMOR_TAG);
    }

    private static boolean isBlacklisted(Item item, ItemStack stack) {
        return AffixItemFilter.isBlacklisted(item) || stack.is(HORSE_ARMOR_TAG);
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(path));
    }

    private static void updateRoot(ItemStack stack, CompoundTag epicReset) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                customRoot -> customRoot.put(ROOT_KEY, epicReset.copy()));
    }

    private static Map<String, AffixDefinition> createArmorPool() {
        Map<String, AffixDefinition> pool = new LinkedHashMap<>();
        add(pool, new AffixDefinition("armor_guard", "affix.epic-reset.armor_guard",
                0.01, 0.03, Map.of(StatCategory.DAMAGE_REDUCE, 1d)));
        add(pool, new AffixDefinition("armor_precision", "affix.epic-reset.armor_precision",
                0.005, 0.02, Map.of(StatCategory.CRIT_RATE, 1d)));
        add(pool, new AffixDefinition("armor_fury", "affix.epic-reset.armor_fury",
                0.03, 0.08, Map.of(StatCategory.CRIT_DMG, 1d)));
        add(pool, new AffixDefinition("armor_vampiric", "affix.epic-reset.armor_vampiric",
                0.005, 0.015, Map.of(StatCategory.LIFE_STEAL, 1d)));
        add(pool, new AffixDefinition("armor_piercing", "affix.epic-reset.armor_piercing",
                0.01, 0.04, Map.of(StatCategory.ARMOR_PIERCE, 1d)));
        add(pool, new AffixDefinition("armor_evasion", "affix.epic-reset.armor_evasion",
                0.005, 0.02, Map.of(StatCategory.EVASION, 1d)));
        add(pool, new AffixDefinition("armor_swiftness", "affix.epic-reset.armor_swiftness",
                0.01, 0.03, Map.of(StatCategory.MOVE_SPEED, 1d)));
        add(pool, new AffixDefinition("armor_fortitude", "affix.epic-reset.armor_fortitude",
                0.02, 0.05, Map.of(StatCategory.KNOCKBACK_RESIST, 1d)));
        add(pool, new AffixDefinition("armor_recovery", "affix.epic-reset.armor_recovery",
                0.02, 0.05, Map.of(StatCategory.HEAL_POWER, 1d)));
        add(pool, new AffixDefinition("armor_haste", "affix.epic-reset.armor_haste",
                0.01, 0.03, Map.of(StatCategory.ATTACK_SPEED, 1d)));
        return Map.copyOf(pool);
    }

    private static Map<String, AffixDefinition> createWeaponPool() {
        Map<String, AffixDefinition> pool = new LinkedHashMap<>();
        add(pool, new AffixDefinition("weapon_power", "affix.epic-reset.weapon_power",
                0.02, 0.06, Map.of(
                StatCategory.MELEE_DMG, 1d,
                StatCategory.RANGED_DMG, 1d,
                StatCategory.THROW_DMG, 1d)));
        add(pool, new AffixDefinition("weapon_precision", "affix.epic-reset.weapon_precision",
                0.01, 0.04, Map.of(StatCategory.CRIT_RATE, 1d)));
        add(pool, new AffixDefinition("weapon_fury", "affix.epic-reset.weapon_fury",
                0.04, 0.12, Map.of(StatCategory.CRIT_DMG, 1d)));
        add(pool, new AffixDefinition("weapon_piercing", "affix.epic-reset.weapon_piercing",
                0.01, 0.05, Map.of(StatCategory.ARMOR_PIERCE, 1d)));
        add(pool, new AffixDefinition("weapon_vampiric", "affix.epic-reset.weapon_vampiric",
                0.005, 0.02, Map.of(StatCategory.LIFE_STEAL, 1d)));
        add(pool, new AffixDefinition("weapon_swiftness", "affix.epic-reset.weapon_swiftness",
                0.01, 0.04, Map.of(StatCategory.ATTACK_SPEED, 1d)));
        add(pool, new AffixDefinition("weapon_burning", "affix.epic-reset.weapon_burning",
                0.01, 0.03, Map.of(StatCategory.BURN_DMG, 1d)));
        add(pool, new AffixDefinition("weapon_bleeding", "affix.epic-reset.weapon_bleeding",
                0.01, 0.03, Map.of(StatCategory.DOT_BLEED, 1d)));

        add(pool, new AffixDefinition("weapon_butcher", "affix.epic-reset.weapon_butcher",
                0.03, 0.07, Map.of(StatCategory.MELEE_DMG, 1d)));

        add(pool, new AffixDefinition("weapon_marksman", "affix.epic-reset.weapon_marksman",
                0.03, 0.07, Map.of(StatCategory.RANGED_DMG, 1d)));

        return Map.copyOf(pool);
    }

    private static void add(Map<String, AffixDefinition> pool, AffixDefinition definition) {
        pool.put(definition.id(), definition);
    }

    private static double roundFourDecimals(double value) {
        return Math.round(value * 10_000d) / 10_000d;
    }

    public record AffixInstance(AffixDefinition definition, double value) {
    }
}