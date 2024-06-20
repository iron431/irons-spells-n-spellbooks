package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.spells.ICastData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;


public class TargetEntityCastData implements ICastData {
    //private Entity castingEntity;
    //private LivingEntity targetEntity;
    private final UUID targetUUID;

    public TargetEntityCastData(LivingEntity target) {
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

    public UUID getTargetUUID() {
        return targetUUID;
    }

    @Nullable
    public Vec3 getTargetPosition(ServerLevel level) {
        var target = getTarget(level);
        return target == null ? null : target.position();
    }
}