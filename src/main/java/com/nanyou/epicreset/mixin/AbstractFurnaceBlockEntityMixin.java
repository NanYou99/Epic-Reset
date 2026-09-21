package com.nanyou.epicreset.mixin;

import com.nanyou.epicreset.recipe.ArmorSmeltingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Inject(method = "canBurn", at = @At("HEAD"), cancellable = true)
    private static void epicreset$canSmeltArmor(RegistryAccess registries, RecipeHolder<?> recipe,
                                                NonNullList<ItemStack> items, int maxStackSize,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (recipe == null || !(recipe.value() instanceof ArmorSmeltingRecipe armorRecipe)) return;

        ItemStack result = armorRecipe.createResult(items.get(0));
        ItemStack output = items.get(2);
        boolean canAccept = !result.isEmpty() && (output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount()
                <= Math.min(maxStackSize, output.getMaxStackSize()));
        cir.setReturnValue(canAccept);
    }

    @Inject(method = "burn", at = @At("HEAD"), cancellable = true)
    private static void epicreset$smeltArmor(RegistryAccess registries, RecipeHolder<?> recipe,
                                             NonNullList<ItemStack> items, int maxStackSize,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (recipe == null || !(recipe.value() instanceof ArmorSmeltingRecipe armorRecipe)) return;

        ItemStack result = armorRecipe.createResult(items.get(0));
        ItemStack output = items.get(2);
        if (result.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (output.isEmpty()) {
            items.set(2, result.copy());
        } else if (ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount()
                <= Math.min(maxStackSize, output.getMaxStackSize())) {
            output.grow(result.getCount());
        } else {
            cir.setReturnValue(false);
            return;
        }

        items.get(0).shrink(1);
        cir.setReturnValue(true);
    }
}
