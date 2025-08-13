package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.AnimatedActionGoal;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.fire_orb.FireOrbEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class OminousThrowFireOrbGoal extends AnimatedActionGoal<FireBossEntity> {
    public static final int ANIM_DURATION = 10;
    public static final int ACTION_TIMESTAMP = 1;

    public OminousThrowFireOrbGoal(FireBossEntity mob) {
        super(mob);
    }

    @Override
    protected boolean canStartAction() {
        return mob.isOminous() && mob.getTarget() != null;
    }

    @Override
    protected int getActionTimestamp() {
        return ACTION_TIMESTAMP;
    }

    @Override
    protected int getActionDuration() {
        return ANIM_DURATION;
    }

    @Override
    protected int getCooldown() {
        return 20 * 10;
    }

    @Override
    protected String getAnimationId() {
        return "instant_slash";
    }

    @Override
    protected void doAction() {
        var target = mob.getTarget();
        if (target != null) {
            mob.playSound(SoundRegistry.FIRE_BOSS_FIREBALL.get(), 2f, Utils.random.nextIntBetweenInclusive(80, 110) * .01f);
            Vec3 delta = this.mob.position().subtract(target.position()).normalize();
            Vec3 random = Utils.getRandomVec3(1).normalize().subtract(delta).normalize();
            float intensity = Mth.lerp(mob.getHealth() / mob.getMaxHealth(), 1, 0.5f);
            FireOrbEntity fireOrb = new FireOrbEntity(mob.level);
            fireOrb.setFuse(20 * 20);
            fireOrb.setDamage(160 * intensity);
            fireOrb.setHealth(150);
            fireOrb.setRadius(50 * intensity);
            fireOrb.setOwner(mob);
            fireOrb.setPos(mob.getEyePosition());
            fireOrb.setDeltaMovement(random.scale(0.3).add(0, 1, 0));
            mob.level.addFreshEntity(fireOrb);
        }
    }
}
