package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrow;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;

public class PoisonArrowSpell extends AbstractSpell {

    private static final float PROJECTILE_BASE_SPEED = 2.5f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public PoisonArrowSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DOT_DAMAGE, 0f), 1)));
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
        return PlayableSound.standard(SoundRegistry.POISON_ARROW_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.POISON_ARROW_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float power = getSpellPower(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, power);
        castContext.set(SkillcastingComponentTypes.DOT_DAMAGE, power * 0.2f);
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, PROJECTILE_BASE_SPEED);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        PoisonArrow magicArrow = new PoisonArrow(level, castContext.asEntityCaster());
        magicArrow.setPos(castContext.position(PositionAnchor.CASTING_POSITION)
                .add(castContext.direction())
                .add(0, magicArrow.getBoundingBox().getYsize() * -0.5f, 0));
        magicArrow.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        magicArrow.setDotDamage(castContext.getOrDefault(SkillcastingComponentTypes.DOT_DAMAGE, 0f));
        magicArrow.shootFromContext(magicArrow, castContext);
        level.addFreshEntity(magicArrow);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.BOW_CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.stop();
    }
}
