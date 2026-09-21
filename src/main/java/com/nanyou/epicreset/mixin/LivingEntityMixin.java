package com.nanyou.epicreset.mixin;

import com.nanyou.epicreset.util.DamageOverrideMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "actuallyHurt",
            at = @At("HEAD"),
            argsOnly = true)
    private float epicreset$applyPreArmorDamage(float damageAmount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        Float override = DamageOverrideMap.consumeOverride(self);
        return override != null ? override : damageAmount;
    }

    @ModifyVariable(method = "getDamageAfterArmorAbsorb",
            at = @At("HEAD"),
            argsOnly = true)
    private float epicreset$applyPostArmorDamage(float damageAmount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        Float override = DamageOverrideMap.consumeArmored(self);
        return override != null ? override : damageAmount;
    }
}
