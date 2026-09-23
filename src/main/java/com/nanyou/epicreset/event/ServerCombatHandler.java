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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ServerCombatHandler {

    private static final Random RNG = new Random();
    private static int tickCounter = 0;

    private static final UUID HEALTH_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOVE_SPEED_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID KNOCKBACK_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID ARMOR_UUID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof Player player)) return true;
            if (player.level().isClientSide()) return true;
            if (source.getEntity() == null) return true;

            Map<StatCategory, Double> stats = collectAllStats(player);
            double evasion = Math.min(0.30, stats.getOrDefault(StatCategory.EVASION, 0d));

            if (evasion > 0 && RNG.nextDouble() < evasion) {
                player.displayClientMessage(Component.literal("闪避！").withStyle(ChatFormatting.AQUA), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.5f, 1.5f);
                return false;
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 20 != 0) return;

            for (Player player : server.getPlayerList().getPlayers()) {
                Map<StatCategory, Double> stats = collectAllStats(player);

                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_UUID, "epicreset_health",
                        stats.getOrDefault(StatCategory.MAX_HEALTH, 0d), AttributeModifier.Operation.ADDITION);

                applyModifier(player, Attributes.ARMOR, ARMOR_UUID, "epicreset_armor",
                        stats.getOrDefault(StatCategory.ARMOR, 0d), AttributeModifier.Operation.ADDITION);

                double atkSpeedFactor = stats.getOrDefault(StatCategory.ATTACK_SPEED, 1.0) - 1.0;
                if (atkSpeedFactor > 0) {
                    applyModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUID, "epicreset_attack_speed",
                            atkSpeedFactor, AttributeModifier.Operation.MULTIPLY_BASE);
                } else {
                    removeModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUID);
                }

                double moveSpeedFactor = stats.getOrDefault(StatCategory.MOVE_SPEED, 1.0) - 1.0;
                if (moveSpeedFactor > 0) {
                    applyModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_UUID, "epicreset_move_speed",
                            moveSpeedFactor, AttributeModifier.Operation.MULTIPLY_BASE);
                } else {
                    removeModifier(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_UUID);
                }

                applyModifier(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_UUID, "epicreset_knockback_resist",
                        stats.getOrDefault(StatCategory.KNOCKBACK_RESIST, 0d), AttributeModifier.Operation.ADDITION);
            }
        });
    }

    private static void applyModifier(Player player, Attribute attribute, UUID uuid, String name,
                                      double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(uuid);
        if (value > 0) {
            try {
                instance.addTransientModifier(new AttributeModifier(uuid, name, value, operation));
            } catch (IllegalArgumentException e) {
                return;
            }
            if (attribute == Attributes.MAX_HEALTH && player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    private static void removeModifier(Player player, Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(uuid);
    }

    public static Map<StatCategory, Double> collectAllStats(Player player) {
        StatAccumulator accumulator = new StatAccumulator();
        ArmorSetManager manager = ArmorSetManager.getInstance();
        Map<String, List<Boolean>> setPieces = new HashMap<>();

        for (EquipmentSlot slot : ArmorSetManager.getArmorSlots()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            String groupKey = null;
            if (stack.getItem() instanceof IArmorSetProvider provider) {
                groupKey = provider.epicreset$getArmorSetId();
            }
            if (groupKey == null) {
                Optional<ThirdPartyArmorSetResolver.SetInfo> infoOpt = ThirdPartyArmorSetResolver.getSetInfo(stack, player);
                if (infoOpt.isPresent()) groupKey = infoOpt.get().groupKey();
            }
            if (groupKey == null) continue;

            ThirdPartyArmorSetResolver.TierInfo tierInfo = ThirdPartyArmorSetResolver.getTierInfo(groupKey, manager);
            if (tierInfo == null) continue;

            boolean counts = true;
            if (ThirdPartyArmorSetResolver.requiresIdentification(tierInfo.tier())) {
                counts = ThirdPartyArmorSetResolver.isPieceIdentified(stack);
            }
            setPieces.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(counts);
        }

        for (Map.Entry<String, List<Boolean>> entry : setPieces.entrySet()) {
            int validCount = 0;
            for (boolean b : entry.getValue()) if (b) validCount++;
            if (validCount < 2) continue;

            ThirdPartyArmorSetResolver.TierInfo tierInfo = ThirdPartyArmorSetResolver.getTierInfo(entry.getKey(), manager);
            if (tierInfo == null) continue;
            if (validCount >= 2) tierInfo.twoPieceMods().forEach(accumulator::add);
            if (validCount >= 4) tierInfo.fourPieceMods().forEach(accumulator::add);
        }

        accumulator.merge(AffixManager.collectArmorMods(player));
        return accumulator.mulReduce();
    }

    public static Map<StatCategory, Double> collectAllStatsPublic(Player player) {
        return collectAllStats(player);
    }
}