package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.CounterSpellEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.recast.RecastManager;
import io.redspace.skillcasting.data.recast.RecastResult;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CounterspellSpell extends AbstractSpell {

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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(io.redspace.skillcasting.registry.SkillcastingComponentTypes.CAST_RANGE, 30f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 start = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 forward = castContext.direction();
        Vec3 end = start.add(forward.scale(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f)));
        List<HitResult> hitResults = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                .checkForBlocks(true)
                .bbInflation(0.35f)
                .filter(Utils::validAntiMagicTarget)
                .performRaycastWithPiercing(-1);
        for (HitResult hitResult : hitResults) {
            CasterRef target = resolveCasterFromHit(level, hitResult);
            if (target == null) {
                continue;
            }
            if (NeoForge.EVENT_BUS.post(new CounterSpellEvent.Pre(castContext.caster(), target)).isCanceled()) {
                continue;
            }
            boolean didWork = false;
            if (target.get() instanceof AntiMagicSusceptible antiMagicSusceptible) {
                boolean isOwnSummonAndIsInCombat = antiMagicSusceptible instanceof IMagicSummon summon && summon instanceof Mob mob && mob.isAggressive() && summon.getSummoner() == castContext.asEntityCaster();
                if (!isOwnSummonAndIsInCombat) {
                    didWork = true;
                    antiMagicSusceptible.onAntiMagic(MagicData.get(castContext.caster().get()));
                }
            }
            SkillcastingData skillcastingData = SkillcastingData.get(target.get());
            RecastManager recasts = skillcastingData.recasts();
            if (skillcastingData.isCasting()) {
                didWork = true;
                SkillcastingManager.cancelCast(target, CastEndReason.INTERRUPTED, true);
            }
            if (recasts.hasRecastsActive()) {
                for (RecastInstance instance : recasts.getActiveRecasts()) {
                    if (!instance.components().has(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA.get())) {
                        didWork = true;
                        recasts.removeRecast(target, instance.skill(), RecastResult.INTERRUPTED);
                    }
                }
            }
            if (target.get() instanceof LivingEntity livingEntity) {
                for (Holder<MobEffect> mobEffect : livingEntity.getActiveEffectsMap().keySet().stream().toList()) {
                    if (mobEffect.value() instanceof MagicMobEffect) {
                        didWork = true;
                        livingEntity.removeEffect(mobEffect);
                    }
                }
            }
            if (didWork) {
                NeoForge.EVENT_BUS.post(new CounterSpellEvent.Post(castContext.caster(), target));
                end = hitResult.getLocation();
                break;
            }
        }
        double distance = castContext.position(PositionAnchor.CASTING_POSITION).distanceTo(end);
        for (float i = 1; i < distance; i += .5f) {
            Vec3 pos = start.add(forward.scale(i));
            MagicManager.spawnParticles(level, ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
        }
    }

    private @Nullable CasterRef resolveCasterFromHit(ServerLevel level, HitResult hitResult) {
        CasterRef caster;
        if (hitResult.getType() == HitResult.Type.MISS) {
            return null;
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            caster = CasterRef.entity(((EntityHitResult) hitResult).getEntity());
        } else {
            BlockEntity blockEntity = level.getBlockEntity(((BlockHitResult) hitResult).getBlockPos());
            if (blockEntity == null) {
                return null;
            }
            caster = CasterRef.block(blockEntity);
        }
        return caster;
    }
}
