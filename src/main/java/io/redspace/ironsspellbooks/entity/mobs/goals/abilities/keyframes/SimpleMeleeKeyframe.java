package io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframes;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.EventKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.IAbilityHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SimpleMeleeKeyframe<T extends Mob & IAbilityHandler<T>> extends EventKeyframe<T> {
    Vec3 lunge = Vec3.ZERO;
    Vec3 knockback = Vec3.ZERO;
    int iframes = -1;
    boolean shieldBreak = false;
    @Nullable SoundEventKeyframe<T> impactSound = null;

    public SimpleMeleeKeyframe(int timestamp) {
        super(timestamp);
    }

    public SimpleMeleeKeyframe<T> lunge(Vec3 lunge) {
        this.lunge = lunge;
        return this;
    }

    public SimpleMeleeKeyframe<T> knockback(Vec3 knockback) {
        this.knockback = knockback;
        return this;
    }

    public SimpleMeleeKeyframe<T> iframes(int iframes) {
        this.iframes = iframes;
        return this;
    }

    public SimpleMeleeKeyframe<T> shieldBreak(boolean shieldBreak) {
        this.shieldBreak = shieldBreak;
        return this;
    }

    public SimpleMeleeKeyframe<T> impactSound(@Nullable SoundEventKeyframe<T> impactSound) {
        this.impactSound = impactSound;
        return this;
    }

    float meleeRange(T mob) {
        var attributes = mob.getAttributes();
        if (attributes.hasAttribute(Attributes.ENTITY_INTERACTION_RANGE) && attributes.hasAttribute(Attributes.SCALE)) {
            return (float) (mob.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) * mob.getAttributeValue(Attributes.SCALE));
        }
        return 3f;
    }

    @Override
    public void onEvent(T mob) {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }

        float f = -Utils.getAngle(mob.getX(), mob.getZ(), target.getX(), target.getZ()) - Mth.HALF_PI;
        if (this.lunge != Vec3.ZERO) {
            Vec3 lungeVector = this.lunge.yRot(f);
            mob.push(lungeVector.x, lungeVector.y, lungeVector.z);
        }
        float meleeRange = meleeRange(mob);
        if (mob.distanceToSqr(target) <= meleeRange * meleeRange && Utils.hasLineOfSight(mob.level(), mob, target, true)) {
            boolean hit = mob.doHurtTarget(target);
            if (iframes >= 0) {
                target.invulnerableTime = iframes;
            }
            boolean blocking = target.isBlocking();
            if (shieldBreak && blocking && target instanceof Player player) {
                player.disableShield();
            }
            if (hit) {
                if (impactSound != null) {
                    impactSound.onEvent(mob);
                }
                if (knockback != Vec3.ZERO) {
                    target.setDeltaMovement(target.getDeltaMovement().add(knockback.yRot(f)));
                }
            }
            postHit(mob, hit, blocking);
        }
        postEvent();
    }

    public void postEvent() {
    }

    public void postHit(T mob, boolean hit, boolean blocking) {

    }
}
