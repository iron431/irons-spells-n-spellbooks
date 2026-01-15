package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.worldgen.IceSpiderPatrolSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.EventBusSubscriber;


public class IceSpiderLureEffect extends MobEffect implements IMobEffectEndCallback {

    public IceSpiderLureEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level instanceof ServerLevel serverLevel) {
            IceSpiderPatrolSpawner.performIceSpiderHuntSpawn(serverLevel, pLivingEntity, 8);
        }
    }
}
