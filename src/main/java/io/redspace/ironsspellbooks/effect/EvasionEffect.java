package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.datagen.DamageTypeTagGenerator;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EvasionEffect extends CustomDescriptionMobEffect implements ISyncedMobEffect {
    public EvasionEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public Component getDescriptionLine(MobEffectInstance instance) {
        int amp = instance.getAmplifier() + 1;
        return Component.translatable("tooltip.irons_spellbooks.evasion_description", amp).withStyle(ChatFormatting.BLUE);
    }

    @Override
    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
        MagicData.getPlayerMagicData(pLivingEntity).getSyncedData().setEvasionHitsRemaining(pAmplifier);
    }

    public static boolean doEffect(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity.level.isClientSide
                || damageSource.is(DamageTypeTags.IS_FALL)
                || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || damageSource.is(DamageTypeTagGenerator.BYPASS_EVASION)) {
            return false;
        }

        var data = MagicData.getPlayerMagicData(livingEntity).getSyncedData();
        data.subtractEvasionHit();
        if (data.getEvasionHitsRemaining() < 0) {
            livingEntity.removeEffect(MobEffectRegistry.EVASION);
        }

        double d0 = livingEntity.getX();
        double d1 = livingEntity.getY();
        double d2 = livingEntity.getZ();
        float maxRadius = 12;
        var level = livingEntity.level;
        var random = livingEntity.getRandom();
        Vec3 horizontalDirection = livingEntity.getForward().multiply(1, 0, 1).add(0.1,0,0).normalize();
        Vec3 origin = livingEntity.getBoundingBox().getCenter();
        float radius = maxRadius / 2f;
        int range = 45;
        for (int attempts = 0; attempts < 24; attempts++) {
            Vec3 direction = horizontalDirection.scale(-1).yRot(random.nextIntBetweenInclusive(-range, range) * Mth.DEG_TO_RAD);
            HitResult raycast = Utils.raycastForBlock(level, origin, origin.add(direction.scale(radius).add(0, 4, 0)), ClipContext.Fluid.NONE);
            Vec3 destination = Utils.moveToRelativeGroundLevel(level, raycast.getLocation().subtract(direction.scale(livingEntity.getBbWidth())), 5);
            if (destination.distanceToSqr(livingEntity.position()) > 4 && level.noCollision(livingEntity.getBoundingBox().move(destination.subtract(livingEntity.position())).inflate(-0.05)) && !level.getBlockState(BlockPos.containing(destination).below()).isAir()) {
                particleCloud(livingEntity);
                level.playSound(null, d0, d1, d2, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                Utils.handleSpellTeleport(SpellRegistry.EVASION_SPELL.get(), livingEntity, destination);
                if (damageSource.getEntity() != null) {
                    livingEntity.lookAt(EntityAnchorArgument.Anchor.EYES, damageSource.getEntity().getEyePosition());
                }
                break;
            }
            range *= 2;
            radius += 1;
        }

        particleCloud(livingEntity);
        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static void particleCloud(LivingEntity entity) {
        Vec3 pos = entity.position().add(0, entity.getBbHeight() / 2, 0);
        MagicManager.spawnParticles(entity.level, ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 70, entity.getBbWidth() / 4, entity.getBbHeight() / 5, entity.getBbWidth() / 4, .035, false);
    }

}