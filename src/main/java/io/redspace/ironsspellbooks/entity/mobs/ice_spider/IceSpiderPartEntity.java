package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

public class IceSpiderPartEntity extends PartEntity<IceSpiderEntity> {
    public final IceSpiderEntity parentMob;
    private final EntityDimensions size;
    private final Vec3 baseOffset;

    public IceSpiderPartEntity(IceSpiderEntity pParentMob, Vec3 offset16, float pWidth, float pHeight) {
        super(pParentMob);
        this.size = EntityDimensions.scalable(pWidth, pHeight);
        this.parentMob = pParentMob;
        this.refreshDimensions();
        this.baseOffset = offset16.scale(0.0625f);
    }

    public void positionSelf() {
        Vec3 parentPos = parentMob.position();
        Vec3 localVec = parentMob.rotateWithBody(baseOffset);
        hardSetPos(parentPos.add(localVec.scale(parentMob.getScale())));
    }

//    @Override
//    public boolean canBeCollidedWith() {
//        return true;
//    }
//
//    @Override
//    public boolean canCollideWith(Entity entity) {
//        return !this.isPassengerOfSameVehicle(entity)
//                && entity.getY() >= getY() + size.height() - 0.01f;
//    }

    private void hardSetPos(Vec3 newVector) {
        this.setPos(newVector);
        this.setDeltaMovement(newVector);
        var vec3 = this.position();
        this.xo = vec3.x;
        this.yo = vec3.y;
        this.zo = vec3.z;
        this.xOld = vec3.x;
        this.yOld = vec3.y;
        this.zOld = vec3.z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return parentMob.hurt(this, pSource, pAmount);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return this.parentMob.getPickResult();
    }

    @Override
    public boolean is(Entity pEntity) {
        return this == pEntity || this.parentMob == pEntity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return this.size.scale(parentMob.getScale());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
