package com.nanyou.epicreset.registry;

import com.nanyou.epicreset.EpicReset;
import com.nanyou.epicreset.item.IdentificationScrollItem;
import com.nanyou.epicreset.item.RockCrystalArmorItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

/**
 * 自定义物品注册（岩晶套装）- 严格按 Minecraft 1.21.1 Fabric 编译签名（通过报错推导的真实签名）
 *
 * ================ 真实 1.21.1 ArmorMaterial 构造签名（7 参数，顺序如下）：
 *  1. defense               : Map<ArmorItem.Type, Integer>   各盔甲部位防御点数（首参！Key 是 ArmorItem.Type）
 *  2. enchantmentValue      : int                            附魔能力
 *  3. equipSound            : Holder<SoundEvent>             装备音效
 *  4. repairIngredient      : Supplier<Ingredient>           铁砧修复配方供应源
 *  5. layers                : List<ArmorMaterial.Layer>      盔甲纹理层
 *  6. toughness             : float                          韧性
 *  7. knockbackResistance   : float                          击退抗性
 *
 * ================ 真实 1.21.1 ArmorItem 构造签名：
 *  public ArmorItem(Holder<ArmorMaterial> material, Type type, Item.Properties properties)
 *      → material 是第一参数，type 是第二参数（与之前猜测相反）
 */
public final class ModItems {

    // ========== 岩晶套装单部位耐久（铁盔甲基础倍率 15 × 11/16/15/13 = 头 165 / 胸 240 / 腿 225 / 靴 195）
    public static final int ROCK_CRYSTAL_HELMET_DURABILITY = 165;
    public static final int ROCK_CRYSTAL_CHESTPLATE_DURABILITY = 240;
    public static final int ROCK_CRYSTAL_LEGGINGS_DURABILITY = 225;
    public static final int ROCK_CRYSTAL_BOOTS_DURABILITY = 195;

    // ========== 岩晶套装各部位防御点数 Map<ArmorItem.Type, Integer>（Key 是 ArmorItem.Type）
    // 总防御 17：介于铁（15）与钻石（20）之间
    private static final Map<ArmorItem.Type, Integer> ROCK_CRYSTAL_DEFENSE = Map.of(
            ArmorItem.Type.HELMET, 2,      // 头盔 +2
            ArmorItem.Type.CHESTPLATE, 7,   // 胸甲 +7
            ArmorItem.Type.LEGGINGS, 5,     // 护腿 +5
            ArmorItem.Type.BOOTS, 3         // 靴子 +3
    );

    // ========== 岩晶盔甲纹理 Layer（1.21.1 ArmorMaterial 第5参数 List<ArmorMaterial.Layer>）
    private static final List<ArmorMaterial.Layer> ROCK_CRYSTAL_LAYERS = List.of(
            new ArmorMaterial.Layer(EpicReset.id("rock_crystal"))
    );

    // ========== ArmorMaterial 注册 ResourceKey ==========
    private static final ResourceKey<ArmorMaterial> ROCK_CRYSTAL_MATERIAL_KEY =
            ResourceKey.create(Registries.ARMOR_MATERIAL, EpicReset.id("rock_crystal"));

    /**
     * 注册岩晶护甲材料，返回 Holder<ArmorMaterial>
     * 严格按 1.21.1 7 参数签名：防御Map<ArmorItem.Type> / 附魔值 / equipSound Holder / 修复Supplier / layers / 韧性 / 击退
     */
    public static final Holder<ArmorMaterial> ROCK_CRYSTAL_ARMOR_MATERIAL = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ROCK_CRYSTAL_MATERIAL_KEY,
            new ArmorMaterial(
                    ROCK_CRYSTAL_DEFENSE,                          // 1. Map<ArmorItem.Type, Integer> ✅
                    15,                                              // 2. 附魔能力 15
                    SoundEvents.ARMOR_EQUIP_DIAMOND,                  // 3. Holder<SoundEvent> ✅
                    () -> Ingredient.of(Items.DIAMOND),             // 4. Supplier<Ingredient> ✅
                    ROCK_CRYSTAL_LAYERS,                             // 5. List<Layer> ✅
                    4.0f,                                            // 6. 韧性
                    0.1f                                             // 7. 击退抗性
            )
    );

    public static ArmorMaterial rockCrystalMaterial() {
        return ROCK_CRYSTAL_ARMOR_MATERIAL.value();
    }

    // ========== 4 件岩晶盔甲
    // 1.21.1 ArmorItem 构造签名：ArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties)
    public static final ArmorItem ROCK_CRYSTAL_HELMET = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
            new Item.Properties().stacksTo(1).durability(ROCK_CRYSTAL_HELMET_DURABILITY));
    public static final ArmorItem ROCK_CRYSTAL_CHESTPLATE = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
            new Item.Properties().stacksTo(1).durability(ROCK_CRYSTAL_CHESTPLATE_DURABILITY));
    public static final ArmorItem ROCK_CRYSTAL_LEGGINGS = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
            new Item.Properties().stacksTo(1).durability(ROCK_CRYSTAL_LEGGINGS_DURABILITY));
    public static final ArmorItem ROCK_CRYSTAL_BOOTS = new RockCrystalArmorItem(
            ROCK_CRYSTAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
            new Item.Properties().stacksTo(1).durability(ROCK_CRYSTAL_BOOTS_DURABILITY));

    public static final Item IDENTIFICATION_SCROLL = new IdentificationScrollItem(
            new Item.Properties().stacksTo(16));

    private ModItems() {}

    /** ModInitializer.onInitialize 中调用：注册 4 件盔甲物品 */
    public static void registerAll() {
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_helmet"), ROCK_CRYSTAL_HELMET);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_chestplate"), ROCK_CRYSTAL_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_leggings"), ROCK_CRYSTAL_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("rock_crystal_boots"), ROCK_CRYSTAL_BOOTS);
        Registry.register(BuiltInRegistries.ITEM, EpicReset.id("identification_scroll"), IDENTIFICATION_SCROLL);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(IDENTIFICATION_SCROLL));
    }
}
