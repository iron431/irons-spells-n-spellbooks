package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.CounterSpellEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.recast.RecastManager;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public class CounterspellSpell extends AbstractSpellSkill {

    public CounterspellSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(10)
            .build();

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 start = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 forward = castContext.direction();
        HitResult hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 80)
                .checkForBlocks(true)
                .bbInflation(0.35f)
                .filter(Utils::validAntiMagicTarget)
                .build();
        // fixme: how to counterspell blocks?
        if (hitResult instanceof EntityHitResult entityHitResult) {
            var hitEntity = entityHitResult.getEntity();
            if (!NeoForge.EVENT_BUS.post(new CounterSpellEvent(
                    castContext.asEntityCaster() instanceof LivingEntity caster ? caster : null,
                    hitEntity)).isCanceled()) {
                MagicData casterMagicData = castContext.asEntityCaster() != null
                        ? MagicData.get(castContext.asEntityCaster()) : null;
                if (hitEntity instanceof AntiMagicSusceptible antiMagicSusceptible) {
                    if (antiMagicSusceptible instanceof IMagicSummon summon
                            && castContext.asEntityCaster() instanceof LivingEntity caster) {
                        if (summon.getSummoner() == caster) {
                            if (summon instanceof Mob mob && mob.getTarget() == null) {
                                antiMagicSusceptible.onAntiMagic(casterMagicData);
                            }
                        } else {
                            antiMagicSusceptible.onAntiMagic(casterMagicData);
                        }
                    } else {
                        antiMagicSusceptible.onAntiMagic(casterMagicData);
                    }
                } else {
                    SkillcastingData data = SkillcastingData.get(hitEntity);
                    if (data.isCasting() || !data.recasts().isEmpty()) {
                        CasterRef casterRef = CasterRef.entity(hitEntity);
                        SkillcastingManager.cancelCast(casterRef, CastEndReason.INTERRUPTED, true);
                        RecastManager recasts = casterRef.skillcastingData().recasts();
                        for (RecastInstance instance : recasts.getActiveRecasts()) {
                            if (!instance.components().has(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA.get())) {
                                SkillcastingManager.removeRecast(casterRef, instance, RecastResult.INTERRUPTED);
                            }
                        }
                    }
                }
                if (hitEntity instanceof LivingEntity livingEntity) {
                    for (Holder<MobEffect> mobEffect : livingEntity.getActiveEffectsMap().keySet().stream().toList()) {
                        if (mobEffect.value() instanceof MagicMobEffect) {
                            livingEntity.removeEffect(mobEffect);
                        }
                    }
                }
            }
        }
        double distance = castContext.position(PositionAnchor.ORIGIN).distanceTo(hitResult.getLocation());
        for (float i = 1; i < distance; i += .5f) {
            Vec3 pos = start.add(forward.scale(i));
            MagicManager.spawnParticles(level, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
        }
    }
}
