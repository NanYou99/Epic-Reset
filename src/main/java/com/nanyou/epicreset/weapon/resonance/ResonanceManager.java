package com.nanyou.epicreset.weapon.resonance;

import com.nanyou.epicreset.armorset.ArmorSetApplier;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;

import java.util.EnumMap;
import java.util.Map;

import static com.nanyou.epicreset.weapon.resonance.WeaponType.*;

public final class ResonanceManager {

    private static final ResonanceManager INST = new ResonanceManager();

    private final EnumMap<MaterialKey, EnumMap<WeaponType, ResonanceEntry>> table =
            new EnumMap<>(MaterialKey.class);

    private int registeredCount;

    private ResonanceManager() {}

    public static ResonanceManager getInstance() { return INST; }

    public int getRegisteredCount() { return registeredCount; }

    public synchronized void register(ResonanceEntry entry) {
        table.computeIfAbsent(entry.mat(), k -> new EnumMap<>(WeaponType.class))
                .put(entry.type(), entry);
        registeredCount++;
    }

    public ResonanceEntry getActive(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.isEmpty()) return null;
        Item item = main.getItem();
        WeaponType wt = weaponTypeOf(item);
        if (wt != SWORD && wt != AXE) return null;
        MaterialKey mat = ArmorSetApplier.getInstance().getFullArmorMaterial(player);
        if (mat == null) return null;
        if (!isWeaponMatchesArmorMaterial(item, mat, wt)) return null;
        var matTab = table.get(mat);
        return matTab == null ? null : matTab.get(wt);
    }

    public Map<StatCategory, Double> getActiveTotalMods(ServerPlayer player) {
        ResonanceEntry e = getActive(player);
        if (e == null) return Map.of();
        EnumMap<StatCategory, Double> out = new EnumMap<>(StatCategory.class);
        if (e.mods() != null) out.putAll(e.mods());
        if (e.extraDynamicMods() != null) {
            Map<StatCategory, Double> extra = e.extraDynamicMods().apply(player);
            if (extra != null) out.putAll(extra);
        }
        return out;
    }

    public ResonanceEntry lookup(MaterialKey m, WeaponType t) {
        var mat = table.get(m); return mat == null ? null : mat.get(t);
    }

    public static WeaponType weaponTypeOf(Item item) {
        if (item instanceof SwordItem) return SWORD;
        if (item instanceof AxeItem) return AXE;
        if (item instanceof BowItem) return BOW;
        if (item instanceof CrossbowItem) return CROSSBOW;
        if (item instanceof TridentItem) return TRIDENT;
        if (item instanceof MaceItem) return MACE;
        if (item instanceof ShieldItem) return SHIELD;
        return OTHER;
    }

    public static boolean isWeaponMatchesArmorMaterial(Item item, MaterialKey mat, WeaponType wt) {
        if (wt != SWORD && wt != AXE) return false;
        return switch (mat) {
            case LEATHER -> item == Items.WOODEN_SWORD || item == Items.WOODEN_AXE;
            case CHAINMAIL -> item == Items.STONE_SWORD || item == Items.STONE_AXE;
            case IRON -> item == Items.IRON_SWORD || item == Items.IRON_AXE;
            case GOLDEN -> item == Items.GOLDEN_SWORD || item == Items.GOLDEN_AXE;
            case DIAMOND -> item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE;
            case NETHERITE -> item == Items.NETHERITE_SWORD || item == Items.NETHERITE_AXE;
            case ROCK_CRYSTAL -> false;
        };
    }

    public static MaterialKey materialOfSwordAxe(Item item) {
        if (item == Items.WOODEN_SWORD || item == Items.WOODEN_AXE) return MaterialKey.LEATHER;
        if (item == Items.STONE_SWORD || item == Items.STONE_AXE) return MaterialKey.CHAINMAIL;
        if (item == Items.IRON_SWORD || item == Items.IRON_AXE) return MaterialKey.IRON;
        if (item == Items.GOLDEN_SWORD || item == Items.GOLDEN_AXE) return MaterialKey.GOLDEN;
        if (item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE) return MaterialKey.DIAMOND;
        if (item == Items.NETHERITE_SWORD || item == Items.NETHERITE_AXE) return MaterialKey.NETHERITE;
        return null;
    }
}
