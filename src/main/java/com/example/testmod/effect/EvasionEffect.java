package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class EvasionEffect extends MobEffect {
    public EvasionEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasEvasion(false);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasEvasion(true);
        }
    }

    public static void doEffect(ServerPlayer serverPlayer, Entity damageSource) {
        double d0 = serverPlayer.getX();
        double d1 = serverPlayer.getY();
        double d2 = serverPlayer.getZ();
        var level = serverPlayer.level;

        for (int i = 0; i < 16; ++i) {
            double x = serverPlayer.getX() + (serverPlayer.getRandom().nextDouble() - 0.5D) * 18.0D;
            double y = Mth.clamp(serverPlayer.getY() + (double) (serverPlayer.getRandom().nextInt(18) - 9), (double) level.getMinBuildHeight(), (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
            double z = serverPlayer.getZ() + (serverPlayer.getRandom().nextDouble() - 0.5D) * 18.0D;
            if (serverPlayer.isPassenger()) {
                serverPlayer.stopRiding();
            }

            if (serverPlayer.randomTeleport(x, y, z, true)) {
                serverPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, damageSource, EntityAnchorArgument.Anchor.EYES);
                level.playSound((Player) null, d0, d1, d2, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.0F);
                serverPlayer.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                break;
            }
        }
    }
}
