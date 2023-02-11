package com.example.testmod.effect;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.damage.DamageSources;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;


public class HeartstopEffect extends MobEffect {
    public HeartstopEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    private int duration;

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);

        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);

            playerMagicData.getSyncedData().setHasHeartstop(false);

            //Whether or not player has spawn immunity (we want to damage them regardless)
            if (serverPlayer.tickCount > 60) {
                serverPlayer.hurt(DamageSources.HEARTSTOP, playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());
                //TestMod.LOGGER.debug("{} had no spawn immunity", pLivingEntity.getName().getString());

            } else {
                //TODO: find a better way to apply damage
                serverPlayer.kill();
//                serverPlayer.setHealth(serverPlayer.getHealth() - playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());

                //TestMod.LOGGER.debug("{} had spawn immunity", pLivingEntity.getName().getString());

            }
            playerMagicData.getSyncedData().setHeartstopAccumulatedDamage(0);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        //TestMod.LOGGER.debug("{} ticks existed: {}", pLivingEntity.getName().getString(), pLivingEntity.tickCount);

        //Heart beats once every 2 seconds at 0% damage, and 2 times per second at 100% damage (relative to health)
        if (pLivingEntity instanceof LocalPlayer player) {
            float damage = ClientMagicData.getPlayerSyncedData(player.getId()).getHeartstopAccumulatedDamage();
            float f = 1 - Mth.clamp(damage / player.getHealth(), 0, 1);
            int i = (int) (10 + (40 - 10) * f);
            TestMod.LOGGER.debug("{} ({}/{} = {})", i, damage, player.getHealth(), f);
            if (this.duration % Math.max(i, 1) == 0) {
                player.playSound(SoundEvents.WARDEN_HEARTBEAT, 1, 0.85f);
            }

        }
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        this.duration = pDuration;
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().setHasHeartstop(true);
        }
    }

}
