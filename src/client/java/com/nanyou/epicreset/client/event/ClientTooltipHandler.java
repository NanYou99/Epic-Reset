package com.nanyou.epicreset.client.event;

import com.nanyou.epicreset.affix.AffixManager;
import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.ArmorSetManager;
import com.nanyou.epicreset.armorset.ThirdPartyArmorSetResolver;
import com.nanyou.epicreset.armorset.ThirdPartyArmorTier;
import com.nanyou.epicreset.armorset.api.IArmorSetProvider;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientTooltipHandler {

    private ClientTooltipHandler() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            Player player = net.minecraft.client.Minecraft.getInstance().player;
            appendAffixTooltip(stack, lines);
            if (stack.getItem() instanceof ArmorItem
                    && stack.getItem() instanceof IArmorSetProvider provider
                    && appendArmorTooltip(stack, player, provider, lines)) {
                return;
            }
            appendThirdPartyArmorTooltip(stack, player, lines);
        });
    }

    private static void appendAffixTooltip(ItemStack stack, List<Component> lines) {
        if (!AffixManager.hasAffixData(stack)) return;
        List<AffixManager.AffixInstance> affixes = AffixManager.getAffixes(stack);

        lines.add(Component.empty());
        lines.add(Component.translatable("affix.epic-reset.header").withStyle(ChatFormatting.GOLD));
        if (!AffixManager.isIdentified(stack)) {
            lines.add(Component.translatable("affix.epic-reset.unidentified").withStyle(ChatFormatting.DARK_PURPLE));
        }

        ChatFormatting color;
        if (!AffixManager.isIdentified(stack)) {
            color = ChatFormatting.DARK_GRAY;
        } else {
            color = switch (affixes.size()) {
                case 3 -> ChatFormatting.GOLD;
                case 2 -> ChatFormatting.BLUE;
                default -> ChatFormatting.WHITE;
            };
        }

        for (AffixManager.AffixInstance instance : affixes) {
            String percent = String.format(Locale.ROOT, "%.1f", instance.value() * 100d);
            lines.add(Component.literal("  ").append(Component.translatable(instance.definition().translationKey(), percent)
                    .withStyle(color)));
        }
        if (!AffixManager.isIdentified(stack)) {
            lines.add(Component.translatable("affix.epic-reset.identify_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static boolean appendArmorTooltip(ItemStack stack, Player player, IArmorSetProvider provider, List<Component> lines) {
        String setId = provider.epicreset$getArmorSetId();
        if (setId == null) return false;
        ArmorSet set = ArmorSetManager.getInstance().getSet(setId);
        if (set == null) return false;

        int equipped = countEquipped(player, set);
        ThirdPartyArmorSetResolver.TierInfo tierInfo = ThirdPartyArmorSetResolver.getTierInfo(setId, ArmorSetManager.getInstance());
        if (tierInfo.totalPieces() < 2) return true;

        ThirdPartyArmorTier tier = tierInfo.tier();
        TextColor tierColor = tier.getColor();
        boolean needsId = ThirdPartyArmorSetResolver.requiresIdentification(tier);
        boolean thisPieceIdentified = ThirdPartyArmorSetResolver.isPieceIdentified(stack);

        MutableComponent title = Component.translatable(set.getTranslationKey())
                .append(Component.literal(" · "))
                .append(Component.translatable(tier.getTranslationKey()))
                .withStyle(style -> style.withColor(tierColor));

        MutableComponent suffix = Component.literal(" (")
                .append(Component.literal(equipped + "/" + tierInfo.totalPieces()))
                .append(Component.literal(")"))
                .withStyle(style -> style.withColor(tierColor));

        lines.add(Component.empty());
        lines.add(title.append(suffix));

        if (needsId && !thisPieceIdentified) {
            lines.add(Component.literal("  ⚠ 未鉴定 - 使用鉴定卷轴激活套装效果")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }

        appendSetPieceList(lines, player, tierInfo.slotPieces(), tierColor);

        if (tierInfo.totalPieces() >= 2 || tierInfo.totalPieces() >= 4) {
            boolean showAsActive = !needsId || thisPieceIdentified;
            if (tierInfo.totalPieces() >= 2) appendDynamicEffects(lines, showAsActive && equipped >= 2, false, tierInfo.twoPieceMods(), tierColor);
            if (tierInfo.totalPieces() >= 4) appendDynamicEffects(lines, showAsActive && equipped >= 4, true, tierInfo.fourPieceMods(), tierColor);
        }
        return true;
    }

    private static void appendThirdPartyArmorTooltip(ItemStack stack, Player player, List<Component> lines) {
        ThirdPartyArmorSetResolver.getSetInfo(stack, player).ifPresent(info -> {
            if (info.totalPieces() < 2) return;

            String groupName = getReadableName(info.groupKey(), stack);
            TextColor tierColor = info.tier().getColor();
            boolean needsId = ThirdPartyArmorSetResolver.requiresIdentification(info.tier());
            boolean thisPieceIdentified = ThirdPartyArmorSetResolver.isPieceIdentified(stack);

            MutableComponent title = Component.literal(groupName + " · ")
                    .append(Component.translatable(info.tier().getTranslationKey()))
                    .withStyle(style -> style.withColor(tierColor));

            MutableComponent suffix = Component.literal(" (")
                    .append(Component.literal(info.equipped() + "/" + info.totalPieces()))
                    .append(Component.literal(")"))
                    .withStyle(style -> style.withColor(tierColor));

            lines.add(Component.empty());
            lines.add(title.append(suffix));

            if (needsId && !thisPieceIdentified) {
                lines.add(Component.literal("  ⚠ 未鉴定 - 使用鉴定卷轴激活套装效果")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }

            appendSetPieceList(lines, player, info.slotPieces(), tierColor);

            if (info.totalPieces() >= 2 || info.totalPieces() >= 4) {
                boolean showAsActive = !needsId || thisPieceIdentified;
                if (info.totalPieces() >= 2) appendDynamicEffects(lines, showAsActive && info.equipped() >= 2, false, info.twoPieceMods(), tierColor);
                if (info.totalPieces() >= 4) appendDynamicEffects(lines, showAsActive && info.equipped() >= 4, true, info.fourPieceMods(), tierColor);
            }
        });
    }

    private static void appendSetPieceList(List<Component> lines, Player player, Map<EquipmentSlot, Item> slotPieces, TextColor tierColor) {
        if (slotPieces == null || slotPieces.isEmpty()) return;
        for (Map.Entry<EquipmentSlot, Item> entry : slotPieces.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            Item requiredItem = entry.getValue();
            ItemStack wornItem = player.getItemBySlot(slot);
            boolean isEquipped = !wornItem.isEmpty() && wornItem.getItem() == requiredItem;

            String itemName = requiredItem.getDescription().getString();

            if (isEquipped) {
                lines.add(Component.literal("  " + itemName).withStyle(style -> style.withColor(tierColor)));
            } else {
                lines.add(Component.literal("  " + itemName).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static void appendDynamicEffects(List<Component> lines, boolean active, boolean fourPiece, Map<StatCategory, Double> mods, TextColor tierColor) {
        if (mods == null || mods.isEmpty()) return;

        MutableComponent mark = Component.translatable(fourPiece ? "armorset.tooltip.four_piece_mark" : "armorset.tooltip.two_piece_mark");
        MutableComponent desc = Component.literal("");
        boolean first = true;

        if (active) {
            mark.withStyle(style -> style.withColor(tierColor));
            for (Map.Entry<StatCategory, Double> entry : mods.entrySet()) {
                if (!first) desc.append(Component.literal("，"));
                first = false;
                desc.append(formatStat(entry.getKey(), entry.getValue(), tierColor));
            }
        } else {
            mark.withStyle(ChatFormatting.DARK_GRAY);
            TextColor darkGrayColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY);
            for (Map.Entry<StatCategory, Double> entry : mods.entrySet()) {
                if (!first) desc.append(Component.literal("，"));
                first = false;
                desc.append(formatStat(entry.getKey(), entry.getValue(), darkGrayColor));
            }
        }
        lines.add(Component.literal("  ").append(mark).append(desc));
    }

    private static MutableComponent formatStat(StatCategory cat, double value, TextColor color) {
        String statName = I18n.get("stat.epic-reset." + cat.name().toLowerCase(Locale.ROOT));
        String valStr;
        if (cat == StatCategory.MAX_HEALTH || cat == StatCategory.ARMOR) {
            valStr = "+" + (int)value;
        } else if (cat == StatCategory.ATTACK_RANGE) {
            valStr = "+" + String.format(Locale.ROOT, "%.2f", value) + " 格";
        } else if (cat == StatCategory.KNOCKBACK_POWER) {
            valStr = "+" + String.format(Locale.ROOT, "%.2f", value);
        } else {
            valStr = "+" + String.format(Locale.ROOT, "%.1f", value * 100) + "%";
        }
        return Component.literal(statName + " " + valStr).withStyle(style -> style.withColor(color));
    }

    private static String getReadableName(String groupKey, ItemStack stack) {
        if (groupKey == null || !groupKey.contains(":")) return "未知套装";
        String path = groupKey.substring(groupKey.indexOf(':') + 1);
        String specificKey = "armorset.third_party.name." + path;
        String specificName = I18n.get(specificKey);
        if (!specificName.equals(specificKey)) return specificName;

        if (stack != null && !stack.isEmpty()) {
            String itemName = stack.getHoverName().getString();
            String baseName = stripArmorNameSuffix(itemName);
            if (!baseName.isEmpty() && !baseName.equals(itemName)) return baseName + "套装";
        }

        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            String lowerWord = word.toLowerCase(Locale.ROOT);
            String translationKey = "armorset.third_party.word." + lowerWord;
            String translated = I18n.get(translationKey);
            if (translated.equals(translationKey)) {
                translated = word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
            }
            sb.append(translated);
        }
        return sb.toString() + "套装";
    }

    private static String stripArmorNameSuffix(String name) {
        if (name == null || name.isEmpty()) return name;
        String[] cnSuffixes = {
                "头盔", "头冠", "头巾", "帽子", "兜帽", "头饰", "面具", "面罩", "头罩", "角盔", "风帽", "面纱", "头环", "冠冕", "护目镜", "兜帽领", "帽",
                "胸甲", "外套", "上衣", "战袍", "长袍", "胸铠", "铠甲", "夹克", "皮衣", "大衣", "衬衣", "护胸", "马甲", "法衣", "法袍", "外衣", "袍子", "长袍（下半身）", "胸甲",
                "护腿", "腿甲", "裤子", "长裤", "短裤", "护膝", "腿铠", "裙甲", "战裙", "之翼", "翅膀", "下装",
                "靴子", "鞋子", "战靴", "皮靴", "长靴", "短靴", "马靴", "脚甲", "足甲", "便鞋"
        };
        for (String suffix : cnSuffixes) {
            if (name.endsWith(suffix) && name.length() > suffix.length()) {
                return name.substring(0, name.length() - suffix.length());
            }
        }
        String lower = name.toLowerCase(Locale.ROOT);
        String[] enSuffixes = {" helmet", " headpiece", " hat", " hood", " goggles", " chestplate", " chestpiece", " tunic", " robe", " vest", " leggings", " legplates", " pants", " wings", " boots", " shoes", " head", " chest", " legs", " feet"};
        for (String suffix : enSuffixes) {
            if (lower.endsWith(suffix)) {
                return name.substring(0, name.length() - suffix.length());
            }
        }
        return name;
    }

    private static int countEquipped(Player player, ArmorSet set) {
        if (player == null) return 0;
        int count = 0;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack s = player.getItemBySlot(slot);
            if (!s.isEmpty() && set.containsItem(s.getItem())) count++;
        }
        return count;
    }
}