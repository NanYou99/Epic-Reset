package com.nanyou.epicreset.armorset.api;

import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

@FunctionalInterface
public interface IStatModifiable {
    Map<StatCategory, Double> getStatMods(ServerPlayer player);
}
