package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.AnimatedActionGoal;
import net.minecraft.world.phys.Vec3;

public class LeapBackGoal extends AnimatedActionGoal<IceSpiderEntity> {
    public LeapBackGoal(IceSpiderEntity mob) {
        super(mob);
    }

    @Override
    protected boolean canStartAction() {
        return mob.wantsToLeapBack;
    }

    @Override
    protected int getActionTimestamp() {
        return 0;
    }

    @Override
    protected int getActionDuration() {
        return 10;
    }

    @Override
    protected int getCooldown() {
        return 0;//mob.getRandom().nextIntBetweenInclusive(3, 8) * 20;
    }

    @Override
    protected String getAnimationId() {
        return "leap_back";
    }

    @Override
    protected void doAction() {
        //todo: sound effect
        Vec3 leapVector = new Vec3(0, .5, -1.5);
        mob.push(mob.rotateWithBody(leapVector));
        mob.wantsToLeapBack = false;
    }
}
