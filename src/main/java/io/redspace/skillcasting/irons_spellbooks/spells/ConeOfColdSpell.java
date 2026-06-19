package io.redspace.skillcasting.irons_spellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ConeOfColdSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public ConeOfColdSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;
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
        return Optional.of(PlayableSound.of(SoundRegistry.CONE_OF_COLD_LOOP, 2f, 0.9f, 1.1f));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER, 0f));
    }

    @Override
    public void onCast(CastContext castContext) {
        List<AABB> coneColliders = new ArrayList<>(List.of(
                new AABB(0, 0, 0, 1, 1, 1),
                new AABB(0, 0, 0, 2.5, 1.5, 2.5),
                new AABB(0, 0, 0, 3.5, 2, 3.5),
                new AABB(0, 0, 0, 4.5, 3, 4.5)

        ));
        Vec3 direction = castContext.direction();
        Vec3 origin = castContext.position().subtract(0, 0.5, 0);
        for (int i = 0; i < coneColliders.size(); i++) {
            AABB collider = coneColliders.get(i);
            double distance = 1 + (i * collider.getXsize() / 2);
            Vec3 position = origin.add(direction.scale(distance));
            position = position.subtract(collider.getXsize() / 2, 0, collider.getZsize() / 2);
            coneColliders.set(i, collider.move(position));
        }
        Set<Entity> entities = coneColliders.stream().flatMap(aabb -> castContext.level().getEntities(castContext.asEntityCaster(), aabb).stream()).filter(target ->
                target.canBeHitByProjectile() && Utils.hasLineOfSight(castContext.level(), origin, target.getBoundingBox().getCenter(), true)
        ).collect(Collectors.toSet());
        entities.forEach(entity -> {
            if (!DamageSources.isFriendlyFireBetween(castContext.asEntityCaster(), entity)) {
                DamageSources.applyDamage(entity, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), castContext.level().damageSources().magic());
            }
        });
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of(this::spawnParticles);
    }

    public void spawnParticles(CasterRef casterRef, SkillcastingData data, ActiveCast activeCast) {
        CastContext castContext = activeCast.context();
        Vec3 rotation = castContext.direction();
        var pos = castContext.position().add(rotation.scale(0.25));

        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        for (int i = 0; i < 10; i++) {
            double speed = casterRef.level().getRandom().nextDouble() * .7 + .15;
            double offset = .125;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            double angularness = .8;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
            casterRef.level().addParticle(Math.random() > .15 ? ParticleHelper.SNOW_DUST : ParticleHelper.SNOWFLAKE, x + ox, y + oy, z + oz, result.x, result.y, result.z);

        }
    }

//    @Override
//    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
//        return super.getDamageSource(projectile, attacker).setFreezeTicks(80);
//    }

//    public float getDamage(int spellLevel, LivingEntity caster) {
//        return 1 + getSpellPower(spellLevel, caster) * .75f;
//    }

//    @Override
//    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
//        return mob.distanceToSqr(target) > (10 * 10) * 1.2;
//    }
}
