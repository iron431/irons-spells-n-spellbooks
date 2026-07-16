package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellDamageSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.EchoingStrikesData;
import io.redspace.ironsspellbooks.effect.EchoingStrikesEffect;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class EchoingStrikesSpell extends AbstractSpell {

    private static final int DURATION_TICKS = 2 * 20 * 60;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(60)
            .build();

    public EchoingStrikesSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 5;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.percent_damage",
                        Utils.stringTruncation(EchoingStrikesEffect.getDamageModifier(amplifier, getSpellPowerMultiplier(castContext)) * 100, 0)),
                Component.translatable("ui.irons_spellbooks.echoing_hits", castContext.getOrDefault(SpellcastingComponentTypes.HIT_COUNT, 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.ECHOING_STRIKES_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.HIT_COUNT, castContext.getSkillLevel() + 2);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, DURATION_TICKS);
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, provideAmplifier());
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.ECHOING_STRIKES,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0),
                    false, false, true));
            EchoingStrikesData.get(entity).setHitCount(castContext.getOrDefault(SpellcastingComponentTypes.HIT_COUNT, 1));

            Vec3 vec3 = entity.position().add(0, 0.5, 0);
            MagicManager.spawnParticles(level, new ShockwaveParticleOptions(new Vector3f(1f, 0.333f, 1f), 10 * -1.5f * 0.05f, true),
                    vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, new ShockwaveParticleOptions(new Vector3f(1f, 0.333f, 1f), 20 * -1.5f * 0.05f, true),
                    vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, new ShockwaveParticleOptions(new Vector3f(1f, 0.333f, 1f), 30 * -1.5f * 0.05f, true),
                    vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, true);
        }
    }

    @Override
    public void onClientCastComplete(CastContext castContext, CastEndReason reason) {
        super.onClientCastComplete(castContext, reason);
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            EchoingStrikesData.get(entity).vfxTimestamp = entity.tickCount + 20;
        }
    }

    @Override
    public SpellDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setIFrames(0);
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }

    private int provideAmplifier() {
        return (int) (0.75 / EchoingStrikesEffect.PERCENT_PER_AMPLIFIER);
    }
}
