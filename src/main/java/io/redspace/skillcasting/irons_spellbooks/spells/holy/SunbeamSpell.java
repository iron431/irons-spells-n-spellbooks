package io.redspace.skillcasting.irons_spellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SunbeamSpell extends AbstractSpellSkill {

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
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, 48, 0.5f, false);
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        Vec3 spawn = null;
        SunbeamEntity sunbeam = new SunbeamEntity(level);
        if (level instanceof ServerLevel serverLevel) {
            var targetData = castContext.getOrNull(SkillcastingComponentTypes.TARGETED_ENTITIES);
            LivingEntity target = targetData != null ? targetData.getFirstLivingEntityTarget(serverLevel) : null;
            if (target != null) {
                spawn = target.position();
                sunbeam.setTarget(target);
            }
        }
        if (spawn == null) {
            HitResult raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 48f)
                    .checkForBlocks(true)
                    .build();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
            } else {
                spawn = Utils.moveToRelativeGroundLevel(level,
                        raycast.getLocation().subtract(castContext.direction().normalize()).add(0, 2, 0), 3, 18);
            }
        }

        sunbeam.setOwner(caster);
        sunbeam.moveTo(spawn);
        sunbeam.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        level.addFreshEntity(sunbeam);
        level.playSound(null, sunbeam.blockPosition(), SoundRegistry.SUNBEAM_WINDUP.get(), SoundSource.NEUTRAL, 3.5f, 1);
    }
}
