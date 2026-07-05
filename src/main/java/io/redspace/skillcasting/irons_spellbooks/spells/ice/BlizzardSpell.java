package io.redspace.skillcasting.irons_spellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.BlizzardAoe;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class BlizzardSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(22)
            .build();

    public BlizzardSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 3;
        this.castTime = 25;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.duration",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
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
        return PlayableSound.standard(SoundRegistry.CONE_OF_COLD_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 2f + 6f * getSpellPowerMultiplier(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (20 * (10 + 1.5f * castContext.getSkillLevel())));
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext,
                castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f).intValue(), 0.15f, false);
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 spawn = null;
        if (level instanceof ServerLevel serverLevel) {
            var targetData = castContext.getOrNull(SkillcastingComponentTypes.TARGETED_ENTITIES);
            if (targetData != null) {
                Entity target = targetData.getFirstEntityTarget(serverLevel);
                if (target != null) {
                    spawn = target.position();
                }
            }
        }
        if (spawn == null) {
            spawn = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION,
                            castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f))
                    .checkForBlocks(true)
                    .bbInflation(0.15f)
                    .build()
                    .getLocation();
        }
        spawn = Utils.moveToRelativeGroundLevel(level, spawn, 6);

        int duration = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0);
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);

        BlizzardAoe aoe = new BlizzardAoe(EntityRegistry.BLIZZARD_AOE.get(), level);
        aoe.moveTo(spawn);
        aoe.setOwner(castContext.asEntityCaster());
        aoe.setRadius(radius);
        aoe.setDuration(duration);
        aoe.setDeltaMovement(castContext.direction().multiply(1, 0, 1).normalize().scale(0.05f));
        level.addFreshEntity(aoe);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST;
    }
}
