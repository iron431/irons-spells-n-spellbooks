package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.BlightEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

public class BlightSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(90)
            .build();

    public BlightSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 50;
        this.baseManaCost = 60;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.reduced_healing",
                        Utils.stringTruncation((1 + amplifier) * BlightEffect.HEALING_PER_LEVEL * -100, 1)),
                Component.translatable("ui.irons_spellbooks.reduced_damage",
                        Utils.stringTruncation((1 + amplifier) * BlightEffect.DAMAGE_PER_LEVEL * -100, 1)),
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
        return PlayableSound.standard(SoundRegistry.BLIGHT_BEGIN).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.POISON_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() - 1);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20 * 30));
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(castContext, 0.35f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        LivingEntity target = SkillcastingUtils.getTargetedLivingEntity(level, castContext);
        if (target != null) {
            target.addEffect(new MobEffectInstance(
                    MobEffectRegistry.BLIGHT,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0)));
        }
    }
}
