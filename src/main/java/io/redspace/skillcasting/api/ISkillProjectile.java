package io.redspace.skillcasting.api;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;

public interface ISkillProjectile {

    void setRadius(float radius);

    float getRadius();

    void setDamage(float damage);

    float getDamage();

    void setPierceLevel(int pierceLevel);

    int getPierceLevel();

    void setRicochetLevel(int ricochetLevel);

    int getRicochetLevel();

    void setCursorHoming(boolean cursorHoming);

    void setProjectileSpeed(float speed);

    void setHomingTarget(@NotNull Entity target);

    default void setHealing(float healing) {
        // Healing is not expected to be a commonly supported feature, so implementation is not required
    }

    default void copyFrom(ISkillProjectile parent) {
        this.setRadius(parent.getRadius());
        this.setDamage(parent.getDamage());
        this.setPierceLevel(parent.getPierceLevel());
        this.setRicochetLevel(parent.getRicochetLevel());
        // fixme: expose healing? getter is not required
//        this.setHealing();
    }

    default void applyContext(CastContext context) {
        context.find(SkillcastingComponentTypes.CAST_RADIUS).ifPresent(this::setRadius);
        context.find(SkillcastingComponentTypes.DAMAGE).ifPresent(this::setDamage);
        context.find(SkillcastingComponentTypes.HEALING).ifPresent(this::setHealing);
        context.find(SkillcastingComponentTypes.PROJECTILE_PIERCE).ifPresent(this::setPierceLevel);
        context.find(SkillcastingComponentTypes.PROJECTILE_RICOCHET).ifPresent(this::setRicochetLevel);

        if (context.has(SkillcastingComponentTypes.CURSOR_HOMING)) {
            setCursorHoming(true);
        } /*else if (context.level() instanceof ServerLevel serverLevel) {
        // todo: decide how to store entity homing data
        }*/
    }

    default void shootFromContext(Projectile self, CastContext castContext) {
        shootFromContext(self, castContext, 0f);
    }

    default void shootFromContext(Projectile self, CastContext castContext, float inaccuracy) {
        applyContext(castContext);
        var trajectory = castContext.direction();
        self.shoot(trajectory.x, trajectory.y, trajectory.z, castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_SPEED, 1.0f), inaccuracy);
    }
}
