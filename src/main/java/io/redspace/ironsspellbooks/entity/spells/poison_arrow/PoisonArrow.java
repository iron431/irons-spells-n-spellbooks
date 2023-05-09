package io.redspace.ironsspellbooks.entity.spells.poison_arrow;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class PoisonArrow extends AbstractMagicProjectile {

    public PoisonArrow(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.POISON_ARROW.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement());
        level.addParticle(ParticleTypes.CRIT, vec3.x, vec3.y, vec3.z, 0, 0, 0);
        level.addParticle(ParticleTypes.SNEEZE, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 15, .1, .1, .1, .5, false);
    }

    @Override
    public float getSpeed() {
        return 2.2f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    public PoisonArrow(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean respectsGravity() {
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
        this.setYRot(this.getYRot() + 180.0F);
        this.yRotO += 180.0F;
    }


}
