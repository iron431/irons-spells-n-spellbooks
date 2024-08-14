package io.redspace.ironsspellbooks.entity.dragon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

public class DragonPartEntity extends PartEntity<DragonEntity> {
    public final DragonEntity parentMob;
    private final EntityDimensions size;
    private final Vec3 baseOffset;

    public DragonPartEntity(DragonEntity pParentMob, Vec3 offset16, float pWidth, float pHeight) {
        super(pParentMob);
        this.size = EntityDimensions.scalable(pWidth, pHeight);
        this.parentMob = pParentMob;
        this.refreshDimensions();
        this.baseOffset = offset16.scale(1 / 16f);
    }

    public void positionSelf() {
        Vec3 parentPos = parentMob.position();
        float y = -parentMob.yBodyRot + Mth.HALF_PI;
        Vec3 newVector = parentPos.add(baseOffset.scale(parentMob.getScale()).yRot(y * Mth.DEG_TO_RAD));
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

    @Nullable
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
