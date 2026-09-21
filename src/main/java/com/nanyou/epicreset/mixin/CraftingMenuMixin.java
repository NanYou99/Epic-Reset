package com.nanyou.epicreset.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nanyou.epicreset.affix.AffixManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @ModifyExpressionValue(
            method = "quickMoveStack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack epicreset$assignQuickCraftAffixes(ItemStack result, Player player, int slotIndex) {
        if (slotIndex == 0 && !player.level().isClientSide) {
            AffixManager.assignWorkbenchAffixes(result, player.getRandom());
        }
        return result;
    }
}
