package io.redspace.ironsspellbooks.spells.ender;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StarfallSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "starfall");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(16)
            .build();

    public StarfallSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 160;
        this.baseManaCost = 5;

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
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ENDER_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof StarfallCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(world, Utils.raycastForEntity(world, entity, 40, true).getLocation(), 12);
            playerMagicData.setAdditionalCastData(new StarfallCastData(targetArea));
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public static void particleTrail(Level level, Vec3 a, Vec3 b, ParticleOptions particleType) {
        double d = a.distanceTo(b) * 4;
        for (int i = 0; i < d; i++) {
            double p = i / d;
            Vec3 vec = a.add(b.subtract(a).scale(p));
            MagicManager.spawnParticles(level, particleType, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0, true);
        }
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null || !(playerMagicData.getAdditionalCastData() instanceof StarfallCastData castData)) {
            return;
        }
        float radius = getRadius(entity);
        int tick = playerMagicData.getCastDurationRemaining() - 1;
        if (tick % 20 == 0) {
            castData.updateTrackedEntities(level.getEntities(entity, AABB.ofSize(castData.center, radius * 3, radius, radius * 3), e -> e instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(entity, e)));
        }
        if (tick % 4 == 0)
            for (int i = 0; i < 2; i++) {
                Vec3 center = castData.center;
                Vec3 weightedArea = Vec3.ZERO;
                for (Entity target : castData.trackedEntities) {
                    weightedArea = weightedArea.add(target.position().subtract(center).scale(1f / castData.trackedEntities.size()));
                }
                var spawnRadius = Mth.clampedLerp(radius, radius * .5, weightedArea.length() / radius);
                Vec3 spawnTarget = Utils.moveToRelativeGroundLevel(level, center.add(weightedArea).add(new Vec3(0, 0, entity.getRandom().nextFloat() * spawnRadius).yRot(entity.getRandom().nextInt(360) * Mth.DEG_TO_RAD)), 3).add(0, 0.5, 0);
                var trajectory = new Vec3(.15f, -.85f, 0).normalize();
                Vec3 spawn = Utils.raycastForBlock(level, spawnTarget, spawnTarget.add(trajectory.scale(-12)), ClipContext.Fluid.NONE).getLocation().add(trajectory);
                shootComet(level, spellLevel, entity, spawn, trajectory);
                MagicManager.spawnParticles(level, ParticleHelper.COMET_FOG, spawn.x, spawn.y, spawn.z, 1, 1, 1, 1, 1, false);
                MagicManager.spawnParticles(level, ParticleHelper.COMET_FOG, spawn.x, spawn.y, spawn.z, 1, 1, 1, 1, 1, true);
            }
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * .5f;
    }

    private float getRadius(LivingEntity caster) {
        return 6;
    }

    public void shootComet(Level world, int spellLevel, LivingEntity entity, Vec3 spawn, Vec3 trajectory) {
        Comet fireball = new Comet(world, entity);
        fireball.setPos(spawn.add(-1, 0, 0));
        fireball.shoot(trajectory/*new Vec3(.15f, -.85f, 0)*/, .075f);
        fireball.setDamage(getDamage(spellLevel, entity));
        fireball.setExplosionRadius(2f);
        world.addFreshEntity(fireball);
        world.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.0f, 0.7f + Utils.random.nextFloat() * .3f);

    }

    public static class StarfallCastData implements ICastData {
        Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public StarfallCastData(Vec3 center) {
            this.center = center;
        }

        @Override
        public void reset() {
            trackedEntities.clear();
        }

        public void updateTrackedEntities(List<Entity> entities) {
            trackedEntities.clear();
            trackedEntities.addAll(entities);
        }

    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

}
