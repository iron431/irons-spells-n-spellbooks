package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.dragon_breath.DragonBreathPool;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

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
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.FIRE_BREATH_LOOP).toOpt();
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.ENDER_DRAGON_GROWL).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 1 + getSpellPower(castContext) * 0.75f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 origin = SkillcastingUtils.defaultConeOrigin(castContext);
        Set<Entity> entities = SkillcastingUtils.collectConeTargets(castContext,
                target -> SkillcastingUtils.isConeProjectileTarget(level, origin, target));
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        entities.forEach(entity -> {
            if (!DamageSources.isFriendlyFireBetween(castContext.asEntityCaster(), entity)) {
                if (entity instanceof LivingEntity livingEntity) {
                    DamageSources.ignoreNextKnockback(livingEntity);
                }
                if (DamageSources.applyDamage(entity, damage, getDamageSource(level, null, castContext.asEntityCaster())) && castContext.level().getRandom().nextFloat() < 0.3f) {
                    createDragonBreathPuddle(castContext, entity.position());
                }
            }
        });
    }

    private void createDragonBreathPuddle(CastContext castContext, Vec3 spawnPos) {
        DragonBreathPool pool = new DragonBreathPool(castContext.level());
        pool.setOwner(castContext.asEntityCaster());
        pool.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        pool.moveTo(spawnPos);
        castContext.level().addFreshEntity(pool);
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of(this::spawnParticles);
    }

    private void spawnParticles(CasterRef casterRef, SkillcastingData data, ActiveCast activeCast) {
        CastContext castContext = activeCast.context();
        Vec3 rotation = castContext.direction();
        var pos = castContext.position(PositionAnchor.CASTING_POSITION_CENTER).add(rotation.scale(1.5));
        for (int i = 0; i < 12; i++) {
            double speed = casterRef.level().getRandom().nextDouble() * .35 + .25;
            double offset = .15;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            double angularness = .3;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            casterRef.level().addParticle(ParticleTypes.DRAGON_BREATH, pos.x + ox, pos.y + oy, pos.z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (10 * 10) * 1.2;
    }
}
