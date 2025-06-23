package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.AnimatedActionGoal;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
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
        mob.playSound(SoundRegistry.ICE_SPIDER_SWING.get(), 3, Utils.random.nextIntBetweenInclusive(13, 16) * .1f);
        Vec3 leapVector = new Vec3(0, .5, -2.2);
        mob.push(mob.rotateWithBody(leapVector));
        mob.wantsToLeapBack = false;
    }
}
