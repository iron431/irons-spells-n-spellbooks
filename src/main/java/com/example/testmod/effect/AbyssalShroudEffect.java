package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.registries.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public class AbyssalShroudEffect extends MobEffect {

    public AbyssalShroudEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().removeEffects(SyncedSpellData.ABYSSAL_SHROUD);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().addEffects(SyncedSpellData.ABYSSAL_SHROUD);
    }


    public static boolean doEffect(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity.level.isClientSide || EvasionEffect.excludeDamageSources.contains(damageSource) || damageSource.isFall() || damageSource.isBypassMagic() || damageSource.isBypassInvul()) {
            return false;
        }
        var random = livingEntity.getRandom();
        var level = livingEntity.level;


        Vec3 sideStep = new Vec3(random.nextBoolean() ? 1 : -1, 0, -.25);
        sideStep.yRot(livingEntity.getYRot());

        particleCloud(livingEntity);

        Vec3 ground = livingEntity.position().add(sideStep);
        ground = level.clip(new ClipContext(ground.add(0, 3.5, 0), ground.add(0, -3.5, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null)).getLocation();

        if (livingEntity.isPassenger()) {
            livingEntity.stopRiding();
        }
        if (!level.getBlockState(new BlockPos(ground).below()).isAir()) {
            livingEntity.teleportTo(ground.x, ground.y, ground.z);
            particleCloud(livingEntity);
        }
        if (damageSource.getEntity() != null) {
            livingEntity.lookAt(EntityAnchorArgument.Anchor.EYES, damageSource.getEntity().getEyePosition().subtract(0, .15, 0));
        }
        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundRegistry.ABYSSAL_TELEPORT.get(), SoundSource.AMBIENT, 1.0F, .9F + random.nextFloat() * .2f);
        return true;
    }

    private static void particleCloud(LivingEntity entity) {
        Vec3 pos = entity.position().add(0, entity.getBbHeight() / 2, 0);
        MagicManager.spawnParticles(entity.level, ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 70, entity.getBbWidth() / 4, entity.getBbHeight() / 5, entity.getBbWidth() / 4, .035, false);
    }

    public static void ambientParticles(ClientLevel level, LivingEntity entity) {
        Vec3 backwards = entity.getForward().scale(.003).reverse().add(0, 0.02, 0);
        var random = entity.getRandom();
        for (int i = 0; i < 2; i++) {
            Vec3 motion = new Vec3(
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1
            );
            motion = motion.scale(.04f).add(backwards);
            level.addParticle(ParticleTypes.SMOKE, entity.getRandomX(.4f), entity.getRandomY(), entity.getRandomZ(.4f), motion.x, motion.y, motion.z);
        }
    }
}
