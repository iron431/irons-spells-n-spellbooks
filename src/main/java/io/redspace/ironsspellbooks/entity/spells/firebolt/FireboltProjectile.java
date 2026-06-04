package io.redspace.ironsspellbooks.entity.spells.firebolt;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FireboltProjectile extends AbstractMagicProjectile {
    public FireboltProjectile(EntityType<? extends FireboltProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FireboltProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.FIREBOLT_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    public float getSpeed() {
        return 1.75f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.FIREWORK_ROCKET_BLAST));
    }

    @Override
    protected void doImpactSound(Holder<SoundEvent> sound) {
        level.playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 2, 1.2f + Utils.random.nextFloat() * .2f);

    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        var target = entityHitResult.getEntity();
        DamageSources.applyDamage(target, getDamage(), SpellRegistry.FIREBOLT_SPELL.get().getDamageSource(this, getOwner()));
        consumeEntityImpact(entityHitResult, true);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleTypes.LAVA, x, y, z, 5, .1, .1, .1, .25, true);
    }

    @Override
    public void trailParticles() {
        if (tickCount < 2) {
            return;
        }
        Vec3 vel = getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-6) {
            return;
        }

        float radius = 0.25f;
        int steps = 4;
        Vec3 forward = vel.normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 axis1 = forward.cross(worldUp);
        if (axis1.lengthSqr() < 1.0E-6) {
            axis1 = forward.cross(new Vec3(1, 0, 0));
        }
        axis1 = axis1.normalize();
        Vec3 axis2 = forward.cross(axis1);

        double x2 = getX();
        double y2 = getY();
        double z2 = getZ();
        double x1 = x2 - vel.x;
        double y1 = y2 - vel.y;
        double z1 = z2 - vel.z;

        for (int j = 0; j < steps; j++) {
            float t = j / (float) steps;
            double baseX = Mth.lerp(t, x1, x2);
            double baseY = Mth.lerp(t, y1, y2);
            double baseZ = Mth.lerp(t, z1, z2);
            double radians = ((tickCount + t) / 7.5) * Mth.TWO_PI;
            double c = Math.cos(radians) * radius;
            double s = Math.sin(radians) * radius;
            double x = baseX + axis1.x * c + axis2.x * s;
            double y = baseY + axis1.y * c + axis2.y * s;
            double z = baseZ + axis1.z * c + axis2.z * s;
            Vec3 jitter = Utils.getRandomVec3(0.05f);
            level.addParticle(ParticleHelper.EMBERS, true, x, y, z, jitter.x, jitter.y, jitter.z);
        }
    }
}
