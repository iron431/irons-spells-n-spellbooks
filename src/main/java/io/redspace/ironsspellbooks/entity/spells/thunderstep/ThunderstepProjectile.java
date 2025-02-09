package io.redspace.ironsspellbooks.entity.spells.thunderstep;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ThunderstepProjectile extends AbstractMagicProjectile {

    public ThunderstepProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThunderstepProjectile(Level levelIn, Entity shooter) {
        this(EntityRegistry.THUNDERSTEP_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    protected boolean inGround;

    @Override
    public void tick() {
        if (!inGround) {
            super.tick();
        } else {
            deltaMovementOld = getDeltaMovement();
            if (tickCount > EXPIRE_TIME) {
                discard();
                return;
            }
            if (shouldFall()) {
                inGround = false;
                this.setDeltaMovement(getDeltaMovement().normalize().scale(0.05f));
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        Vec3 vec3 = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.inGround = true;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return false;
    }

    private boolean shouldFall() {
        return this.inGround && this.level.noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
