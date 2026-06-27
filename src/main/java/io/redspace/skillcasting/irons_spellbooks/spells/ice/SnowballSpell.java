package io.redspace.skillcasting.irons_spellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.snowball.Snowball;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SnowballSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(12)
            .build();

    public SnowballSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 40;
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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 3.5f + castContext.getSkillLevel() * .5f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (200 * Mth.sqrt(getSpellPowerMultiplier(castContext))));
    }

    @Override
    public Optional<PlayableSound> getCastChannelSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.FROSTWAVE_PREPARE).toOpt();
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Snowball orb = new Snowball(level, castContext.asEntityCaster());
        orb.setPos(castContext.position().add(castContext.direction()));
        orb.shoot(castContext.direction());
        orb.setDeltaMovement(orb.getDeltaMovement().add(0, 0.2, 0));
        orb.setRadius(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f));
        // use damage as duration
        orb.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0));
        level.addFreshEntity(orb);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CHARGED_CAST;
    }
}
