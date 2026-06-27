package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ArrowVolleyEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ArrowVolleySpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int rows = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        int arrowsPerRow = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_PIERCE, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", rows * arrowsPerRow)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public ArrowVolleySpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 0;
        this.castTime = 30;
        this.baseManaCost = 40;
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
        return PlayableSound.standard(SoundRegistry.ARROW_VOLLEY_PREPARE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.EVOKER_CAST_SPELL).toOpt();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 48f).intValue(), 0.25f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 48f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 4 + level);
        castContext.set(SkillcastingComponentTypes.PROJECTILE_PIERCE, 5 + level / 2);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 targetLocation = null;
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null && level instanceof ServerLevel serverLevel) {
            var target = targetData.getFirstEntityTarget(serverLevel);
            if (target != null) {
                targetLocation = target.position();
            }
        }
        if (targetLocation == null) {
            targetLocation = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 100f)
                    .checkForBlocks(true)
                    .build()
                    .getLocation();
        }

        Vec3 casterPos = castContext.position(PositionAnchor.ORIGIN);
        double casterX = casterPos.x;
        double casterZ = casterPos.z;
        float casterYRot = castContext.getYRot();

        Vec3 backward = new Vec3(targetLocation.x - casterX, 0, targetLocation.z - casterZ).normalize().scale(-4);
        Vec3 raycastTarget = Utils.moveToRelativeGroundLevel(level, targetLocation.add(0, 2, 0), 4).add(backward).add(0, 6, 0);
        Vec3 spawnLocation = Utils.raycastForBlock(level, targetLocation, raycastTarget, ClipContext.Fluid.NONE).getLocation();
        spawnLocation = spawnLocation.subtract(targetLocation).scale(.9f).add(targetLocation);
        float dx = Mth.sqrt((float) ((spawnLocation.x - targetLocation.x) * (spawnLocation.x - targetLocation.x) + (spawnLocation.z - targetLocation.z) * (spawnLocation.z - targetLocation.z)));
        float arrowAngleX = dx == 0 ? 70 : (float) (Mth.atan2(dx, (spawnLocation.y - targetLocation.y)) * Mth.RAD_TO_DEG);
        float arrowAngleY = casterX == targetLocation.x && casterZ == targetLocation.z ? (casterYRot - 90) * Mth.DEG_TO_RAD : Utils.getAngle(casterX, casterZ, targetLocation.x, targetLocation.z);

        ArrowVolleyEntity arrowVolleyEntity = new ArrowVolleyEntity(EntityRegistry.ARROW_VOLLEY_ENTITY.get(), level);
        arrowVolleyEntity.moveTo(spawnLocation);
        arrowVolleyEntity.setYRot(arrowAngleY * Mth.RAD_TO_DEG + 90);
        arrowVolleyEntity.setXRot(arrowAngleX + 25);
        arrowVolleyEntity.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        arrowVolleyEntity.setArrowsPerRow(castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_PIERCE, 0));
        arrowVolleyEntity.setRows(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0));
        arrowVolleyEntity.setOwner(castContext.asEntityCaster());
        level.addFreshEntity(arrowVolleyEntity);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setIFrames(0);
    }
}
