package io.redspace.ironsspellbooks.entity.dragon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.function.Function;

public class DragonPartEntity extends PartEntity<DragonEntity> {
    public final DragonEntity parentMob;
    private final EntityDimensions size;
    private final Vec3 baseOffset;
    private final boolean followTorso;
    private final Function<DragonEntity.BodyVisualOffsets, Vec3> customPositioning;

    public DragonPartEntity(DragonEntity pParentMob, Vec3 offset16, float pWidth, float pHeight, Function<DragonEntity.BodyVisualOffsets, Vec3> customPositioning, boolean followTorso) {
        super(pParentMob);
        this.size = EntityDimensions.scalable(pWidth, pHeight);
        this.parentMob = pParentMob;
        this.refreshDimensions();
        this.baseOffset = offset16.scale(0.0625f);
        this.customPositioning = customPositioning;
        this.followTorso = followTorso;
    }

    public DragonPartEntity(DragonEntity pParentMob, Vec3 offset16, float pWidth, float pHeight) {
        this(pParentMob, offset16, pWidth, pHeight, (offset) -> Vec3.ZERO, true);
    }

    public DragonPartEntity(DragonEntity pParentMob, Vec3 offset16, float pWidth, float pHeight, Function<DragonEntity.BodyVisualOffsets, Vec3> customPositioning) {
        this(pParentMob, offset16, pWidth, pHeight, customPositioning, true);
    }

    public void positionSelf(DragonEntity.BodyVisualOffsets offsets) {
        Vec3 parentPos = parentMob.position();
        Vec3 localVec = parentMob.rotateWithBody(baseOffset.add(customPositioning.apply(offsets)));
        if (followTorso) {
            localVec = localVec.add(new Vec3(0, offsets.torsoY() * 0.0625f, 0));
        }
        hardSetPos(parentPos.add(localVec.scale(parentMob.getScale())));
    }

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
