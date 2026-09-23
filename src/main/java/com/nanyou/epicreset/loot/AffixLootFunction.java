package com.nanyou.epicreset.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.nanyou.epicreset.EpicReset;
import com.nanyou.epicreset.affix.AffixManager;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class AffixLootFunction extends LootItemConditionalFunction {

    private static final float CHEST_AFFIX_CHANCE = 0.75f;
    private static final AffixLootFunction DEFAULT_INSTANCE = new AffixLootFunction(1.0f, new LootItemCondition[0]);
    private static final AffixLootFunction CHEST_INSTANCE = new AffixLootFunction(CHEST_AFFIX_CHANCE, new LootItemCondition[0]);

    public static final LootItemFunctionType TYPE = Registry.register(
            BuiltInRegistries.LOOT_FUNCTION_TYPE,
            EpicReset.id("random_affixes"),
            new LootItemFunctionType(new Serializer()));

    private final float affixChance;

    private AffixLootFunction(float affixChance, LootItemCondition[] conditions) {
        super(conditions);
        this.affixChance = affixChance;
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            boolean isChestLoot = id.getPath().startsWith("chests/");
            tableBuilder.apply(isChestLoot ? CHEST_INSTANCE : DEFAULT_INSTANCE);
        });
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.getRandom().nextFloat() < affixChance) {
            AffixManager.assignLootAffixes(stack, context.getRandom());
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AffixLootFunction> {
        @Override
        public void serialize(JsonObject json, AffixLootFunction value, JsonSerializationContext ctx) {
            json.addProperty("chance", value.affixChance);
        }

        @Override
        public AffixLootFunction deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            float chance = json.has("chance") ? GsonHelper.getAsFloat(json, "chance") : 1.0f;
            return new AffixLootFunction(chance, conditions);
        }
    }
}