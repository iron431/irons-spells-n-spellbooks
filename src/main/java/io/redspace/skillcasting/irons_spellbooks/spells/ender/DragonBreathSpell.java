package io.redspace.skillcasting.irons_spellbooks.spells.ender;



import io.redspace.ironsspellbooks.api.config.DefaultConfig;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;

import io.redspace.ironsspellbooks.api.util.Utils;

import io.redspace.ironsspellbooks.damage.DamageSources;

import io.redspace.ironsspellbooks.registries.SoundRegistry;

import io.redspace.skillcasting.api.cast.CastContext;

import io.redspace.skillcasting.api.cast.CasterRef;

import io.redspace.skillcasting.api.skill.CastType;

import io.redspace.skillcasting.client.ClientSkillTicker;

import io.redspace.skillcasting.data.PlayableSound;

import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;

import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;

import io.redspace.skillcasting.lifecycle.ActiveCast;

import io.redspace.skillcasting.lifecycle.SkillcastingData;

import io.redspace.skillcasting.registry.SkillcastingComponentTypes;

import io.redspace.skillcasting.util.SkillcastingUtils;

import net.minecraft.core.particles.ParticleTypes;

import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.sounds.SoundEvents;

import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.Mob;

import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;



import javax.annotation.Nullable;

import java.util.List;

import java.util.Optional;

import java.util.Set;



public class DragonBreathSpell extends AbstractSpellSkill {



    private final DefaultConfig defaultConfig = new DefaultConfig()

            .setMinRarity(SpellRarity.COMMON)

            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)

            .setMaxLevel(10)

            .setCooldownSeconds(12)

            .build();



    public DragonBreathSpell() {

        this.manaCostPerLevel = 1;

        this.baseSpellPower = 1;

        this.spellPowerPerLevel = 0.75f;

        this.castTime = 100;

        this.baseManaCost = 5;

    }



    @Override

    public List<MutableComponent> getUniqueInfo(CastContext castContext) {

        return List.of(Component.translatable("ui.irons_spellbooks.damage",

                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)));

    }



    @Override

    public CastType getCastType() {

        return CastType.CONTINUOUS;

    }



    @Override

    public DefaultConfig getDefaultConfig() {

        return defaultConfig;

    }



    @Override

    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {

        return PlayableSound.standard(SoundEvents.ENDER_DRAGON_GROWL).toOpt();

    }



    @Override

    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {

        return PlayableSound.standard(SoundRegistry.FIRE_BREATH_LOOP).toOpt();

    }



    @Override

    public void buildContextComponents(CastContext castContext) {

        super.buildContextComponents(castContext);

        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));

    }



    @Override

    public void onCast(Level level, CastContext castContext) {

        Vec3 origin = SkillcastingUtils.defaultConeOrigin(castContext);

        Set<Entity> entities = SkillcastingUtils.collectConeTargets(castContext,

                target -> SkillcastingUtils.isConeProjectileTarget(level, origin, target));

        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);

        entities.forEach(entity -> {

            if (!DamageSources.isFriendlyFireBetween(castContext.asEntityCaster(), entity)) {

                if (entity instanceof LivingEntity livingEntity) {

                    DamageSources.ignoreNextKnockback(livingEntity);

                }

                DamageSources.applyDamage(entity, damage, getDamageSource(level, null, castContext.asEntityCaster()));

            }

        });

    }



    @Override

    public Optional<ClientSkillTicker> createClientTicker() {

        return Optional.of(this::spawnParticles);

    }



    private void spawnParticles(CasterRef casterRef, SkillcastingData data, ActiveCast activeCast) {
        Entity owner = activeCast.context().asEntityCaster();
        if (owner == null) {
            return;
        }

        Vec3 rotation = owner.getLookAngle().normalize();

        Vec3 pos = owner.position().add(rotation.scale(1.6));

        double x = pos.x;

        double y = pos.y + owner.getEyeHeight() * 0.9f;

        double z = pos.z;



        for (int i = 0; i < 12; i++) {

            double speed = casterRef.level().getRandom().nextDouble() * 0.35 + 0.25;

            double offset = 0.15;

            double ox = Math.random() * 2 * offset - offset;

            double oy = Math.random() * 2 * offset - offset;

            double oz = Math.random() * 2 * offset - offset;

            double angularness = 0.3;

            Vec3 randomVec = new Vec3(

                    Math.random() * 2 * angularness - angularness,

                    Math.random() * 2 * angularness - angularness,

                    Math.random() * 2 * angularness - angularness).normalize();

            Vec3 result = rotation.scale(3).add(randomVec).normalize().scale(speed);

            casterRef.level().addParticle(ParticleTypes.DRAGON_BREATH, x + ox, y + oy, z + oz, result.x, result.y, result.z);

        }

    }



    @Override

    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {

        return super.getDamageSource(level, projectile, attacker).setFireTicks(80);

    }



    @Override

    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {

        return mob.distanceToSqr(target) > (10 * 10) * 1.2;

    }

}


