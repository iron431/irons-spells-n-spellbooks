package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface IMagicSummon extends AntiMagicSusceptible {

    default Entity getSummoner() {
        return SummonManager.getOwner((Entity) this);
    }

    void onUnSummon();

    @Override
    default void onAntiMagic(MagicData playerMagicData) {
        onUnSummon();
    }

    default boolean shouldIgnoreDamage(DamageSource damageSource) {
        if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !ServerConfigs.CAN_ATTACK_OWN_SUMMONS.get() && damageSource.getEntity() != null) {
            return DamageSources.isFriendlyFireBetween(damageSource.getEntity(), (Entity) this);
        }
        return false;
    }

    default boolean isAlliedHelper(Entity entity) {
        var owner = getSummoner();
        if (owner == null) {
            return false;
        }
        if (entity instanceof IMagicSummon magicSummon) {
            var otherOwner = magicSummon.getSummoner();
            return otherOwner != null && (owner == otherOwner || otherOwner.isAlliedTo(otherOwner));
        } else if (entity instanceof OwnableEntity tamableAnimal) {
            var otherOwner = tamableAnimal.getOwner();
            return otherOwner != null && (owner == otherOwner || otherOwner.isAlliedTo(otherOwner));
        }
        return false;
    }

    default void onDeathHelper() {
        if (this instanceof LivingEntity entity) {
            Level level = entity.level;
            var deathMessage = entity.getCombatTracker().getDeathMessage();

            if (!level.isClientSide && level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && getSummoner() instanceof ServerPlayer player) {
                player.sendSystemMessage(deathMessage);
            }
        }
    }

    default void onRemovedHelper(Entity entity) {
        if (entity.level.isClientSide) {
            return;
        }
        var reason = entity.getRemovalReason();
        if (reason == null || reason == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
            // seems to cause undefined behavior, not entirely sure why. disabling for now, as im not sure what this was accomplishing anyways - summons are not persistent by default
            // Force unloaded summons to die
//            ((EntityAccessor) entity).setRemovalReason(Entity.RemovalReason.DISCARDED);
        }
        if (reason == Entity.RemovalReason.DISCARDED) {
            if (this.getSummoner() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ui.irons_spellbooks.summon_despawn_message", ((Entity) this).getDisplayName()));
            }
        }
        if (reason != null && reason.shouldDestroy()) {
            SummonManager.removeSummon(entity);
            SummonManager.stopTrackingExpiration(entity);
        }
    }
}
