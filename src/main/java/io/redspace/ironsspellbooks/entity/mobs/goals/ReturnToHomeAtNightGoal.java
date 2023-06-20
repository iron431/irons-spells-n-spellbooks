package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ReturnToHomeAtNightGoal<T extends PathfinderMob & HomeOwner> extends WaterAvoidingRandomStrollGoal {

    T homeOwnerMob;

    public ReturnToHomeAtNightGoal(T pMob, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        this.homeOwnerMob = pMob;
    }

    @Override
    public boolean canUse() {
        return homeOwnerMob.getHome() != null && !mob.level.isDay() && super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        return homeOwnerMob.getHome() == null ? super.getPosition() : Vec3.atBottomCenterOf(homeOwnerMob.getHome());
    }
}