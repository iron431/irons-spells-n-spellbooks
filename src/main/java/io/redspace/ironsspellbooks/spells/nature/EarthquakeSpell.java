package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.EarthquakeAoe;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class EarthquakeSpell extends AbstractSpell {

    private static final int DURATION_TICKS = 20 * 12;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(16)
            .build();

    public EarthquakeSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 0.25f;
        this.castTime = 40;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DOT_DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.slowness_effect",
                        castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0) + 1),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1))
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
        return PlayableSound.standard(SoundRegistry.EARTHQUAKE_LOOP).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.EARTHQUAKE_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float powerMultiplier = castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f);
        float damage = getSpellPower(castContext);
        castContext.set(SkillcastingComponentTypes.DOT_DAMAGE, damage);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 4 + 4 * powerMultiplier);
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, Math.clamp((int) damage - 2, 0, 2));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, DURATION_TICKS);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, 0.15f, false);
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 spawn = SkillcastingUtils.getTargetedEntityPosition(level, castContext)
                .orElse(RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                .checkForBlocks(true)
                .bbInflation(0.35f)
                .build()
                .getLocation());
        spawn = Utils.moveToRelativeGroundLevel(level, spawn, 6);

        EarthquakeAoe aoeEntity = new EarthquakeAoe(level);
        aoeEntity.moveTo(spawn);
        aoeEntity.setOwner(castContext.asEntityCaster());
        aoeEntity.setCircular();
        aoeEntity.setRadius(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f));
        aoeEntity.setDuration(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, DURATION_TICKS));
        aoeEntity.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DOT_DAMAGE, 0f));
        aoeEntity.setSlownessAmplifier(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0));
        level.addFreshEntity(aoeEntity);
    }
}
