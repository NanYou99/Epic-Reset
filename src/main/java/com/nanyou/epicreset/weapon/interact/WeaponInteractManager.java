package com.nanyou.epicreset.weapon.interact;

import com.nanyou.epicreset.armorset.ArmorSetApplier;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import com.nanyou.epicreset.weapon.resonance.MaterialKey;
import com.nanyou.epicreset.weapon.resonance.ResonanceManager;
import com.nanyou.epicreset.weapon.resonance.WeaponType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;

import java.util.*;

import static com.nanyou.epicreset.armorset.stat.StatCategory.*;
import static com.nanyou.epicreset.weapon.resonance.WeaponType.*;

public final class WeaponInteractManager {

    private static final WeaponInteractManager INST = new WeaponInteractManager();

    private final EnumMap<WeaponType, EnumMap<MaterialKey, WeaponInteractEntry>> table =
            new EnumMap<>(WeaponType.class);
    private final EnumMap<WeaponType, Map<StatCategory, Double>> intrinsic =
            new EnumMap<>(WeaponType.class);

    private int intrinsicCount;
    private int interactCount;

    private WeaponInteractManager() {}

    public static WeaponInteractManager getInstance() { return INST; }

    public int getIntrinsicCount() { return intrinsicCount; }
    public int getInteractCount() { return interactCount; }

    public synchronized void registerIntrinsic(WeaponType wt, Map<StatCategory, Double> mods) {
        intrinsic.put(wt, Map.copyOf(mods));
        intrinsicCount++;
    }

    public synchronized void register(WeaponInteractEntry e) {
        table.computeIfAbsent(e.type(), k -> new EnumMap<>(MaterialKey.class))
                .put(e.fullArmorMat(), e);
        interactCount++;
    }

    public Map<StatCategory, Double> getIntrinsicMods(WeaponType wt) {
        return intrinsic.getOrDefault(wt, Map.of());
    }

    public WeaponInteractEntry getActiveInteract(ServerPlayer player, WeaponType wt) {
        if (wt == SWORD || wt == AXE || wt == OTHER) return null;
        MaterialKey full = ArmorSetApplier.getInstance().getFullArmorMaterial(player);
        if (full == null) return null;
        var mat = table.get(wt); return mat == null ? null : mat.get(full);
    }

    public Map<StatCategory, Double> getAllInteractMods(ServerPlayer player, WeaponType wt) {
        WeaponInteractEntry e = getActiveInteract(player, wt);
        return (e == null || e.mods() == null) ? Map.of() : e.mods();
    }

    public static WeaponType weaponTypeOfStack(ItemStack s) {
        if (s == null || s.isEmpty()) return OTHER;
        return ResonanceManager.weaponTypeOf(s.getItem());
    }

    public static boolean isBowLike(WeaponType wt) { return wt == BOW || wt == CROSSBOW || wt == TRIDENT; }
}
