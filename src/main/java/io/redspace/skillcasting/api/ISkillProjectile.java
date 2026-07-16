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

    int getEffectDuration();

    void setEffectDuration(int effectDuration);

    int getEffectAmplifier();

    void setEffectAmplifier(int effectAmplifier);

    default void copyFrom(ISkillProjectile parent) {
        this.setRadius(parent.getRadius());
        this.setDamage(parent.getDamage());
        this.setPierceLevel(parent.getPierceLevel());
        this.setRicochetLevel(parent.getRicochetLevel());
        this.setEffectDuration(parent.getEffectDuration());
        this.setEffectAmplifier(parent.getEffectAmplifier());
    }

    default void applyContext(CastContext context) {
        context.find(SkillcastingComponentTypes.CAST_RADIUS).ifPresent(this::setRadius);
        context.find(SkillcastingComponentTypes.DAMAGE).ifPresent(this::setDamage);
        context.find(SkillcastingComponentTypes.PROJECTILE_PIERCE).ifPresent(this::setPierceLevel);
        context.find(SkillcastingComponentTypes.PROJECTILE_RICOCHET).ifPresent(this::setRicochetLevel);
        context.find(SkillcastingComponentTypes.EFFECT_DURATION_TICKS).ifPresent(this::setEffectDuration);
        context.find(SkillcastingComponentTypes.EFFECT_AMPLIFIER).ifPresent(this::setEffectAmplifier);
        context.find(SkillcastingComponentTypes.CURSOR_HOMING).ifPresent(u -> this.setCursorHoming(true));
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
