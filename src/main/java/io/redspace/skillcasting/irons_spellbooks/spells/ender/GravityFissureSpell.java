package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class GravityFissureSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(45)
            .build();

    public GravityFissureSpell() {
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 150;
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
        return PlayableSound.standard(SoundRegistry.GRAVITY_FISSURE_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.of(SoundRegistry.BLACK_HOLE_CAST, 4).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 3.5f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);
        Vec3 spawn = castContext.position().add(castContext.direction().scale(2)).subtract(0, radius, 0);

        BlackHole blackHole = new BlackHole(level, castContext.asEntityCaster());
        blackHole.setRadius(radius);
        blackHole.setDamage(0);
        blackHole.moveTo(spawn);
        blackHole.setDuration(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0));
        blackHole.setDeltaMovement(castContext.direction().normalize().scale(.2));
        level.addFreshEntity(blackHole);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_WAVY_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ONE_HANDED_VERTICAL_UPSWING_ANIMATION;
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }
}
