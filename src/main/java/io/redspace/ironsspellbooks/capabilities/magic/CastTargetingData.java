package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class CastTargetingData implements ICastData {
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
        var target = getTarget(level);
        return target == null ? null : target.position();
    }
}