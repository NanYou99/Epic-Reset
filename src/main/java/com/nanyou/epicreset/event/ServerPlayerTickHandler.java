package com.nanyou.epicreset.event;

import com.nanyou.epicreset.armorset.ArmorSet;
import com.nanyou.epicreset.armorset.ArmorSetManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * 服务端玩家Tick事件
 * 负责：
 * 1. 每 tick 计算玩家当前穿戴各套装的数量
 * 2. 根据 2/4 件激活规则调用套装效果（全部在服务端执行，保证联机同步）
 * 规则：
 *  1件 → 无套装加成
 *  2/3件 → 只激活2件套
 *  4件 → 2件套 + 4件套 叠加生效
 */
public final class ServerPlayerTickHandler {

    private ServerPlayerTickHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                try {
                    tickPlayer(player);
                } catch (Exception e) {
                    com.nanyou.epicreset.EpicReset.LOGGER.error(
                            "套装tick出错：玩家 {} - {}", player.getName().getString(), e.getMessage());
                }
            }
        });
    }

    private static void tickPlayer(ServerPlayer player) {
        if (player.isDeadOrDying() || player.isSpectator()) return;
        ArmorSetManager mgr = ArmorSetManager.getInstance();
        var wornItems = mgr.getWornArmorItems(player);

        for (ArmorSet set : mgr.getAllSets()) {
            int equipped = 0;
            for (var item : wornItems) {
                if (set.containsItem(item)) equipped++;
            }
            if (equipped >= 2) {
                safeApply(set.getTwoPieceEffect(), player, equipped);
                if (equipped >= 4) {
                    safeApply(set.getFourPieceEffect(), player, 4);
                }
            }
        }
    }

    private static void safeApply(com.nanyou.epicreset.armorset.api.ArmorSetEffect effect,
                                  ServerPlayer player, int count) {
        try {
            effect.apply(player, count);
        } catch (Exception e) {
            com.nanyou.epicreset.EpicReset.LOGGER.error("套装效果执行异常: {}", e.getMessage());
        }
    }
}
