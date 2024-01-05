package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlanarSightEffect extends MagicMobEffect {
    public PlanarSightEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        MagicData.getPlayerMagicData(pLivingEntity).getSyncedData().removeEffects(SyncedSpellData.PLANAR_SIGHT);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        MagicData.getPlayerMagicData(pLivingEntity).getSyncedData().addEffects(SyncedSpellData.PLANAR_SIGHT);
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
        if (livingEntity.level.isClientSide && livingEntity == Minecraft.getInstance().player) {
            for (int i = 0; i < 3; i++) {
                Vec3 pos = new Vec3(Utils.getRandomScaled(16), Utils.getRandomScaled(5f) + 5, Utils.getRandomScaled(16)).add(livingEntity.position());
                Vec3 random = new Vec3(Utils.getRandomScaled(.08f), Utils.getRandomScaled(.08f), Utils.getRandomScaled(.08f));
                livingEntity.level.addParticle(ParticleTypes.WHITE_ASH, pos.x, pos.y, pos.z, random.x, random.y, random.z);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class EcholocationBlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
        public MobEffect getMobEffect() {
            return MobEffectRegistry.PLANAR_SIGHT.get();
        }

        public void setupFog(FogRenderer.FogData fogData, LivingEntity entity, MobEffectInstance mobEffectInstance, float p_234184_, float p_234185_) {
            float f = 160f;
            if (fogData.mode == FogRenderer.FogMode.FOG_SKY) {
                fogData.start = 0.0F;
                fogData.end = f * 0.25F;
            } else {
                fogData.start = -f * .5f;
                fogData.end = f;
            }
        }
    }
}