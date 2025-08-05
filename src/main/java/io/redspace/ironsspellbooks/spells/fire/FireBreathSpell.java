package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellSkillDamageSource;
import io.redspace.ironsspellbooks.network.particles.FireBreathParticlesPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.registries.SpellSkillRegistry;
import io.redspace.skillcastingapi.core.CastType;
import io.redspace.skillcastingapi.data.ICastContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class FireBreathSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(castContext), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public FireBreathSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;
    }

    @Override
    public io.redspace.skillcastingapi.core.CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<SoundEvent> getOnCastSound() {
        return Optional.of(SoundRegistry.FIRE_BREATH_LOOP.get());
    }

    @Override
    public void onServerCastTick(ICastContext castContext) {
        castContext.caster().type().handlePacketDistribution((ServerLevel) castContext.getLevel(), castContext.caster(), new FireBreathParticlesPacket(castContext.getPosition().add(castContext.getForward()), castContext.getForward()));
    }

    @Override
    public void onCast(ICastContext castContext) {
        var level = castContext.getLevel();
        var pos = castContext.getPosition();
        var forward = castContext.getForward();
        var damage = getDamage(castContext);
        Utils.coneHitbox(level, pos, forward,
                        entity -> entity != castContext.getEntity() &&
                                Utils.hasLineOfSight(level, pos, entity.getBoundingBox().getCenter(), true) &&
                                !DamageSources.isFriendlyFireBetween(castContext.getEntity(), entity))
                .forEach(entity ->
                        DamageSources.applyDamage(entity, damage, SpellSkillRegistry.FIRE_BREATH.get().getDamageSource(castContext.getEntity())));
    }

    public float getDamage(ICastContext castContext) {
        return 1 + getSpellPower(castContext) * .75f;
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTicks(80);
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (10 * 10) * 1.2;
    }
}
