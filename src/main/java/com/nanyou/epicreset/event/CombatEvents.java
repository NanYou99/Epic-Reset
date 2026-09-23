package com.nanyou.epicreset.event;

import com.nanyou.epicreset.EpicReset;
import com.nanyou.epicreset.affix.AffixManager;
import com.nanyou.epicreset.armorset.ArmorSetApplier;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import com.nanyou.epicreset.armorset.stat.StatAccumulator;
import com.nanyou.epicreset.util.DamageOverrideMap;
import com.nanyou.epicreset.util.PerPlayerTimerStore;
import com.nanyou.epicreset.weapon.interact.WeaponInteractManager;
import com.nanyou.epicreset.weapon.interact.WeaponInteractEntry;
import com.nanyou.epicreset.weapon.resonance.ResonanceEntry;
import com.nanyou.epicreset.weapon.resonance.ResonanceManager;
import com.nanyou.epicreset.weapon.resonance.ResonanceRegistry;
import com.nanyou.epicreset.weapon.resonance.WeaponType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import com.nanyou.epicreset.weapon.resonance.MaterialKey;

import java.util.Map;
import java.util.UUID;

public final class CombatEvents {

    private static final UUID PIERCE_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final double MAX_CRIT_RATE = 0.6;
    private static final double BASE_CRIT_MULT = 1.5;

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(CombatEvents::onAllowDamage);
        // 1.20.1 Fabric API 没有 AFTER_DAMAGE 事件，需要 Mixin 调用 triggerAfterDamage
    }

    private static boolean onAllowDamage(LivingEntity target, DamageSource source, float baseAmount) {
        if (source.getEntity() instanceof ServerPlayer attacker) {
            if (!(target instanceof ServerPlayer)) {
                try {
                    float finalDamage = applyAttackModifiers(attacker, target, source, baseAmount);
                    if (finalDamage <= 0f) return false;
                    setAmount(target, source, finalDamage);
                } catch (Exception e) {
                    EpicReset.LOGGER.error("战斗计算错误", e);
                }
            }
        }
        if (target instanceof ServerPlayer player) {
            try {
                applyDefenseModifiers(player, source, baseAmount);
            } catch (Exception e) {
                EpicReset.LOGGER.error("防御计算错误", e);
            }
        }
        return true;
    }

    private static boolean isVanillaCrit(ServerPlayer player) {
        return player.fallDistance > 0.0F
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger()
                && !player.isSprinting();
    }

    private static float applyAttackModifiers(ServerPlayer attacker, LivingEntity target, DamageSource source, float dmg) {
        ItemStack mainHand = attacker.getMainHandItem();
        WeaponType wt = WeaponType.OTHER;
        Item item = mainHand.isEmpty() ? null : mainHand.getItem();

        StatAccumulator acc = new StatAccumulator();
        acc.merge(ServerCombatHandler.collectAllStatsPublic(attacker));
        acc.merge(AffixManager.collectArmorMods(attacker));

        ResonanceEntry re = ResonanceManager.getInstance().getActive(attacker);
        if (re != null) {
            if (re.mods() != null) acc.merge(re.mods());
            if (re.extraDynamicMods() != null) {
                Map<StatCategory, Double> extra = re.extraDynamicMods().apply(attacker);
                if (extra != null) acc.merge(extra);
            }
        }

        if (item != null) {
            acc.merge(AffixManager.collectWeaponMods(mainHand));
            wt = ResonanceManager.weaponTypeOf(item);
            if (item instanceof SwordItem) {
                acc.merge(ResonanceRegistry.intrinsicSword(item));
            } else if (item instanceof AxeItem) {
                acc.merge(ResonanceRegistry.intrinsicAxe(item));
            } else {
                acc.merge(WeaponInteractManager.getInstance().getIntrinsicMods(wt));
                acc.merge(WeaponInteractManager.getInstance().getAllInteractMods(attacker, wt));
            }
        }

        applySetDynamicMods(attacker, acc);
        Map<StatCategory, Double> factors = acc.mulReduce();

        boolean isDirect = source.getDirectEntity() != null;
        boolean isVanillaMelee = (wt == WeaponType.SWORD || wt == WeaponType.AXE || wt == WeaponType.MACE
                || (wt == WeaponType.TRIDENT && isDirect));
        boolean isVanillaRanged = (wt == WeaponType.BOW || wt == WeaponType.CROSSBOW
                || (wt == WeaponType.TRIDENT && !isDirect));
        boolean isOther = !isVanillaMelee && !isVanillaRanged;
        boolean melee = isVanillaMelee || (isOther && isDirect);
        boolean ranged = isVanillaRanged || (isOther && !isDirect);
        boolean vanillaCrit = isVanillaCrit(attacker);

        if (!vanillaCrit && factors.containsKey(StatCategory.CRIT_RATE)) {
            double critRate = Math.max(0, Math.min(MAX_CRIT_RATE, factors.get(StatCategory.CRIT_RATE)));
            if (attacker.getRandom().nextFloat() < critRate) {
                double critMult = BASE_CRIT_MULT;
                if (factors.containsKey(StatCategory.CRIT_DMG)) {
                    double dmgBonus = factors.get(StatCategory.CRIT_DMG);
                    if (dmgBonus > critMult) critMult = dmgBonus;
                }
                dmg *= (float) critMult;
                if (item instanceof SwordItem || item instanceof AxeItem) {
                    triggerCritLifeStealIfGold(attacker);
                }
            }
        }

        if (factors.containsKey(StatCategory.ARMOR_PIERCE)) {
            applyPierce(target, Math.min(0.6, factors.get(StatCategory.ARMOR_PIERCE)));
        }

        if (melee && factors.containsKey(StatCategory.MELEE_DMG)) {
            double mul = factors.get(StatCategory.MELEE_DMG);
            if (mul != 1d) dmg *= (float) Math.max(0.1, mul);
        }
        if (ranged && factors.containsKey(StatCategory.RANGED_DMG)) {
            double mul = factors.get(StatCategory.RANGED_DMG);
            if (mul != 1d) dmg *= (float) Math.max(0.1, mul);
        }

        if (re != null && re.onHit() != null) re.onHit().accept(attacker, target);

        if (factors.containsKey(StatCategory.LIFE_STEAL)) {
            double ls = Math.max(0, Math.min(0.5, factors.get(StatCategory.LIFE_STEAL)));
            double healBonus = Math.max(1d, factors.getOrDefault(StatCategory.HEAL_POWER, 1d));
            if (ls > 0.0001) {
                float healed = Math.min(attacker.getMaxHealth() - attacker.getHealth(),
                        dmg * (float) ls * (float) healBonus);
                if (healed > 0.01f) attacker.heal(healed);
            }
        }

        if (factors.containsKey(StatCategory.BURN_DMG)) {
            double burnFactor = factors.get(StatCategory.BURN_DMG);
            if (burnFactor > 1.001) dmg += (float) ((burnFactor - 1.0) * 100);
        }

        if (factors.containsKey(StatCategory.DOT_BLEED)) {
            double bleedFactor = factors.get(StatCategory.DOT_BLEED);
            if (bleedFactor > 1.001) {
                int amplifier = Math.max(0, (int) (((bleedFactor - 1.0) * 100) / 2));
                MobEffectInstance existing = target.getEffect(MobEffects.WITHER);
                if (existing == null || existing.getDuration() < 60) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amplifier, false, false, true));
                }
            }
        }

        if (factors.containsKey(StatCategory.KNOCKBACK_POWER)) {
            double kb = Math.min(0.5, factors.get(StatCategory.KNOCKBACK_POWER));
            if (kb > 0) target.knockback((float) kb, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        }

        return Math.max(0f, dmg);
    }

    private static void applySetDynamicMods(ServerPlayer attacker, StatAccumulator acc) {
        float hpPct = attacker.getHealth() / Math.max(0.0001f, attacker.getMaxHealth());
        ItemStack m = attacker.getItemBySlot(EquipmentSlot.MAINHAND);
        MaterialKey mat = ArmorSetApplier.getInstance().getFullArmorMaterial(attacker);

        if (mat == MaterialKey.IRON && hpPct < 0.3f) {
            acc.add(StatCategory.MELEE_DMG, 0.08);
            if (!m.isEmpty() && m.getItem() == Items.IRON_SWORD) acc.add(StatCategory.MELEE_DMG, 0.05);
            if (!m.isEmpty() && m.getItem() == Items.IRON_AXE) acc.add(StatCategory.ARMOR_PIERCE, 0.04);
        }
        if (mat == MaterialKey.NETHERITE && hpPct > 0.7f) {
            boolean netheriteSword = !m.isEmpty() && m.getItem() == Items.NETHERITE_SWORD;
            acc.add(StatCategory.MELEE_DMG, netheriteSword ? 0.18 : 0.10);
        }
    }

    private static void applyDefenseModifiers(ServerPlayer player, DamageSource source, float amount) {
        StatAccumulator armorAccumulator = new StatAccumulator();
        armorAccumulator.merge(ArmorSetApplier.getInstance().collectArmorMods(player));
        armorAccumulator.merge(AffixManager.collectArmorMods(player));
        Map<StatCategory, Double> armor = armorAccumulator.mulReduce();
        MaterialKey mat = ArmorSetApplier.getInstance().getFullArmorMaterial(player);

        double reduce = armor.getOrDefault(StatCategory.DAMAGE_REDUCE, 0d);
        if (mat == MaterialKey.DIAMOND && amount > player.getMaxHealth() * 0.2f
                && !PerPlayerTimerStore.cooldownPassed(player, "epicreset:lastDiamondBigHitTick", 20)) {
            reduce = 1d - (1d - reduce) * 0.85d;
            PerPlayerTimerStore.writeLast(player, "epicreset:lastDiamondBigHitTick");
        }
        if (mat == MaterialKey.LEATHER && player.getRandom().nextFloat() < 0.08
                && !PerPlayerTimerStore.cooldownPassed(player, "epicreset:lastLeatherBootsMoveTick", 60)) {
            ensureEffect(player, MobEffects.MOVEMENT_SPEED, 80, 1);
            PerPlayerTimerStore.writeLast(player, "epicreset:lastLeatherBootsMoveTick");
        } else if (mat == MaterialKey.LEATHER && player.getRandom().nextFloat() < 0.15 && amount > 0.5f
                && !PerPlayerTimerStore.cooldownPassed(player, "epicreset:lastLeatherFourMoveTick", 80)) {
            ensureEffect(player, MobEffects.MOVEMENT_SPEED, 100, 1);
            PerPlayerTimerStore.writeLast(player, "epicreset:lastLeatherFourMoveTick");
        }
        if (reduce > 0) {
            setAmount(player, source, amount * (float) (1d - Math.min(0.95, reduce)));
        }
    }

    // 需要 Mixin 调用此方法（注入 LivingEntity#actuallyHurt 尾部）
    public static void triggerAfterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        if (entity instanceof ServerPlayer p && !entity.isAlive()) {
            if (source.getEntity() instanceof ServerPlayer killer) {
                MaterialKey mat = ArmorSetApplier.getInstance().getFullArmorMaterial(killer);
                if (mat == MaterialKey.GOLDEN
                        && !PerPlayerTimerStore.cooldownPassed(killer, "epicreset:lastGoldKillLifeStealTick", 20)) {
                    ensureEffect(killer, MobEffects.REGENERATION, 100, 0);
                    float healed = Math.min(killer.getMaxHealth() - killer.getHealth(), baseDamageTaken * 0.05f);
                    if (healed > 0.01f) killer.heal(healed);
                    PerPlayerTimerStore.writeLast(killer, "epicreset:lastGoldKillLifeStealTick");
                }
            }
        }
        if (source.getEntity() instanceof ServerPlayer attacker) {
            if (!(entity instanceof ServerPlayer)) {
                LivingEntity target = entity;
                ResonanceEntry re = ResonanceManager.getInstance().getActive(attacker);
                if (re != null && re.onHit() != null) {
                    try { re.onHit().accept(attacker, target); } catch (Exception ignore) {}
                }
                WeaponType type = WeaponInteractManager.weaponTypeOfStack(attacker.getMainHandItem());
                WeaponInteractEntry interact = WeaponInteractManager.getInstance().getActiveInteract(attacker, type);
                if (interact != null) {
                    try {
                        interact.onHit().accept(attacker, target);
                        if (type == WeaponType.MACE && isMaceSmash(source)) {
                            interact.onChargedHit().accept(attacker, target);
                        }
                    } catch (Exception e) {
                        EpicReset.LOGGER.error("武器套装互动执行错误", e);
                    }
                }
            }
        }
    }

    private static void triggerCritLifeStealIfGold(ServerPlayer player) {
        MaterialKey full = ArmorSetApplier.getInstance().getFullArmorMaterial(player);
        if (full == MaterialKey.GOLDEN && player.getMainHandItem().getItem() == Items.GOLDEN_SWORD) {
            if (PerPlayerTimerStore.cooldownPassed(player, "epicreset:lastGoldCritStealTick", 30)) return;
            ensureEffect(player, MobEffects.REGENERATION, 60, 1);
            PerPlayerTimerStore.writeLast(player, "epicreset:lastGoldCritStealTick");
        }
    }

    private static boolean isMaceSmash(DamageSource source) {
        return "mace_smash".equals(source.getMsgId());
    }

    private static void applyPierce(LivingEntity target, double ratio) {
        var inst = target.getAttribute(Attributes.ARMOR);
        if (inst == null) return;
        inst.removeModifier(PIERCE_UUID);
        double base = inst.getBaseValue();
        double reduce = -base * Math.min(1d, Math.max(0, ratio));
        if (reduce == 0d) return;
        AttributeModifier mod = new AttributeModifier(PIERCE_UUID, "epicreset_armor_pierce", reduce, AttributeModifier.Operation.ADDITION);
        inst.addTransientModifier(mod);
        MinecraftServer srv = target.getServer();
        if (srv != null) srv.execute(() -> inst.removeModifier(PIERCE_UUID));
    }

    private static void ensureEffect(ServerPlayer p, MobEffect eff, int dur, int amp) {
        MobEffectInstance cur = p.getEffect(eff);
        if (cur == null || cur.getDuration() < 10 || cur.getAmplifier() < amp) {
            p.addEffect(new MobEffectInstance(eff, dur, amp, false, false, true));
        }
    }

    private static void setAmount(LivingEntity entity, DamageSource source, float amount) {
        DamageOverrideMap.setOverride(entity, Math.max(0f, amount));
    }
}