package io.redspace.ironsspellbooks.entity.spells.snowball;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FrostField extends AoeEntity {

    public FrostField(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        reapplicationDelay = 1;
    }

    public FrostField(Level level) {
        this(EntityRegistry.FROST_FIELD.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        //todo: cap?
        target.setTicksFrozen(target.getTicksFrozen() + 10);
    }

    @Override
    public float getParticleCount() {
        return 0.8f * getRadius();
    }

    @Override
    protected float particleYOffset() {
        return .25f;
    }

    @Override
    protected float getParticleSpeedModifier() {
        return 1.4f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(ParticleHelper.SNOW_DUST);
    }
}
