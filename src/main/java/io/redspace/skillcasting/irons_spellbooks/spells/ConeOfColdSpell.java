package io.redspace.skillcasting.irons_spellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

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
    public void onCast(CastContext castContext) {
        // todo: implement cone hitbox helpers
//        if (playerMagicData.isCasting()
//                && playerMagicData.getCastingSpellId().equals(this.getSpellId())
//                && playerMagicData.getAdditionalCastData() instanceof EntityCastData entityCastData
//                && entityCastData.getCastingEntity() instanceof AbstractConeProjectile cone) {
//            cone.setDealDamageActive();
//        } else {
//            ConeOfColdProjectile coneOfColdProjectile = new ConeOfColdProjectile(world, entity);
//            coneOfColdProjectile.setPos(entity.position().add(0, entity.getEyeHeight() * .7, 0));
//            coneOfColdProjectile.setDamage(getDamage(spellLevel, entity));
//            world.addFreshEntity(coneOfColdProjectile);
//            playerMagicData.setAdditionalCastData(new EntityCastData(coneOfColdProjectile));
//            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
//        }
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
