package io.redspace.ironsspellbooks.entity.mobs.goals.abilities;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframe.EventKeyframe;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class MobAbilityInstance<T extends Mob & IAbilityHandler<T>> {
    protected final MobAbilityType<T> type;
    protected final Vec3 startPos;
    protected final T entity;
    protected int currentTick;

    public MobAbilityInstance(MobAbilityType<T> type, T entity) {
        this.type = type;
        this.startPos = Utils.moveToRelativeGroundLevel(entity.level, entity.position(), 5);
        this.entity = entity;
    }

    public void onStart() {
        entity.serverTriggerAnimation(type.getAnimation());
    }

    public void onFinish() {

    }

    public final int getCurrentTick() {
        return currentTick;
    }

    public MobAbilityType<T> getType() {
        return type;
    }

    public Vec3 getStartPos() {
        return startPos;
    }

    public T getEntity() {
        return entity;
    }

    public boolean isFinished() {
        return currentTick >= type.duration;
    }

    public void tick() {
        handleAbilityMovementSpline();
        EventKeyframe keyframe = type.keyframeHandler.getEvent(currentTick);
        if (keyframe != null) {
            keyframe.onEvent(entity);
        }
        currentTick++;
    }

    protected void handleAbilityMovementSpline() {
        Vec3 desiredPos = type.movementSpline.getInterpolatedPosition(currentTick + 3).scale(entity.getScale()).yRot(-entity.getYRot() * Mth.DEG_TO_RAD).add(startPos);
        Vec3 deltaMovement = entity.getDeltaMovement();
        Vec3 desiredMotion = desiredPos.subtract(entity.position());
        Vec3 interpolatedMotion = desiredMotion.subtract(deltaMovement).scale(0.6f).add(deltaMovement);
        if (!entity.isNoGravity()) {
            interpolatedMotion = interpolatedMotion.add(0, -0.1, 0);
        }
        entity.setDeltaMovement(interpolatedMotion.scale(1f));
    }
}
