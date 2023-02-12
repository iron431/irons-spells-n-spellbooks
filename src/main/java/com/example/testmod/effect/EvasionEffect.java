package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.damage.DamageSources;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    public static Set<DamageSource> excludeDamageSources = Set.of(
            DamageSource.ON_FIRE,
            DamageSource.WITHER,
            DamageSource.FREEZE,
            DamageSources.CAULDRON,
            DamageSources.BLEED_DAMAGE,
            DamageSource.STARVE,
            DamageSource.DROWN,
            DamageSource.STALAGMITE,
            DamageSource.OUT_OF_WORLD);

    public EvasionEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().setHasEvasion(false);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().setHasEvasion(true);
    }

    public static boolean doEffect(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity.level.isClientSide || excludeDamageSources.contains(damageSource) || damageSource.isFall() || damageSource.isBypassMagic() || damageSource.isBypassInvul()) {
            return false;
        }

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
                level.playSound((Player) null, d0, d1, d2, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.0F);
                livingEntity.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                break;
            }

            if (maxRadius > 2) {
                maxRadius--;
            }
        }
        return true;
    }
}
