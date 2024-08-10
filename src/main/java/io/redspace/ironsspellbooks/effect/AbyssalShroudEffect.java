package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.datagen.DamageTypeTagGenerator;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class AbyssalShroudEffect extends MagicMobEffect {

    public AbyssalShroudEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        MagicData.getPlayerMagicData(pLivingEntity).getSyncedData().removeEffects(SyncedSpellData.ABYSSAL_SHROUD);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        MagicData.getPlayerMagicData(pLivingEntity).getSyncedData().addEffects(SyncedSpellData.ABYSSAL_SHROUD);
    }


    public static boolean doEffect(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity.level().isClientSide || damageSource.is(DamageTypeTagGenerator.BYPASS_EVASION) || damageSource.is(DamageTypeTags.IS_FALL)  || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        var random = livingEntity.getRandom();
        var level = livingEntity.level();


        Vec3 sideStep = new Vec3(random.nextBoolean() ? 1 : -1, 0, -.25);
        sideStep.yRot(livingEntity.getYRot());

        particleCloud(livingEntity);

        Vec3 ground = livingEntity.position().add(sideStep);
        ground = Utils.moveToRelativeGroundLevel(level, ground, 2, 1);

        var dimensions = livingEntity.getDimensions(livingEntity.getPose());
        Vec3 vec3 = ground.add(0.0, dimensions.height / 2.0, 0.0);
        VoxelShape voxelshape = Shapes.create(AABB.ofSize(vec3, dimensions.width + .2f, dimensions.height + .2f, dimensions.width + .2f));
        Optional<Vec3> optional = level
                .findFreePosition(null, voxelshape, vec3, (double) dimensions.width, (double) dimensions.height, (double) dimensions.width);
        if (optional.isPresent()) {
            ground = optional.get().add(0, -dimensions.height / 2 + 1.0E-6, 0);
        }
        if (level.collidesWithSuffocatingBlock(null, AABB.ofSize(ground.add(0, dimensions.height / 2, 0), dimensions.width, dimensions.height, dimensions.width))) {
            ground = livingEntity.position();
        }

        if (livingEntity.isPassenger()) {
            livingEntity.stopRiding();
        }
        if (!level.getBlockState(BlockPos.containing(ground).below()).isAir()) {
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
        MagicManager.spawnParticles(entity.level(), ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 70, entity.getBbWidth() / 4, entity.getBbHeight() / 5, entity.getBbWidth() / 4, .035, false);
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
