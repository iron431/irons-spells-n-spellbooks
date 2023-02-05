package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class EvasionEffect extends MobEffect {

    private LivingEntity livingEntity;

    public EvasionEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasAngelWings(false);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasAngelWings(true);
        }
    }

}
