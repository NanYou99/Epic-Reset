package com.nanyou.epicreset.item;

import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class RockCrystalArmorItem extends ArmorItem implements IArmorSetProvider {

    private String armorSetId;

    public RockCrystalArmorItem(ArmorMaterial material, Type type, Properties properties) {
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