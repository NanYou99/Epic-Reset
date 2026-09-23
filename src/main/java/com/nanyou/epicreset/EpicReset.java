package com.nanyou.epicreset;

import com.nanyou.epicreset.armorset.ArmorSetManager;
import com.nanyou.epicreset.event.CombatEvents;
import com.nanyou.epicreset.event.ServerPlayerTickHandler;
import com.nanyou.epicreset.event.ServerCombatHandler;
import com.nanyou.epicreset.loot.AffixLootFunction;
import com.nanyou.epicreset.recipe.ModRecipes;
import com.nanyou.epicreset.registry.ModItems;
import com.nanyou.epicreset.util.DamageOverrideMap;
import com.nanyou.epicreset.util.PerPlayerTimerStore;
import com.nanyou.epicreset.weapon.interact.WeaponInteractManager;
import com.nanyou.epicreset.weapon.interact.WeaponInteractRegistry;
import com.nanyou.epicreset.weapon.resonance.ResonanceManager;
import com.nanyou.epicreset.weapon.resonance.ResonanceRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpicReset implements ModInitializer {
	public static final String MOD_ID = "epic-reset";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PerPlayerTimerStore.register();
		DamageOverrideMap.register();
		ModItems.registerAll();
		ModRecipes.register();
		AffixLootFunction.register();
		ArmorSetManager.getInstance().registerAll();
		ResonanceRegistry.registerAll();
		WeaponInteractRegistry.registerAll();
		ServerPlayerTickHandler.register();
		CombatEvents.register();
		
		// 注册护甲套装战斗属性处理器
		ServerCombatHandler.register();

		LOGGER.info("[共鸣] ResonanceManager 注册条目 [{}]", ResonanceManager.getInstance().getRegisteredCount());
		LOGGER.info("[互动] WeaponInteractManager intrinsic=[{}] interact=[{}]",
				WeaponInteractManager.getInstance().getIntrinsicCount(),
				WeaponInteractManager.getInstance().getInteractCount());
		LOGGER.info("[EpicReset] 模组初始化完成 - 已启用套装、材质共鸣、随机词条、鉴定与护甲熔炼");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}