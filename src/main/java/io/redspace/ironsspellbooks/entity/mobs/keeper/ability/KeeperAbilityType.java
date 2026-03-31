package io.redspace.ironsspellbooks.entity.mobs.keeper.ability;

import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.EventKeyframeHandler;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.MobAbilityType;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.MovementSpline;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import org.jetbrains.annotations.Nullable;

public class KeeperAbilityType extends MobAbilityType<KeeperEntity> {
    private final float rangeMultiplier;

    public KeeperAbilityType(@Nullable MovementSpline movementSpline, EventKeyframeHandler<KeeperEntity> keyframeHandler, int duration, String animation, InstanceFactory<KeeperEntity> factory, float rangeMultiplier) {
        super(movementSpline, keyframeHandler, duration, animation, factory);
        this.rangeMultiplier = rangeMultiplier;
    }

    public KeeperAbilityType(@Nullable MovementSpline movementSpline, EventKeyframeHandler<KeeperEntity> keyframeHandler, int duration, String animation) {
        super(movementSpline, keyframeHandler, duration, animation);
        this.rangeMultiplier = 1f;
    }

    public float getRangeMultiplier() {
        return rangeMultiplier;
    }

}
