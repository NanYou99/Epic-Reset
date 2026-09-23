package com.nanyou.epicreset.recipe;

import com.google.gson.JsonObject;
import com.nanyou.epicreset.EpicReset;
import com.nanyou.epicreset.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class ArmorSmeltingRecipe implements Recipe<Container> {

    private static final ResourceLocation ID = EpicReset.id("armor_smelting");

    public static final RecipeSerializer<ArmorSmeltingRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public ArmorSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new ArmorSmeltingRecipe();
        }

        @Override
        public ArmorSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new ArmorSmeltingRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ArmorSmeltingRecipe recipe) {
        }
    };

    private static final Map<Item, Salvage> SALVAGE = createSalvageMap();

    public ArmorSmeltingRecipe() {}

    @Override
    public boolean matches(Container container, Level level) {
        return SALVAGE.containsKey(container.getItem(0).getItem());
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return createResult(container.getItem(0));
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return new ItemStack(Items.IRON_INGOT);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMELTING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ItemStack createResult(ItemStack armor) {
        Salvage salvage = SALVAGE.get(armor.getItem());
        if (salvage == null) return ItemStack.EMPTY;

        double remaining = armor.isDamageableItem()
                ? (double) (armor.getMaxDamage() - armor.getDamageValue()) / armor.getMaxDamage()
                : 1d;
        int count = Math.max(1, (int) Math.ceil(salvage.maxCount() * Math.max(0d, remaining)));
        return new ItemStack(salvage.material(), count);
    }

    private static Map<Item, Salvage> createSalvageMap() {
        Map<Item, Salvage> result = new HashMap<>();
        addVanillaSet(result, Items.LEATHER, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        addVanillaSet(result, Items.IRON_INGOT, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        addVanillaSet(result, Items.IRON_INGOT, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        addVanillaSet(result, Items.GOLD_INGOT, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        addVanillaSet(result, Items.DIAMOND, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        addVanillaSet(result, Items.DIAMOND, ModItems.ROCK_CRYSTAL_HELMET, ModItems.ROCK_CRYSTAL_CHESTPLATE, ModItems.ROCK_CRYSTAL_LEGGINGS, ModItems.ROCK_CRYSTAL_BOOTS);
        return Map.copyOf(result);
    }

    private static void addVanillaSet(Map<Item, Salvage> result, Item material, Item helmet, Item chestplate, Item leggings, Item boots) {
        result.put(helmet, new Salvage(material, 2));
        result.put(chestplate, new Salvage(material, 4));
        result.put(leggings, new Salvage(material, 3));
        result.put(boots, new Salvage(material, 2));
    }

    private record Salvage(Item material, int maxCount) {
    }
}