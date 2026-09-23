package com.nanyou.epicreset.mixin;

import com.nanyou.epicreset.event.CombatEvents;
import com.nanyou.epicreset.util.DamageOverrideMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    /**
     * 对应原版 1.21 的 ServerLivingEntityEvents.AFTER_DAMAGE 事件。
     * 在 actuallyHurt 方法执行完毕（TAIL）后触发，用于：
     * - 击杀回复（金套装击杀吸血）
     * - 共鸣/武器互动 onHit 回调
     */
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void epicreset$afterDamage(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        CombatEvents.triggerAfterDamage(self, source, amount, amount, false);
    }
}