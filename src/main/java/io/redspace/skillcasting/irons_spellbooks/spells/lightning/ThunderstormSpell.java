package io.redspace.skillcasting.irons_spellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ThunderstormSpell extends AbstractSpellSkill {

    private static final float STORM_RADIUS = 20f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(120)
            .build();

    public ThunderstormSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 70;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        float power = castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f);
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(((amplifier - 7) * power) + 7, 2)),
                Component.translatable("ui.irons_spellbooks.radius", (int) STORM_RADIUS),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.THUNDERSTORM_PREPARE).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float powerMultiplier = castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f);
        int scaledLevel = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER,
                8 + (int) ((scaledLevel - 1) * powerMultiplier));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS,
                (int) ((20 + (2 * (scaledLevel - 1) * powerMultiplier)) * 20));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, STORM_RADIUS);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.THUNDERSTORM,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0),
                    false, false, true));
        }

        Vec3 origin = castContext.bottomCenter();
        int count = 3;
        for (int i = 0; i < count; i++) {
            Vec3 offset = new Vec3(0, 5 + level.getRandom().nextFloat() * 2, 2 + level.getRandom().nextFloat());
            offset = offset.yRot(i * Mth.TWO_PI / count);
            Vec3 location = origin.add(offset);
            MagicManager.spawnParticles(level, ParticleHelper.FOG_THUNDER_LIGHT, location.x, location.y, location.z, 2, 1, 1, 1, 1, true);
            MagicManager.spawnParticles(level, ParticleHelper.FOG_THUNDER_DARK, location.x, location.y, location.z, 2, 1, 1, 1, 1, true);
            MagicManager.spawnParticles(level, new ZapParticleOption(location), origin.x, origin.y, origin.z, 1, 1, 0, 1, 0, true);
        }
    }
}
