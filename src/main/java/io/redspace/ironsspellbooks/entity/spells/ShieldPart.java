package io.redspace.ironsspellbooks.entity.spells;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.entity.PartEntity;

public class ShieldPart extends PartEntity<AbstractShieldEntity> {

    public final AbstractShieldEntity parentEntity;
    public final String name;
    private final EntityDimensions size;

    public ShieldPart(AbstractShieldEntity shieldEntity, String name, float scaleX, float scaleY) {
        super(shieldEntity);
        this.size = EntityDimensions.scalable(scaleX, scaleY);
        this.refreshDimensions();
        this.parentEntity = shieldEntity;
        this.name = name;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!parentEntity.hurtThisTick) {
            parentEntity.takeDamage(pSource, pAmount, this.position());
            parentEntity.hurtThisTick = true;
        }
        return true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.parentEntity == entity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}