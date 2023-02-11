package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.damage.DamageSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;


public class HeartstopEffect extends MobEffect {
    public HeartstopEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);

            playerMagicData.getSyncedData().setHasHeartstop(false);

            //Whether or not player has spawn immunity (we want to damage them regardless)
            if (serverPlayer.tickCount > 60) {
                serverPlayer.hurt(DamageSources.HEARTSTOP, playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());
                //TestMod.LOGGER.debug("{} had no spawn immunity", pLivingEntity.getName().getString());

            } else {
                //TODO: find a better way to apply damage
                serverPlayer.kill();
//                serverPlayer.setHealth(serverPlayer.getHealth() - playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());

                //TestMod.LOGGER.debug("{} had spawn immunity", pLivingEntity.getName().getString());

            }
            playerMagicData.getSyncedData().setHeartstopAccumulatedDamage(0);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        //TestMod.LOGGER.debug("{} ticks existed: {}", pLivingEntity.getName().getString(), pLivingEntity.tickCount);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasHeartstop(true);
        }
    }

}
