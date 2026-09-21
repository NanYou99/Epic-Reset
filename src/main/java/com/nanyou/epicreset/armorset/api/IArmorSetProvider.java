package com.nanyou.epicreset.armorset.api;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/**
 * 盔甲套装标记接口
 * 通过 Mixin 注入到原版/自定义盔甲物品上，用于标识该盔甲属于哪个套装
 */
public interface IArmorSetProvider {

    /**
     * 获取该盔甲所属的套装ID
     * @return 套装ID，若不属于任何套装返回null
     */
    String epicreset$getArmorSetId();

    /**
     * 设置该盔甲所属的套装ID（内部注册用）
     */
    void epicreset$setArmorSetId(String setId);

    /**
     * 辅助方法：判断Item是否为盔甲且实现了套装接口
     */
    static boolean isSetItem(Item item) {
        return item instanceof ArmorItem && item instanceof IArmorSetProvider;
    }
}
