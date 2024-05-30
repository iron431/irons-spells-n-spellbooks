package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.world.entity.LivingEntity;

public class TelekinesisData extends TargetEntityCastData {
    private float distance;
    private final int minDistance;

    public TelekinesisData(float distance, LivingEntity target, int minDistance) {
        super(target);
        this.distance = distance;
        this.minDistance = minDistance;
    }

    public float getDistance() {
        return Math.max(distance, minDistance);
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}