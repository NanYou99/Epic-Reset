package com.nanyou.epicreset.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nanyou.epicreset.affix.AffixManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private Player player;

    @ModifyReturnValue(method = "remove", at = @At("RETURN"))
    private ItemStack epicreset$assignWorkbenchAffixes(ItemStack result) {
        if (player.level().isClientSide || craftSlots.getWidth() != 3 || craftSlots.getHeight() != 3) return result;
        AffixManager.assignWorkbenchAffixes(result, player.getRandom());
        return result;
    }
}
