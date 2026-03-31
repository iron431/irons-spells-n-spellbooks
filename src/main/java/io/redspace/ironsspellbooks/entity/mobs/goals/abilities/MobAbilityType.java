package io.redspace.ironsspellbooks.entity.mobs.goals.abilities;

import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public class MobAbilityType<T extends Mob & IAbilityHandler<T>> {
    @Nullable
    protected final MovementSpline movementSpline;
    protected final EventKeyframeHandler<T> keyframeHandler;
    protected final int duration;
    protected final String animation;

    protected final InstanceFactory<T> factory;

    public interface InstanceFactory<T extends Mob & IAbilityHandler<T>> {
        MobAbilityInstance<T> create(MobAbilityType<T> type, T entity);
    }

    public MobAbilityType(@Nullable MovementSpline movementSpline, EventKeyframeHandler<T> keyframeHandler, int duration, String animation, InstanceFactory<T> factory) {
        this.movementSpline = movementSpline;
        this.keyframeHandler = keyframeHandler;
        this.duration = duration;
        this.animation = animation;
        this.factory = factory;
    }

    public MobAbilityType(@Nullable MovementSpline movementSpline, EventKeyframeHandler<T> keyframeHandler, int duration, String animation) {
        this(movementSpline, keyframeHandler, duration, animation, MobAbilityInstance::new);
    }

    public MobAbilityInstance<T> createInstance(T entity) {
        return factory.create(this, entity);
    }

    public String getAnimation() {
        return animation;
    }

    public int getDuration() {
        return duration;
    }
}
