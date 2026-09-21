package com.nanyou.epicreset.weapon.interact;

import com.nanyou.epicreset.EpicReset;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public final class MaceInteractEffects {

    private static final ResourceLocation ARMOR_BREAK_ID = EpicReset.id("mace_armor_break");
    private static final Map<LivingEntity, Long> ARMOR_BREAK_EXPIRY = new WeakHashMap<>();
    private static boolean registered;

    private MaceInteractEffects() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<LivingEntity, Long>> iterator = ARMOR_BREAK_EXPIRY.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<LivingEntity, Long> entry = iterator.next();
                LivingEntity entity = entry.getKey();
                if (entity == null || !entity.isAlive() || entity.level().getGameTime() >= entry.getValue()) {
                    if (entity != null) {
                        var armor = entity.getAttribute(Attributes.ARMOR);
                        if (armor != null) armor.removeModifier(ARMOR_BREAK_ID);
                    }
                    iterator.remove();
                }
            }
        });
    }

    public static void applyArmorBreak(LivingEntity target, int durationTicks) {
        var armor = target.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        armor.removeModifier(ARMOR_BREAK_ID);
        armor.addTransientModifier(new AttributeModifier(ARMOR_BREAK_ID, -0.15d,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        ARMOR_BREAK_EXPIRY.put(target, target.level().getGameTime() + durationTicks);
    }

    public static void applyShockwave(ServerPlayer attacker, LivingEntity primaryTarget) {
        for (LivingEntity nearby : primaryTarget.level().getEntitiesOfClass(LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(3d),
                entity -> entity != attacker && entity != primaryTarget && entity.isAlive())) {
            nearby.knockback(0.65f, attacker.getX() - nearby.getX(), attacker.getZ() - nearby.getZ());
        }
    }
}
