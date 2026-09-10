package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class IceTombSpell extends AbstractSpell {
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
        castContext.set(SkillcastingComponentTypes.HEALING, provideHealing(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, provideDuration(castContext));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof Entity entity)) {
            return;
        }
        IceTombEntity iceTombEntity = new IceTombEntity(level, entity);
        SkillcastingUtils.attachToContext(iceTombEntity, castContext);
        iceTombEntity.moveTo(entity.position());
        iceTombEntity.setDeltaMovement(entity.getDeltaMovement());
        iceTombEntity.setHealing(castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f));
        iceTombEntity.setLifetime(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0));
        level.addFreshEntity(iceTombEntity);
        entity.startRiding(iceTombEntity, true);
    }

    public int provideDuration(CastContext castContext) {
        return (int) (80 + castContext.getSkillLevel() * 20 * Mth.sqrt(getSpellPowerMultiplier(castContext)));
    }

    public float provideHealing(CastContext castContext) {
        return 1 * Mth.sqrt(getSpellPowerMultiplier(castContext));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_TWO_HANDS;
    }
}
