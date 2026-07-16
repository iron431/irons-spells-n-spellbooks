package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class EvasionSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(180)
            .build();

    public EvasionSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.hits_dodged",
                (int) getSpellPower(castContext) + 1));
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, (int) getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 60 * 20);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.EVASION,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0),
                    false, false, true));
        }
    }
}
