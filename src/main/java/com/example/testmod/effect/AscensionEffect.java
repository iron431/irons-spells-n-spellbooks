package com.example.testmod.effect;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AscensionEffect extends MobEffect {

    public AscensionEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(livingEntity, pAttributeMap, pAmplifier);
        var level = livingEntity.level;
        if (!level.isClientSide) {
            if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {
                var playerMagicData = PlayerMagicData.getPlayerMagicData(livingEntity);
                var teleportData = (TeleportSpell.TeleportData) playerMagicData.getAdditionalCastData();

                Vec3 dest = null;
                if (teleportData != null) {
                    var potentialTarget = teleportData.getTeleportTargetPosition();
                    if (potentialTarget != null) {
                        dest = Utils.putVectorOnWorldSurface(level, potentialTarget);
                    }
                }

                if (dest == null) {
                    dest = TeleportSpell.findTeleportLocation(level, livingEntity, 32);
                }
                //Put destination on the ground (we're a lightning bolt after all)
                if (level.getBlockState(new BlockPos(dest).below()).isAir())
                    dest = Utils.putVectorOnWorldSurface(level, dest);

                //Messages.sendToPlayersTrackingEntity(new ClientboundTeleportParticles(entity.position(), dest), entity);
                livingEntity.teleportTo(dest.x, dest.y, dest.z);
                livingEntity.resetFallDistance();
                livingEntity.removeEffect(MobEffectRegistry.ASCENSION.get());

                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
                lightningBolt.setDamage(pAmplifier);
                lightningBolt.setPos(dest);
                level.addFreshEntity(lightningBolt);

                livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));

                playerMagicData.resetAdditionalCastData();
            }
        }
        PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData().removeEffects(SyncedSpellData.ASCENSION);
    }

    private int duration;

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
        //Do tick logic

        //We want to ascend for 5 ticks, 3 seconds before the effect wears out
        if (livingEntity.level.isClientSide)
            if (duration <= 60 && duration >= 56) {
                Vec3 delta = livingEntity.getDeltaMovement();
                float f = Utils.smoothstep((float) delta.y, .75f, .55f);
                livingEntity.setDeltaMovement(new Vec3(delta.x, f, delta.z));
            }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        //Whether or not to tick
        this.duration = duration;
        //TestMod.LOGGER.debug("Ascdended duration: {}", duration);
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().addEffects(SyncedSpellData.ASCENSION);
    }
}
