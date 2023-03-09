package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

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
                teleportAndStrikeLightning(livingEntity, level, pAmplifier);
            }
        }
        PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData().removeEffects(SyncedSpellData.ASCENSION);
    }

    private static void teleportAndStrikeLightning(LivingEntity livingEntity, Level level, int damage) {
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
        lightningBolt.setVisualOnly(true);
        lightningBolt.setPos(dest);
        level.addFreshEntity(lightningBolt);

        //livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));
        float radius = 8;
        Vec3 finalDest = dest;
        level.getEntities(livingEntity, livingEntity.getBoundingBox().inflate(radius)).forEach(entity -> {
            double distance = entity.distanceToSqr(finalDest);
            if (distance < radius * radius) {
                float finalDamage = (float) (damage * (1 - distance / (radius * radius)));
                DamageSources.applyDamage(entity, finalDamage, SpellType.ASCENSION_SPELL.getDamageSource(lightningBolt, livingEntity), SchoolType.LIGHTNING);
            }
        });

        playerMagicData.resetAdditionalCastData();
    }

    private int duration;

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
        //Do tick logic

        //We want to ascend for 4 ticks, 2 seconds before the effect wears out
        //Also shoot a beam of electricity to signal the lightning strike
        if (livingEntity.level.isClientSide) {
            if (duration <= 40) {
                if (duration >= 36) {
                    Vec3 delta = livingEntity.getDeltaMovement();
                    float f = Utils.smoothstep((float) delta.y, .75f, .55f);
                    livingEntity.setDeltaMovement(new Vec3(delta.x, f, delta.z));
                }
                if (duration % 3 == 0) {
                    var level = livingEntity.level;
                    Vec3 pos = Utils.raycastForBlock(level,livingEntity.getEyePosition(),livingEntity.getEyePosition().add(livingEntity.getForward().normalize().scale(32)), ClipContext.Fluid.NONE).getLocation();
                    for (int i = 0; i < 3; i++) {
                        level.addParticle(ParticleHelper.ELECTRICITY, pos.x, pos.y + i * .75, pos.z, 0, 0, 0);
                    }
                }

            }
        }

    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        //Whether or not to tick
        this.duration = duration;
        //irons_spellbooks.LOGGER.debug("Ascdended duration: {}", duration);
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        //super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        for (Map.Entry<Attribute, AttributeModifier> entry : this.getAttributeModifiers().entrySet()) {
            AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
            if (attributeinstance != null) {
                AttributeModifier attributemodifier = entry.getValue();
                attributeinstance.removeModifier(attributemodifier);
                attributeinstance.addPermanentModifier(new AttributeModifier(attributemodifier.getId(), this.getDescriptionId() + " " + pAmplifier, attributemodifier.getAmount(), attributemodifier.getOperation()));
            }
        }
        PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().addEffects(SyncedSpellData.ASCENSION);
    }

    public static void ambientParticles(ClientLevel level, LivingEntity entity) {
        var random = entity.getRandom();
        for (int i = 0; i < 2; i++) {
            Vec3 motion = new Vec3(
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1
            );
            motion = motion.scale(.04f);
            level.addParticle(ParticleHelper.ELECTRICITY, entity.getRandomX(.4f), entity.getRandomY(), entity.getRandomZ(.4f), motion.x, motion.y, motion.z);
        }
    }
}
