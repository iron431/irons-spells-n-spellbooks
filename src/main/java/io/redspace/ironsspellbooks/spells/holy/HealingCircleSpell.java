package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.HealingAoe;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class HealingCircleSpell extends AbstractSpell {

    private static final float RADIUS = 5f;
    private static final int DURATION = 200;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(25)
            .build();

    public HealingCircleSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 0.5f;
        this.spellPowerPerLevel = 0.25f;
        this.castTime = 20;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_healing",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(DURATION, 1)));
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
        return PlayableSound.standard(SoundRegistry.CLOUD_OF_REGEN_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.HEALING, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, RADIUS);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, DURATION);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, 0.15f, false);
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
        spawn = Utils.moveToRelativeGroundLevel(level, spawn, 6);

        int duration = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, DURATION);
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, RADIUS);
        float healing = castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f);

        HealingAoe aoeEntity = new HealingAoe(level);
        aoeEntity.setOwner(castContext.asEntityCaster());
        aoeEntity.setCircular();
        aoeEntity.setRadius(radius);
        aoeEntity.setDuration(duration);
        aoeEntity.setDamage(healing);
        aoeEntity.setPos(spawn);
        level.addFreshEntity(aoeEntity);

        TargetedAreaEntity visualEntity = TargetedAreaEntity.createTargetAreaEntity(level, spawn, radius, 0xc80000);
        visualEntity.setDuration(duration);
        visualEntity.setOwner(aoeEntity);
        visualEntity.setShouldFade(true);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

    @Override
    public Vector3f getAccentColor() {
        return new Vector3f(.85f, 0, 0);
    }
}
