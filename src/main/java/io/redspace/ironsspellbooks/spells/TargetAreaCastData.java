package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TargetAreaCastData extends EntityCastData {

    Vec3 center;

    public TargetAreaCastData(Vec3 center, TargetedAreaEntity entity) {
        super(entity);
        this.center = center;
    }

    public Vec3 getCenter() {
        return center;
    }

    @Override
    public TargetedAreaEntity getCastingEntity() {
        return (TargetedAreaEntity) super.getCastingEntity();
    }
}