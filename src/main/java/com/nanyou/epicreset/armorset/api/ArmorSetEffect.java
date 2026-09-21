package com.nanyou.epicreset.armorset.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * 套装效果函数式接口
 * 在服务端执行，确保多人联机安全
 * @param <T> 上下文参数类型
 */
@FunctionalInterface
public interface ArmorSetEffect {
    /**
     * 执行套装效果
     * @param player 目标玩家（服务端）
     * @param equippedCount 已穿戴数量（2或4）
     */
    void apply(ServerPlayer player, int equippedCount);
}
