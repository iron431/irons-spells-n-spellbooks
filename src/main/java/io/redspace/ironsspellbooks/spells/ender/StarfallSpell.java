package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.StarfallCastComponent;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StarfallSpell extends AbstractSpell {

    private static final float RADIUS = 6f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(16)
            .build();

    public StarfallSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 0.5f;
        this.castTime = 160;
        this.baseManaCost = 5;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.ENDER_CAST).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return Optional.empty();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 40f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, RADIUS);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (!castContext.has(SpellcastingComponentTypes.STARFALL_DATA)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(level,
                    RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                            .checkForBlocks(true)
                            .build()
                            .getLocation(), 12);
            castContext.set(SpellcastingComponentTypes.STARFALL_DATA, new StarfallCastComponent(targetArea));
        }
    }

    @Override
    public void onServerCastTick(CastContext castContext) {
        if (!(castContext.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        StarfallCastComponent castData = castContext.getOrNull(SpellcastingComponentTypes.STARFALL_DATA);
        if (castData == null) {
            return;
        }
        Entity caster = castContext.asEntityCaster();
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, RADIUS);
        var activeCast = castContext.getSkillcastingData().getActiveCast();
        if (activeCast == null) {
            return;
        }
        int tick = activeCast.remainingTicks(serverLevel.getGameTime()) - 1;

        if (tick % 20 == 0) {
            List<Entity> tracked = serverLevel.getEntities(caster, AABB.ofSize(castData.center, radius * 3, radius, radius * 3),
                    e -> e instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(caster, e));
            castData.updateTrackedEntities(tracked);
        }
        if (tick % 4 == 0) {
            for (int i = 0; i < 2; i++) {
                Vec3 center = castData.center;
                Vec3 weightedArea = Vec3.ZERO;
                List<Entity> trackedEntities = resolveTrackedEntities(serverLevel, castData);
                for (Entity target : trackedEntities) {
                    weightedArea = weightedArea.add(target.position().subtract(center).scale(1f / trackedEntities.size()));
                }
                var spawnRadius = Mth.clampedLerp(radius, radius * 0.5, weightedArea.length() / radius);
                Vec3 spawnTarget = Utils.moveToRelativeGroundLevel(serverLevel, center.add(weightedArea)
                        .add(new Vec3(0, 0, serverLevel.getRandom().nextFloat() * spawnRadius)
                                .yRot(serverLevel.getRandom().nextInt(360) * Mth.DEG_TO_RAD)), 3).add(0, 0.5, 0);
                var trajectory = new Vec3(0.15f, -0.85f, 0).normalize();
                Vec3 spawn = Utils.raycastForBlock(serverLevel, spawnTarget, spawnTarget.add(trajectory.scale(-12)), ClipContext.Fluid.NONE)
                        .getLocation().add(trajectory);
                shootComet(serverLevel, castContext, spawn, trajectory);
                MagicManager.spawnParticles(serverLevel, ParticleHelper.COMET_FOG, spawn.x, spawn.y, spawn.z, 1, 1, 1, 1, 1, false);
                MagicManager.spawnParticles(serverLevel, ParticleHelper.COMET_FOG, spawn.x, spawn.y, spawn.z, 1, 1, 1, 1, 1, true);
            }
        }
    }

    private List<Entity> resolveTrackedEntities(ServerLevel level, StarfallCastComponent castData) {
        List<Entity> entities = new ArrayList<>();
        for (var uuid : castData.trackedEntityIds) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    public static void particleTrail(Level level, Vec3 a, Vec3 b, ParticleOptions particleType) {
        double d = a.distanceTo(b) * 4;
        for (int i = 0; i < d; i++) {
            double p = i / d;
            Vec3 vec = a.add(b.subtract(a).scale(p));
            MagicManager.spawnParticles(level, particleType, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0, true);
        }
    }

    private void shootComet(Level level, CastContext castContext, Vec3 spawn, Vec3 trajectory) {
        Comet comet = new Comet(level, castContext.asEntityCaster());
        comet.setPos(spawn.add(-1, 0, 0));
        comet.shoot(trajectory.x, trajectory.y, trajectory.z, 1f, 6f);
        comet.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        comet.setRadius(2f);
        level.addFreshEntity(comet);
        level.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS,
                3.0f, 0.7f + Utils.random.nextFloat() * 0.3f);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }
}
