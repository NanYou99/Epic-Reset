package com.nanyou.epicreset.item;

import com.nanyou.epicreset.affix.AffixManager;
import com.nanyou.epicreset.armorset.ArmorSetManager;
import com.nanyou.epicreset.armorset.ThirdPartyArmorSetResolver;
import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class IdentificationScrollItem extends Item {

    public IdentificationScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack scroll = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(scroll);

        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);

        if (target.isEmpty()) {
            player.displayClientMessage(Component.literal("请将未鉴定的装备放在副手").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(scroll);
        }

        if (target.getItem() instanceof IdentificationScrollItem) {
            player.displayClientMessage(Component.literal("无法鉴定鉴定卷轴").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(scroll);
        }

        boolean identifiedAny = false;

        if (AffixManager.hasAffixData(target) && !AffixManager.isIdentified(target)) {
            AffixManager.identify(target);
            identifiedAny = true;
        }

        if (target.getItem() instanceof ArmorItem) {
            if (isPiecePendingSetIdentification(player, target)) {
                ThirdPartyArmorSetResolver.markPieceIdentified(target);
                identifiedAny = true;
            }
        }

        if (!identifiedAny) {
            player.displayClientMessage(Component.literal("该装备已完全鉴定").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(scroll);
        }

        if (!player.getAbilities().instabuild) scroll.shrink(1);

        playIdentifyEffect(level, player);
        player.displayClientMessage(Component.literal("已鉴定：" + target.getHoverName().getString()).withStyle(ChatFormatting.GREEN), true);
        return InteractionResultHolder.consume(scroll);
    }

    private static boolean isPiecePendingSetIdentification(Player player, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) return false;
        if (ThirdPartyArmorSetResolver.isPieceIdentified(stack)) return false;

        String groupKey = null;
        if (stack.getItem() instanceof IArmorSetProvider provider) {
            groupKey = provider.epicreset$getArmorSetId();
        }
        if (groupKey == null) {
            Optional<ThirdPartyArmorSetResolver.SetInfo> infoOpt = ThirdPartyArmorSetResolver.getSetInfo(stack, player);
            if (infoOpt.isPresent()) groupKey = infoOpt.get().groupKey();
        }
        if (groupKey == null) return false;

        ThirdPartyArmorSetResolver.TierInfo tierInfo = ThirdPartyArmorSetResolver.getTierInfo(groupKey, ArmorSetManager.getInstance());
        if (tierInfo == null) return false;

        return ThirdPartyArmorSetResolver.requiresIdentification(tierInfo.tier());
    }

    private static void playIdentifyEffect(Level level, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.1f);
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.8, 0.6, 0.1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.epic-reset.identification_scroll.tooltip").withStyle(ChatFormatting.GRAY));
    }
}