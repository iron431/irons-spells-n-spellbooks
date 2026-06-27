package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class MagmaBombSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(12)
            .build();

    public MagmaBombSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.AOE_DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1))
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
        return PlayableSound.standard(SoundRegistry.FIRE_BOMB_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.FIRE_BOMB_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, baseSpellPower * getSpellPowerMultiplier(castContext));
        castContext.set(SkillcastingComponentTypes.AOE_DAMAGE, 1 + getSpellPower(castContext) * 0.1f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 3 + getSpellPowerMultiplier(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, 1.0f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        FireBomb orb = new FireBomb(level, castContext.asEntityCaster());
        orb.applyContext(castContext);
        orb.setAoeDamage(castContext.getOrDefault(SkillcastingComponentTypes.AOE_DAMAGE, 0f));
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION);
        orb.setPos(origin.add(castContext.direction()).subtract(0, orb.getBbHeight() / 2, 0));
        orb.shootFromContext(orb, castContext);
        orb.setDeltaMovement(orb.getDeltaMovement().add(0, 0.2, 0));
        level.addFreshEntity(orb);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CHARGED_CAST;
    }
}
