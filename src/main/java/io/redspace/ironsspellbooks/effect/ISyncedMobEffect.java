package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Enables mob effect to be synced to all players, not just the affected player.
 * <br>
 * Enables advanced client-specific state tracking and effects
 */
public interface ISyncedMobEffect {

    default void clientTick(LivingEntity livingEntity, MobEffectInstance instance){}
}
