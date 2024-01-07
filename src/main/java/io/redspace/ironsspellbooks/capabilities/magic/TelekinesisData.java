package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.world.entity.LivingEntity;

public class TelekinesisData extends CastTargetingData {
    private float distance;

    public TelekinesisData(float distance, LivingEntity target) {
        super(target);
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}