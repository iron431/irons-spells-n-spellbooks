package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.FangSwirlEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FangSwirlSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(28)
            .setAllowCrafting(false)
            .build();

    public FangSwirlSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 4.5f;
        this.spellPowerPerLevel = 0.75f;
        this.castTime = 20;
        this.baseManaCost = 55;
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
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_ATTACK).toOpt();
    }

    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f).intValue(), 0.35f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 4.5f + 0.5f * level * getSpellPowerMultiplier(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 8 * 20);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 dest = null;
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null && level instanceof ServerLevel serverLevel) {
            var target = targetData.getFirstEntityTarget(serverLevel);
            if (target != null) {
                dest = target.position();
            }
        }
        if (dest == null) {
            HitResult raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f))
                    .checkForBlocks(true)
                    .bbInflation(0.35f)
                    .build();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                dest = ((EntityHitResult) raycast).getEntity().position();
            } else {
                dest = raycast.getLocation().subtract(castContext.direction().normalize());
            }
        }
        dest = Utils.moveToRelativeGroundLevel(level, dest, 6);
        Vec3 start = castContext.position(PositionAnchor.ORIGIN);
        float horizontalDist = (float) dest.subtract(start).horizontalDistance();
        int delay = Math.max(8, Math.min(40, Mth.ceil(horizontalDist * 1.5f))) * 2 / 3;

        FangSwirlEntity swirl = new FangSwirlEntity(EntityRegistry.FANG_SWIRL.get(), level);
        swirl.moveTo(dest.x, dest.y, dest.z);
        swirl.setStartPos(start);
        swirl.setDelay(delay);
        swirl.setRadius(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f));
        swirl.setDuration(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 160));
        swirl.setOwner(castContext.asEntityCaster());
        swirl.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        level.addFreshEntity(swirl);
    }
}
