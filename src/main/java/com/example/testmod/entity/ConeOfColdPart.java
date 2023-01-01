package com.example.testmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

import net.minecraft.world.entity.Pose;
import net.minecraftforge.entity.PartEntity;

public class ConeOfColdPart extends PartEntity<ConeOfColdProjectile> {

    public final ConeOfColdProjectile parentEntity;
    public final String name;
    private final EntityDimensions size;

    public ConeOfColdPart(ConeOfColdProjectile coneOfColdProjectile, String name, float scaleX, float scaleY) {
        super(coneOfColdProjectile);
        this.size = EntityDimensions.scalable(scaleX, scaleY);
        this.refreshDimensions();
        this.parentEntity = coneOfColdProjectile;
        this.name = name;
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