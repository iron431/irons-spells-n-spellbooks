package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ChainLightning;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class ChainLightningSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public ChainLightningSpell() {
        this.manaCostPerLevel = 7;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.max_victims", castContext.getOrDefault(SkillcastingComponentTypes.MAX_TARGETS, 0)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1))
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
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(castContext, 32, 0.35f);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.MAX_TARGETS, 3 + castContext.getSkillLevel());
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 1f + getSpellPower(castContext) * .5f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        var targetEntity = SkillcastingUtils.getTargetedEntity(level, castContext);
        if (targetEntity == null) {
            return;
        }
        ChainLightning chainLightning = new ChainLightning(level, castContext.asEntityCaster(), targetEntity);
        chainLightning.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        chainLightning.range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        chainLightning.maxConnections = castContext.getOrDefault(SkillcastingComponentTypes.MAX_TARGETS, 0);
        level.addFreshEntity(chainLightning);
    }
}
