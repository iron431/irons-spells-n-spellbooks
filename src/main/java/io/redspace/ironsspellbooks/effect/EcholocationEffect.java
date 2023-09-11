package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EcholocationEffect extends MobEffect {

    static {
        FogRenderer.MOB_EFFECT_FOG.add(new EcholocationBlindnessFogFunction());
    }

    public EcholocationEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        ClientSpellCastHelper.setHasEcholocation(true);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        ClientSpellCastHelper.setHasEcholocation(false);
    }

    @OnlyIn(Dist.CLIENT)
    static class EcholocationBlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
        public MobEffect getMobEffect() {
            //this doesnt do anything since we override isEnabled()
            return MobEffectRegistry.ECHOLOCATION.get();
        }

        @Override
        public boolean isEnabled(LivingEntity pEntity, float p_234207_) {
            return ClientSpellCastHelper.hasEcholocation();
        }

        public void setupFog(FogRenderer.FogData p_234181_, LivingEntity p_234182_, MobEffectInstance p_234183_, float p_234184_, float p_234185_) {
            float f = Mth.lerp(Math.min(1.0F, (float) p_234183_.getDuration() / 20.0F), p_234184_, 5.0F);
            if (p_234181_.mode == FogRenderer.FogMode.FOG_SKY) {
                p_234181_.start = 0.0F;
                p_234181_.end = f * 0.8F;
            } else {
                p_234181_.start = f * 0.25F;
                p_234181_.end = f;
            }
        }
    }
}