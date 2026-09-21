package com.nanyou.epicreset.armorset;

import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import com.nanyou.epicreset.armorset.set.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 套装管理器（单例）
 * 负责：
 * 1. 注册所有套装定义
 * 2. 根据玩家穿戴计算各套装已装备数量
 * 3. 对外查询套装定义
 * 线程安全：使用 ConcurrentHashMap 存储注册数据
 */
public final class ArmorSetManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("ArmorSetManager");
    private static final ArmorSetManager INSTANCE = new ArmorSetManager();

    // 按套装ID存储 （线程安全）
    private final Map<String, ArmorSet> setsById = new ConcurrentHashMap<>();
    // 按Item反向查套装ID（用于快速判定某件盔甲属于哪个套装）
    private final Map<Item, String> itemToSetId = new ConcurrentHashMap<>();

    // 4个护甲槽位顺序：头盔、胸甲、护腿、靴子
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private ArmorSetManager() {}

    public static ArmorSetManager getInstance() {
        return INSTANCE;
    }

    /**
     * 模组初始化时调用：注册全部套装
     */
    public void registerAll() {
        register(LeatherArmorSet.create());
        register(ChainmailArmorSet.create());
        register(IronArmorSet.create());
        register(GoldenArmorSet.create());
        register(DiamondArmorSet.create());
        register(NetheriteArmorSet.create());
        register(RockCrystalArmorSet.create());
        LOGGER.info("已注册 [{}] 套盔甲套装", setsById.size());
    }

    /**
     * 注册单个套装
     */
    public void register(ArmorSet set) {
        if (setsById.putIfAbsent(set.getId(), set) != null) {
            throw new IllegalArgumentException("重复注册套装ID: " + set.getId());
        }
        // 建立Item反向索引
        for (Item piece : set.getPieces().values()) {
            itemToSetId.put(piece, set.getId());
            // 同时将套装ID写入IArmorSetProvider接口（给Mixin注入的原版盔甲用）
            if (piece instanceof IArmorSetProvider provider) {
                provider.epicreset$setArmorSetId(set.getId());
            }
        }
    }

    public ArmorSet getSet(String id) {
        return setsById.get(id);
    }

    public Collection<ArmorSet> getAllSets() {
        return Collections.unmodifiableCollection(setsById.values());
    }

    /**
     * 通过盔甲物品反查套装
     */
    public ArmorSet getSetByItem(Item item) {
        String setId = itemToSetId.get(item);
        return setId == null ? null : setsById.get(setId);
    }

    /**
     * 获取玩家身上4个护甲槽的物品集合（忽略空物品）
     */
    public List<Item> getWornArmorItems(Player player) {
        List<Item> list = new ArrayList<>(4);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                list.add(stack.getItem());
            }
        }
        return list;
    }

    /**
     * 计算某玩家对某套装的穿戴数量（0~4）
     */
    public int countEquipped(Player player, ArmorSet set) {
        return set.countEquipped(getWornArmorItems(player));
    }

    /**
     * 判定套装激活状态
     * @return 0=未激活 1=仅2件套  2=2件+4件套全部激活
     */
    public int getActivationLevel(int equippedCount) {
        if (equippedCount >= 4) return 2;
        if (equippedCount >= 2) return 1;
        return 0;
    }

    public static EquipmentSlot[] getArmorSlots() {
        return ARMOR_SLOTS.clone();
    }
}
