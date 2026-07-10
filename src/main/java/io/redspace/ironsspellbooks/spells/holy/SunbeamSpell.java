package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SunbeamSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public SunbeamSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1.5f;
        this.castTime = 0;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)));
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
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 48f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, 0.5f, false);
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 spawn = SkillcastingUtils.getTargetedEntityPosition(level, castContext)
                .orElse(RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                .checkForBlocks(true)
                .bbInflation(0.35f)
                .build()
                .getLocation());
        spawn = Utils.moveToRelativeGroundLevel(level, spawn, 18);

        SunbeamEntity sunbeam = new SunbeamEntity(level);
        LivingEntity target = SkillcastingUtils.getTargetedLivingEntity(level, castContext);
        if (target != null) {
            sunbeam.setTarget(target);
        }
        sunbeam.setOwner(castContext.asEntityCaster());
        sunbeam.moveTo(spawn);
        sunbeam.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        level.addFreshEntity(sunbeam);
        level.playSound(null, sunbeam.blockPosition(), SoundRegistry.SUNBEAM_WINDUP.get(), SoundSource.NEUTRAL, 3.5f, 1);
    }
}
