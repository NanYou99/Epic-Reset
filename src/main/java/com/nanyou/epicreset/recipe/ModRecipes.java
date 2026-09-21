package com.nanyou.epicreset.recipe;

import com.nanyou.epicreset.EpicReset;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModRecipes {

    private ModRecipes() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                EpicReset.id("armor_smelting"), ArmorSmeltingRecipe.SERIALIZER);
    }
}
