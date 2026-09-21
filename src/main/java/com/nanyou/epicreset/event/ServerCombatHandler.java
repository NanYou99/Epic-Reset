package com.nanyou.epicreset.event;

import com.nanyou.epicreset.affix.AffixManager;
import com.nanyou.epicreset.armorset.ArmorSetManager;
import com.nanyou.epicreset.armorset.ThirdPartyArmorSetResolver;
import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import com.nanyou.epicreset.armorset.stat.StatAccumulator;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ServerCombatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("epic-reset-combat");
    private static final Random RNG = new Random();
    private static int tickCounter = 0;

    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("epic-reset", "set_bonus_health");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("epic-reset", "set_bonus_attack_speed");
    private static final ResourceLocation MOVE_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("epic-reset", "set_bonus_move_speed");
    private static final ResourceLocation ATTACK_RANGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("epic-reset", "set_bonus_attack_range");
    private static final ResourceLocation KNOCKBACK_RESIST_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("epic-reset", "set_bonus_knockback_resist");

    public static void register() {
        // 闪避
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof Player player)) return true;
            if (player.level().isClientSide()) return true;
            if (source.getEntity() == null) return true;

            Map<StatCategory, Double> stats = collectAllStats(player);
            double evasion = stats.getOrDefault(StatCategory.EVASION, 0d);
            if (evasion > 0.30) evasion = 0.30;

            if (evasion > 0 && RNG.nextDouble() < evasion) {
                player.displayClientMessage(
                        Component.literal("闪避！").withStyle(ChatFormatting.AQUA), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.5f, 1.5f);
                return false;
            }
            return true;
        });

        // 被动属性刷新
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 20 != 0) return;

            for (Player player : server.getPlayerList().getPlayers()) {
                Map<StatCategory, Double> stats = collectAllStats(player);

                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID,
                        stats.getOrDefault(StatCategory.MAX_HEALTH, 0d),
                        AttributeModifier.Operation.ADD_VALUE, true);

                applyModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID,
                        stats.getOrDefault(StatCategory.ATTACK_SPEED, 0d),
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);

                applyModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MODIFIER_ID,
                        stats.getOrDefault(StatCategory.MOVE_SPEED, 0d),
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);

                applyModifier(player, Attributes.ENTITY_INTERACTION_RANGE, ATTACK_RANGE_MODIFIER_ID,
                        stats.getOrDefault(StatCategory.ATTACK_RANGE, 0d),
                        AttributeModifier.Operation.ADD_VALUE, false);

                applyModifier(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESIST_MODIFIER_ID,
                        stats.getOrDefault(StatCategory.KNOCKBACK_RESIST, 0d),
                        AttributeModifier.Operation.ADD_VALUE, false);
            }
        });
    }

    /**
     * ⭐ 修复：ADD_MULTIPLIED_BASE 操作需要传 rawSum（factor - 1）
     */
    private static void applyModifier(Player player,
                                      net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                      ResourceLocation modifierId, double value,
                                      AttributeModifier.Operation operation, boolean healOnGain) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(modifierId);

        // ⭐ 修复：ADD_MULTIPLIED_BASE 需要的是 rawSum，不是 factor
        double modifierValue = value;
        if (operation == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
            modifierValue = value - 1.0;
        }

        if (modifierValue > 0) {
            float before = player.getHealth();
            float beforeMax = player.getMaxHealth();
            try {
                instance.addTransientModifier(new AttributeModifier(modifierId, modifierValue, operation));
            } catch (IllegalArgumentException e) {
                return;
            }
            if (healOnGain && before > 0 && Math.abs(before - beforeMax) < 0.01f) {
                player.setHealth(player.getMaxHealth());
            }
        } else if (healOnGain && player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static Map<StatCategory, Double> collectAllStats(Player player) {
        StatAccumulator accumulator = new StatAccumulator();
        ArmorSetManager manager = ArmorSetManager.getInstance();

        // ============ 1. 套装属性 ============
        Map<String, List<Boolean>> setPieces = new HashMap<>();

        for (EquipmentSlot slot : ArmorSetManager.getArmorSlots()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            String groupKey = null;

            if (stack.getItem() instanceof IArmorSetProvider provider) {
                groupKey = provider.epicreset$getArmorSetId();
            } else {
                Optional<ThirdPartyArmorSetResolver.SetInfo> infoOpt =
                        ThirdPartyArmorSetResolver.getSetInfo(stack, player);
                if (infoOpt.isPresent()) {
                    groupKey = infoOpt.get().groupKey();
                }
            }

            if (groupKey == null) continue;

            ThirdPartyArmorSetResolver.TierInfo tierInfo =
                    ThirdPartyArmorSetResolver.getTierInfo(groupKey, manager);
            if (tierInfo == null) continue;

            boolean counts = true;
            if (ThirdPartyArmorSetResolver.requiresIdentification(tierInfo.tier())) {
                counts = ThirdPartyArmorSetResolver.isPieceIdentified(stack);
            }

            setPieces.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(counts);
        }

        for (Map.Entry<String, List<Boolean>> entry : setPieces.entrySet()) {
            String groupKey = entry.getKey();
            List<Boolean> flags = entry.getValue();

            int validCount = 0;
            for (boolean b : flags) {
                if (b) validCount++;
            }

            if (validCount < 2) continue;

            ThirdPartyArmorSetResolver.TierInfo tierInfo =
                    ThirdPartyArmorSetResolver.getTierInfo(groupKey, manager);
            if (tierInfo == null) continue;

            if (validCount >= 2) tierInfo.twoPieceMods().forEach(accumulator::add);
            if (validCount >= 4) tierInfo.fourPieceMods().forEach(accumulator::add);
        }

        // ============ 2. 随机词条 ============
        accumulator.merge(AffixManager.collectArmorMods(player));

        return accumulator.mulReduce();
    }

    public static Map<StatCategory, Double> collectAllStatsPublic(Player player) {
        return collectAllStats(player);
    }
}