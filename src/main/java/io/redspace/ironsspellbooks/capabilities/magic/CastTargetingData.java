package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class CastTargetingData implements CastData {
    //private Entity castingEntity;
    //private LivingEntity targetEntity;
    private UUID targetUUID;

    public CastTargetingData(LivingEntity target) {
        //this.targetEntity = target;
        this.targetUUID = target.getUUID();
    }

    @Override
    public void reset() {

    }

    @Nullable
    public LivingEntity getTarget(ServerLevel level) {
        return (LivingEntity) level.getEntity(targetUUID);
    }

    @Nullable
    public Vec3 getTargetPosition(ServerLevel level) {
        return getTarget(level) == null ? null : getTarget(level).position();
    }
}