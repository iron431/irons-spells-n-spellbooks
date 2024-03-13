package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class LightningStrike extends AoeEntity {
    public LightningStrike(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            return;
        }
        if (tickCount == 1) {
            MagicManager.spawnParticles(level, new ShockwaveParticleOptions(SchoolRegistry.LIGHTNING.get().getTargetingColor(), -1.5f, true), getX(), getY(), getZ(), 1, 0, 0, 0, 0, true);
        }
        if (tickCount == 30) {
            checkHits();
            if (!level.isClientSide) {
                MagicManager.spawnParticles(level, ParticleHelper.ELECTRIC_SPARKS, getX(), getY(), getZ(), 25, .2f, .2f, .2f, .25, true);
                MagicManager.spawnParticles(level, ParticleHelper.FIERY_SPARKS, getX(), getY(), getZ(), 5, .2f, .2f, .2f, .125, true);
                MagicManager.spawnParticles(level, new ZapParticleOption(this.position().add(0, 20, 0)), getX(), getY(), getZ(), 1, 0, 0, 0, 0, true);
            }
        }
        if (this.tickCount > 30) {
            discard();
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {

    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }
}
