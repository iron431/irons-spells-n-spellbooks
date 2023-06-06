package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class EvasionEffect extends MobEffect {

    //TODO: 1.19.4 port damage sources
    public static Set<DamageSource> excludeDamageSources = Set.of(
//            DamageSources.ON_FIRE,
//            DamageSource.WITHER,
//            DamageSource.FREEZE,
            DamageSources.CAULDRON,
            DamageSources.BLEED_DAMAGE
//            DamageSource.STARVE,
//            DamageSource.DROWN,
//            DamageSource.STALAGMITE,
//            DamageSource.OUT_OF_WORLD
    );

    public EvasionEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().removeEffects(SyncedSpellData.EVASION);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().addEffects(SyncedSpellData.EVASION);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().setEvasionHitsRemaining(pAmplifier);

    }

    public static boolean doEffect(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity.level.isClientSide || excludeDamageSources.contains(damageSource) || damageSource.is(DamageTypeTags.IS_FALL) || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        var data = PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData();
        data.subtractEvasionHit();
        if (data.getEvasionHitsRemaining() == 0)
            livingEntity.removeEffect(MobEffectRegistry.EVASION.get());

        double d0 = livingEntity.getX();
        double d1 = livingEntity.getY();
        double d2 = livingEntity.getZ();
        double maxRadius = 18d;
        var level = livingEntity.level;
        var random = livingEntity.getRandom();

        for (int i = 0; i < 16; ++i) {
            var minRadius = maxRadius / 2;
            Vec3 vec = new Vec3((double) random.nextInt((int) minRadius, (int) maxRadius), 0, 0);
            int degrees = random.nextInt(360);
            vec = vec.yRot(degrees);

            double x = d0 + vec.x;
            double y = Mth.clamp(livingEntity.getY() + (double) (livingEntity.getRandom().nextInt((int) maxRadius) - maxRadius / 2), (double) level.getMinBuildHeight(), (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
            double z = d2 + vec.z;

            if (livingEntity.isPassenger()) {
                livingEntity.stopRiding();
            }

            if (livingEntity.randomTeleport(x, y, z, true)) {
                if (damageSource.getEntity() != null) {
                    livingEntity.lookAt(EntityAnchorArgument.Anchor.EYES, damageSource.getEntity().getEyePosition());
                }
                level.playSound((Player) null, d0, d1, d2, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                livingEntity.playSound(SoundEvents.ENDERMAN_TELEPORT, 2.0F, 1.0F);
                break;
            }

            if (maxRadius > 2) {
                maxRadius--;
            }
        }
        //Vanilla teleport only spawns particles from the original location, not at the destination
        particleCloud(livingEntity);
        return true;
    }

    private static void particleCloud(LivingEntity entity) {
        Vec3 pos = entity.position().add(0, entity.getBbHeight() / 2, 0);
        MagicManager.spawnParticles(entity.level, ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 70, entity.getBbWidth() / 4, entity.getBbHeight() / 5, entity.getBbWidth() / 4, .035, false);
    }

}
