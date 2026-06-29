package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.effect.EchoingStrikesData;
import io.redspace.ironsspellbooks.effect.EchoingStrikesEffect;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class EchoingStrikesSpell extends AbstractSpell {
    public static final float radius = 2;
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "echoing_strikes");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.percent_damage", Utils.stringTruncation(EchoingStrikesEffect.getDamageModifier(getAmplifierForLevel(spellLevel, caster), caster) * 100, 0)),
                Component.translatable("ui.irons_spellbooks.echoing_hits", getHitCount(spellLevel, caster))
        );
    }

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
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.ECHOING_STRIKES_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return super.getCastStartSound();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ECHOING_STRIKES, 2 * 20 * 60, getAmplifierForLevel(spellLevel, entity), false, false, true));
        EchoingStrikesData.get(entity).setHitCount(getHitCount(spellLevel, entity));

        Vec3 vec3 = entity.position().add(0, 0.5, 0);
//        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(1f, .333f, 1f, 2.5f), vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, false);
        MagicManager.spawnParticles(level, new ShockwaveParticleOptions(new Vector3f(1f, .333f, 1f), 10 * -1.5f * .05f, true), vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, new ShockwaveParticleOptions(new Vector3f(1f, .333f, 1f), 20 * -1.5f * .05f, true), vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, new ShockwaveParticleOptions(new Vector3f(1f, .333f, 1f), 30 * -1.5f * .05f, true), vec3.x, vec3.y, vec3.z, 1, 0, 0, 0, 0, true);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

//    @Override
//    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
//        super.onClientCast(level, spellLevel, entity, castData);
//        EchoingStrikesData.get(entity).vfxTimestamp = entity.tickCount + 20;
//    }

    public int getHitCount(int spellLevel, LivingEntity caster) {
        return spellLevel + 2;
    }

    private int getAmplifierForLevel(int spellLevel, LivingEntity caster) {
        return (int) (.75 / EchoingStrikesEffect.PERCENT_PER_AMPLIFIER); // 75% base (power scaling is handled by effect)
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
