package com.example.testmod.effect;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.damage.DamageSources;
import com.example.testmod.entity.AbstractSpellCastingMob;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasEvasion(false);
        } else if (pLivingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            TestMod.LOGGER.debug("EvasionEffect.removeAttributeModifiers {}", pLivingEntity);
            abstractSpellCastingMob.getPlayerMagicData().getSyncedData().setHasEvasion(false);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);


        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasEvasion(true);
        } else if (pLivingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            TestMod.LOGGER.debug("EvasionEffect.addAttributeModifiers {}", pLivingEntity);
            abstractSpellCastingMob.getPlayerMagicData().getSyncedData().setHasEvasion(true);
        }
    }

    public static boolean doEffect(ServerPlayer serverPlayer, DamageSource damageSource) {

        if (excludeDamageSources.contains(damageSource) || damageSource.isFall() || damageSource.isBypassMagic() || damageSource.isBypassInvul()) {
            return false;
        }

        double d0 = serverPlayer.getX();
        double d1 = serverPlayer.getY();
        double d2 = serverPlayer.getZ();
        double maxRadius = 18d;
        var level = serverPlayer.level;
        var random = serverPlayer.getRandom();

        for (int i = 0; i < 16; ++i) {
            var minRadius = maxRadius / 2;
            Vec3 vec = new Vec3((double) random.nextInt((int) minRadius, (int) maxRadius), 0, 0);
            int degrees = random.nextInt(360);
            vec = vec.yRot(degrees);

            double x = d0 + vec.x;
            double y = Mth.clamp(serverPlayer.getY() + (double) (serverPlayer.getRandom().nextInt((int) maxRadius) - maxRadius / 2), (double) level.getMinBuildHeight(), (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
            double z = d2 + vec.z;

            if (serverPlayer.isPassenger()) {
                serverPlayer.stopRiding();
            }

            if (serverPlayer.randomTeleport(x, y, z, true)) {
                if (damageSource.getEntity() != null) {
                    serverPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, damageSource.getEntity(), EntityAnchorArgument.Anchor.EYES);
                }
                level.playSound((Player) null, d0, d1, d2, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                break;
            }

            if (maxRadius > 2) {
                maxRadius--;
            }
        }
        return true;
    }
}
