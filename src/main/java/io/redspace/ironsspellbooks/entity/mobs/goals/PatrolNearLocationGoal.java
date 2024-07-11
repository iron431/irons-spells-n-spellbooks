package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PatrolNearLocationGoal extends WaterAvoidingRandomStrollGoal {

    Vec3 origin;
    Supplier<Vec3> originHolder;
    float radiusSqr;

    public PatrolNearLocationGoal(PathfinderMob pMob, float radius, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        originHolder = pMob::position;
        radiusSqr = radius * radius;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        var f = super.getPosition();
        //IronsSpellbooks.LOGGER.debug("PatrolNearLocationGoal origin: {}", origin.resolve().get());
        if (origin == null) {
            origin = originHolder.get();
        }
        if (mob.position().horizontalDistanceSqr() > radiusSqr) {
            f = LandRandomPos.getPosTowards(mob, 8, 4, origin);
        }

        //IronsSpellbooks.LOGGER.debug("PatrolNearLocationGoal newPosition: {}", f);
        return f;

    }
}