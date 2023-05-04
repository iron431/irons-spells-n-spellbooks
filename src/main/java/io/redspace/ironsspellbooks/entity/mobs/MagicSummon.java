package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface MagicSummon extends AntiMagicSusceptible {

    LivingEntity getSummoner();

    void onUnSummon();

    @Override
    default void onAntiMagic(PlayerMagicData playerMagicData) {
        onUnSummon();
    }

    default boolean shouldIgnoreDamage(DamageSource damageSource) {
        if (!damageSource.isBypassInvul()) {
            if (damageSource instanceof EntityDamageSource && !ServerConfigs.CAN_ATTACK_OWN_SUMMONS.get())
                return !(getSummoner() == null || damageSource.getEntity() == null || (!damageSource.getEntity().equals(getSummoner()) && !getSummoner().isAlliedTo(damageSource.getEntity())));
        }
        return false;
    }

    default boolean isAlliedHelper(Entity entity) {
        return getSummoner() != null && (entity == getSummoner() || entity.isAlliedTo(getSummoner()));
    }

    default void onDeathHelper(){
        if(this instanceof LivingEntity entity){
            Level level = entity.level;
            var deathMessage = entity.getCombatTracker().getDeathMessage();

            if (!level.isClientSide && level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && getSummoner() instanceof ServerPlayer player) {
                player.sendSystemMessage(deathMessage);
            }
        }
    }
}
