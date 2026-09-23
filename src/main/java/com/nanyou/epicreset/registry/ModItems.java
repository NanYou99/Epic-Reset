package com.nanyou.epicreset.registry;

import com.nanyou.epicreset.EpicReset;
import com.nanyou.epicreset.item.IdentificationScrollItem;
import com.nanyou.epicreset.item.RockCrystalArmorItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModItems {

    public static final ArmorMaterial ROCK_CRYSTAL_ARMOR_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return switch (type) {
                case HELMET -> 165;
                case CHESTPLATE -> 240;
                case LEGGINGS -> 225;
                case BOOTS -> 195;
            };
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return switch (type) {
                case HELMET -> 2;
                case CHESTPLATE -> 7;
                case LEGGINGS -> 5;
                case BOOTS -> 3;
            };
        }

        @Override public int getEnchantmentValue() { return 15; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_DIAMOND; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(Items.DIAMOND); }
        @Override public String getName() { return "rock_crystal"; }
        @Override public float getToughness() { return 4.0f; }
        @Override public float getKnockbackResistance() { return 0.1f; }
    };

    public static final ArmorItem ROCK_CRYSTAL_HELMET = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties());
    public static final ArmorItem ROCK_CRYSTAL_CHESTPLATE = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties());
    public static final ArmorItem ROCK_CRYSTAL_LEGGINGS = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties());
    public static final ArmorItem ROCK_CRYSTAL_BOOTS = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties());

    public static final Item IDENTIFICATION_SCROLL = new IdentificationScrollItem(new Item.Properties().stacksTo(16));

    private ModItems() {}

    public static ArmorMaterial rockCrystalMaterial() {
        return ROCK_CRYSTAL_ARMOR_MATERIAL;
    }

    public static void registerAll() {
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_helmet"), ROCK_CRYSTAL_HELMET);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_chestplate"), ROCK_CRYSTAL_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_leggings"), ROCK_CRYSTAL_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_boots"), ROCK_CRYSTAL_BOOTS);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("identification_scroll"), IDENTIFICATION_SCROLL);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(IDENTIFICATION_SCROLL));
    }
}