package io.redspace.skillcasting.irons_spellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;

public class IceTombSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.healing", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(30)
            .build();

    public IceTombSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
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
        castContext.set(SkillcastingComponentTypes.HEALING, 1 * Mth.sqrt(getSpellPowerMultiplier(castContext)));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (80 + castContext.getSkillLevel() * 20 * Mth.sqrt(getSpellPowerMultiplier(castContext))));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof Entity entity)) {
            // todo: just spit out a tomb anyways?
            // fixme: with upcoming buff application vfx, we should probably be more selective in the logic we skip rather than nuke the entire spellcast
            //  will cause confusion when theres no feedback of a spellcast acting even with sound/redstone cues
            return;
        }
        IceTombEntity iceTombEntity = new IceTombEntity(level, entity);
        iceTombEntity.moveTo(entity.position());
        iceTombEntity.setDeltaMovement(entity.getDeltaMovement());
        iceTombEntity.setHealing(castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f));
        iceTombEntity.setLifetime(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0));
        level.addFreshEntity(iceTombEntity);
        entity.startRiding(iceTombEntity, true);
    }

    // fixme:
    //  note: the issue with getters like these is the nomenclature. these provide values *using* the cast context, not *from* the cast context
    //  runtime effects should only ever use values *from* the cast context
    //  a better name would be "provideHealing" or "baseHealing" or something better in order to indicate the distinction.
//
//    public float getDuration(CastContext castContext) {
//        return 80 + castContext.getSkillLevel() * 20 * Mth.sqrt(getSpellPowerMultiplier(castContext));
//    }
//
//    public float getHealing(CastContext castContext) {
//        return 1 * Mth.sqrt(getSpellPowerMultiplier(castContext));
//    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_TWO_HANDS;
    }
}
