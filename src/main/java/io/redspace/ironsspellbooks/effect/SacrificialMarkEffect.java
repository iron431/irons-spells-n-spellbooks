package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.blood.SacrificeSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class SacrificialMarkEffect extends MagicMobEffect implements ISyncedMobEffect {
    public SacrificialMarkEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8b1538);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level.isClientSide() || !entity.hasEffect(MobEffectRegistry.SACRIFICIAL_MARK.get())) {
            return;
        }
        var mark = entity.getEffect(MobEffectRegistry.SACRIFICIAL_MARK.get());
        if (mark == null) {
            return;
        }

        LivingEntity owner = SummonManager.getOwner(entity) instanceof LivingEntity livingOwner ? livingOwner : entity;
        SacrificeSpell spell = (SacrificeSpell) SpellRegistry.SACRIFICE_SPELL.get();
        float damage = spell.getDamage(mark.getAmplifier() + 1, owner);
        float explosionRadius = spell.getRadius(entity);

        SacrificeSpell.doSacrificeExplosion(
                entity.level(),
                spell.getDamageSource(entity, owner),
                damage,
                explosionRadius,
                entity.getBoundingBox().getCenter()
        );
        entity.remove(Entity.RemovalReason.KILLED);
    }

    @Override
    public void clientTick(LivingEntity livingEntity, MobEffectInstance instance) {
        ParticleOptions particle = ParticleHelper.BLOOD;
        var random = livingEntity.getRandom();
        for (int i = 0; i < 2; i++) {
            Vec3 motion = new Vec3(
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1
            );
            motion = motion.scale(.04f).add(livingEntity.getDeltaMovement().scale(1)).add(0, 0.04, 0);
            livingEntity.level.addParticle(particle, livingEntity.getRandomX(.75f), livingEntity.getRandomY(), livingEntity.getRandomZ(.75f), motion.x, motion.y + 0.1, motion.z);
        }
    }
}
