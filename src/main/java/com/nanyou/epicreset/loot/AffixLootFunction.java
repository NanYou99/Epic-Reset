package com.nanyou.epicreset.loot;

import com.mojang.serialization.MapCodec;
import com.nanyou.epicreset.EpicReset;
import com.nanyou.epicreset.affix.AffixManager;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class AffixLootFunction implements LootItemFunction {

    private static final float CHEST_AFFIX_CHANCE = 0.75f;
    private static final AffixLootFunction DEFAULT_INSTANCE = new AffixLootFunction(1.0f);
    private static final AffixLootFunction CHEST_INSTANCE = new AffixLootFunction(CHEST_AFFIX_CHANCE);

    private static final LootItemFunctionType<AffixLootFunction> TYPE = Registry.register(
            BuiltInRegistries.LOOT_FUNCTION_TYPE,
            EpicReset.id("random_affixes"),
            new LootItemFunctionType<>(MapCodec.unit(DEFAULT_INSTANCE)));

    private final float affixChance;

    private AffixLootFunction(float affixChance) {
        this.affixChance = affixChance;
    }

    public static void register() {
        TYPE.codec();
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            boolean isChestLoot = key.location().getPath().startsWith("chests/");
            tableBuilder.apply(() -> isChestLoot ? CHEST_INSTANCE : DEFAULT_INSTANCE);
        });
    }

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (context.getRandom().nextFloat() < affixChance) {
            AffixManager.assignLootAffixes(stack, context.getRandom());
        }
        return stack;
    }
}
