package io.redspace.skillcasting.irons_spellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.ChargeEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;

public class ChargeSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(40)
            .build();

    public ChargeSpell() {
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 8;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int skillLevel = castContext.getSkillLevel();
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(skillLevel * ChargeEffect.SPEED_PER_LEVEL * 100, 0),
                        Component.translatable("attribute.name.generic.movement_speed")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(skillLevel * ChargeEffect.ATTACK_DAMAGE_PER_LEVEL * 100, 0),
                        Component.translatable("attribute.name.generic.attack_damage")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(skillLevel * ChargeEffect.SPELL_POWER_PER_LEVEL * 100, 0),
                        Component.translatable("attribute.irons_spellbooks.lightning_spell_power"))
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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS,
                (int) (30 * 20 * getSpellPowerMultiplier(castContext)));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() - 1);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.CHARGED,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0),
                    false, false, true));
        }
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
