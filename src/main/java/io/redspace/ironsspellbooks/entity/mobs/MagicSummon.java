package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.effect.SummonTimer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface MagicSummon extends AntiMagicSusceptible {

    LivingEntity getSummoner();

    void onUnSummon();

    @Override
    default void onAntiMagic(MagicData playerMagicData) {
        onUnSummon();
    }

    default boolean shouldIgnoreDamage(DamageSource damageSource) {
        if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (damageSource.getEntity() != null && !ServerConfigs.CAN_ATTACK_OWN_SUMMONS.get())
                return !(getSummoner() == null || damageSource.getEntity() == null || (!damageSource.getEntity().equals(getSummoner()) && !getSummoner().isAlliedTo(damageSource.getEntity())));
        }
        return false;
    }

    default boolean isAlliedHelper(Entity entity) {
        if (getSummoner() == null)
            return false;
        boolean isFellowSummon = entity == getSummoner() || entity.isAlliedTo(getSummoner());
        boolean hasCommonOwner = entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() == getSummoner();
        return isFellowSummon || hasCommonOwner;
    }

    default void onDeathHelper() {
        if (this instanceof LivingEntity entity) {
            Level level = entity.level();
            var deathMessage = entity.getCombatTracker().getDeathMessage();

            if (!level.isClientSide && level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && getSummoner() instanceof ServerPlayer player) {
                player.sendSystemMessage(deathMessage);
            }
        }
    }

    default void onRemovedHelper(Entity entity, DeferredHolder<MobEffect, SummonTimer> holder) {
        /*
        Decreases player's summon timer amplifier to keep track of how many of their summons remain.
        */
        var reason = entity.getRemovalReason();
        if (reason != null && getSummoner() instanceof ServerPlayer player && reason.shouldDestroy()) {
            var effect = player.getEffect(holder);
            if (effect != null) {
                var decrement = new MobEffectInstance(holder, effect.getDuration(), effect.getAmplifier() - 1, false, false, true);
                if (decrement.getAmplifier() >= 0) {
                    player.getActiveEffectsMap().put(holder, decrement);
                    player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), decrement, false));
                } else {
                    player.removeEffect(holder);
                }
            }
            if (reason.equals(Entity.RemovalReason.DISCARDED))
                player.sendSystemMessage(Component.translatable("ui.irons_spellbooks.summon_despawn_message", ((Entity) this).getDisplayName()));

        }
    }
}
