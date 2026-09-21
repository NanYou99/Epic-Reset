package com.nanyou.epicreset.item;

import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

/**
 * 岩晶盔甲物品 - 1.21 构造签名（Holder<ArmorMaterial>）
 */
public class RockCrystalArmorItem extends ArmorItem implements IArmorSetProvider {

    private String armorSetId;

    /**
     * 1.21.1 ArmorItem 真实签名：ArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties)
     */
    public RockCrystalArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public String epicreset$getArmorSetId() {
        return this.armorSetId;
    }

    @Override
    public void epicreset$setArmorSetId(String setId) {
        this.armorSetId = setId;
    }
}
