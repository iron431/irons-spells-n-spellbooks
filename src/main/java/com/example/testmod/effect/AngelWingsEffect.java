package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AngelWingsEffect extends MobEffect {

    private LivingEntity livingEntity;

    public AngelWingsEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasAngelWings(false);
        }
    }
}
