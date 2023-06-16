package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public class PatrolNearLocationGoal extends WaterAvoidingRandomStrollGoal {

    LazyOptional<Vec3> origin;
    float radiusSqr;

    public PatrolNearLocationGoal(PathfinderMob pMob, float radius, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        origin = LazyOptional.of(pMob::position);
        radiusSqr = radius * radius;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        var f = super.getPosition();
        //IronsSpellbooks.LOGGER.debug("PatrolNearLocationGoal origin: {}", origin.resolve().get());
        if (mob.position().horizontalDistanceSqr() > radiusSqr)
            f = LandRandomPos.getPosTowards(mob, 8, 4, origin.resolve().get());

        //IronsSpellbooks.LOGGER.debug("PatrolNearLocationGoal newPosition: {}", f);
        return f;

    }
}