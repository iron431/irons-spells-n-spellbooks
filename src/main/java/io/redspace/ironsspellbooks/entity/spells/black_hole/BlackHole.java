package io.redspace.ironsspellbooks.entity.spells.black_hole;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BlackHole extends Projectile implements AntiMagicSusceptible {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.FLOAT);

    public BlackHole(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public BlackHole(Level pLevel, LivingEntity owner) {
        super(EntityRegistry.BLACK_HOLE.get(), pLevel);
        setOwner(owner);
    }

    @Override
    public void onAntiMagic(PlayerMagicData playerMagicData) {
    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
    }

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_RADIUS, 5F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_RADIUS.equals(pKey)) {
            this.refreshDimensions();
            if (getRadius() < .1f)
                this.discard();
        }

        super.onSyncedDataUpdated(pKey);
    }

    public void setRadius(float pRadius) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, pRadius);
        }
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putFloat("Radius", this.getRadius());
        pCompound.putInt("Age", this.tickCount);

        super.addAdditionalSaveData(pCompound);
    }
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.tickCount = pCompound.getInt("Age");

        if (pCompound.getInt("Radius") > 0)
            this.setRadius(pCompound.getFloat("Radius"));

        super.readAdditionalSaveData(pCompound);

    }
    @Override
    public void tick() {
        super.tick();
        List<Entity> entities = level.getEntities(this, this.getBoundingBox());
        for (Entity entity : entities) {
            Vec3 center = this.position().add(0, this.getBoundingBox().getYsize() / 2, 0);
            float distance = (float) center.distanceTo(entity.position());
            float radius = (float) (this.getBoundingBox().getXsize());
            float scale = (float) Math.pow(1 - distance / radius, 4) * .25f;
            Vec3 diff = center.subtract(entity.position()).scale(scale);
            entity.push(diff.x, diff.y, diff.z);
            if (this.tickCount % 10 == 0) {
                if (distance < 3f)
                    entity.hurt(DamageSource.CRAMMING, 2);
            }
        }
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }
}
