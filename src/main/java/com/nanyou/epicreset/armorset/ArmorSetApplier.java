package com.nanyou.epicreset.armorset;

import com.nanyou.epicreset.armorset.set.*;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import com.nanyou.epicreset.weapon.resonance.MaterialKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public final class ArmorSetApplier {

    private ArmorSetApplier() {}

    private static final EquipmentSlot[] ARMOR = ArmorSetManager.getArmorSlots();

    public static ArmorSetApplier getInstance() { return Holder.INST; }

    public Map<StatCategory, Double> collectArmorMods(Player player) {
        EnumMap<StatCategory, Double> out = new EnumMap<>(StatCategory.class);
        ArmorSetManager mgr = ArmorSetManager.getInstance();
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (!(item instanceof ArmorItem ai)) continue;
            ArmorSet set = mgr.getSetByItem(item);
            if (set == null) continue;
            Map<StatCategory, Double> mods = set.getPieceStatMods(ai.getType());
            if (mods != null) mergeInto(out, mods);
        }
        mergeInto(out, collectSetBonusMods(player, mgr));
        return out;
    }

    public Map<StatCategory, Double> collectSetBonusMods(Player player, ArmorSetManager mgr) {
        EnumMap<StatCategory, Double> out = new EnumMap<>(StatCategory.class);
        var worn = mgr.getWornArmorItems(player);
        for (ArmorSet set : mgr.getAllSets()) {
            int eq = set.countEquipped(worn);
            if (eq >= 2) mergeInto(out, set.getTwoPieceStatMods());
            if (eq >= 4) mergeInto(out, set.getFourPieceStatMods());
        }
        mergeInto(out, ThirdPartyArmorSetResolver.collectSetBonusMods(player, mgr));
        return out;
    }

    public MaterialKey getFullArmorMaterial(Player player) {
        ArmorSetManager mgr = ArmorSetManager.getInstance();
        var worn = mgr.getWornArmorItems(player);
        if (worn.size() < 4) return null;
        for (ArmorSet set : mgr.getAllSets()) {
            if (set.countEquipped(worn) == 4) {
                return MaterialKey.fromSetId(set.getId());
            }
        }
        return null;
    }

    private static void mergeInto(EnumMap<StatCategory, Double> acc, Map<StatCategory, Double> other) {
        if (other == null || other.isEmpty()) return;
        for (var e : other.entrySet()) {
            acc.merge(e.getKey(), e.getValue(), Double::sum);
        }
    }

    private static final class Holder {
        static final ArmorSetApplier INST = new ArmorSetApplier();
    }
}
