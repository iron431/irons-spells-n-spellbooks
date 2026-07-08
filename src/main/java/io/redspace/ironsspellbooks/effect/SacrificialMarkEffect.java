package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.TargetedEntitiesData;
import io.redspace.skillcasting.irons_spellbooks.spells.blood.SacrificeSpell;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber
public class SacrificialMarkEffect extends MagicMobEffect implements ISyncedMobEffect {
    public SacrificialMarkEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8b1538);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel) || !entity.hasEffect(MobEffectRegistry.SACRIFICIAL_MARK)) {
            return;
        }
        var mark = entity.getEffect(MobEffectRegistry.SACRIFICIAL_MARK);
        if (mark == null) {
            return;
        }

        Entity owner = SummonManager.getOwner(entity) instanceof Entity summoner ? summoner : entity;
        SacrificeSpell spell = SpellRegistry.SACRIFICE_SPELL.get();
        // fixme: this is the perfect place for a level cast instance
        //  infrastructure is far from set up though
        CasterRef caster = CasterRef.entity(owner);
        CastContext castContext = SkillcastingManager.buildCastContext(caster, SpellRegistry.SACRIFICE_SPELL, mark.getAmplifier() + 1, null, false);
        castContext.set(SkillcastingComponentTypes.TARGETED_ENTITIES, new TargetedEntitiesData(entity));
        spell.onCast(serverLevel, castContext);
        spell.onPostCast(castContext);
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
