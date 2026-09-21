package com.nanyou.epicreset.mixin;

import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import net.minecraft.world.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin：给原版全部 ArmorItem 动态注入 IArmorSetProvider 接口
 * 这样原版的皮革/锁链/铁/金/钻石/下界合金盔甲都能携带套装ID
 * 自定义岩晶盔甲直接实现该接口，不受此 Mixin 影响
 *
 * 注意：Mixin 不需要显式继承 ArmorItem 父类，仅实现接口即可让所有 ArmorItem 实例
 *  instanceof IArmorSetProvider 为 true；字段由 Mixin 注入到 ArmorItem 类中
 */
@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin implements IArmorSetProvider {

    @Unique
    private String epicreset$armorSetId;

    @Override
    public String epicreset$getArmorSetId() {
        return this.epicreset$armorSetId;
    }

    @Override
    public void epicreset$setArmorSetId(String setId) {
        this.epicreset$armorSetId = setId;
    }
}
