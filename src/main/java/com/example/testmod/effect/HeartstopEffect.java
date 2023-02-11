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
            serverPlayer.hurt(DamageSources.HEARTSTOP, playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());
            playerMagicData.getSyncedData().setHeartstopAccumulatedDamage(0);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasHeartstop(true);
        }
    }

}
