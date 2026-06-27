package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class InvisibilitySpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(45)
            .build();

    public InvisibilitySpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 40;
        this.baseManaCost = 35;
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
        return PlayableSound.standard(SoundEvents.ILLUSIONER_PREPARE_MIRROR).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.TRUE_INVISIBILITY,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    0,
                    false,
                    false,
                    true));
        }
    }
}
