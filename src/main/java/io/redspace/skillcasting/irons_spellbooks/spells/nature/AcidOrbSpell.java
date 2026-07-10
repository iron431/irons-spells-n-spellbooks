package io.redspace.skillcasting.irons_spellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrb;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class AcidOrbSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(15)
            .build();

    public AcidOrbSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 15;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int rendAmplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.rend",
                        Utils.stringTruncation((rendAmplifier + 1) * 5, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)));
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
        return PlayableSound.standard(SoundRegistry.ACID_ORB_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.ACID_ORB_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float power = getSpellPower(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, power * 3);
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() + 2);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (power * 20 * 20));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, 1f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        AcidOrb orb = new AcidOrb(level, castContext.asEntityCaster());
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION);
        orb.setPos(origin.add(castContext.direction()).subtract(0, orb.getBoundingBox().getYsize() * 0.5f, 0));
        orb.shootFromContext(orb, castContext);
        orb.setDeltaMovement(orb.getDeltaMovement().add(0, 0.2, 0));
        orb.setRadius(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f));
        orb.setRendLevel(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0));
        orb.setRendDuration(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0));
        level.addFreshEntity(orb);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_SPIT_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SPIT_FINISH_ANIMATION;
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {
        return target.getAttributeValue(Attributes.ARMOR) < 4;
    }
}
