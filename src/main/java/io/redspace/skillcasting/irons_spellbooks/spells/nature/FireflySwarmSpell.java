package io.redspace.skillcasting.irons_spellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.firefly_swarm.FireflySwarmProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.PositionAnchor;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FireflySwarmSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public FireflySwarmSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1))
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
        return PlayableSound.standard(SoundRegistry.FIREFLY_SPELL_PREPARE).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext) / 3f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, FireflySwarmProjectile.DEFAULT_RADIUS);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(castContext, 32, 0.35f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 spawn = null;
        Entity target = null;

        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null) {
            target = targetData.getFirstEntityTarget(serverLevel);
            if (target != null) {
                spawn = target.position();
            }
        }
        if (spawn == null) {
            HitResult raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 32f)
                    .checkForBlocks(true)
                    .build();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                target = ((EntityHitResult) raycast).getEntity();
                spawn = target.position();
            } else {
                spawn = Utils.moveToRelativeGroundLevel(level,
                        raycast.getLocation().subtract(castContext.direction().normalize()).add(0, 2, 0), 5);
            }
        }

        FireflySwarmProjectile fireflies = new FireflySwarmProjectile(
                level, castContext.asEntityCaster(), target,
                castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        fireflies.setRadius(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, FireflySwarmProjectile.DEFAULT_RADIUS));
        fireflies.moveTo(spawn.add(0, 0.5, 0));
        level.addFreshEntity(fireflies);
    }
}
