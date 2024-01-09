package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RoamVillageGoal extends PatrolNearLocationGoal {

    //    boolean wantsToBeInVillage;
    GlobalPos villagePoi;
    int searchCooldown;

    public RoamVillageGoal(PathfinderMob pMob, float radius, double pSpeedModifier) {
        super(pMob, radius, pSpeedModifier);
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        if (villagePoi != null) {
            //IronsSpellbooks.LOGGER.debug("RoamVillageGoal: finding position ({})", villagePoi.pos());

            return Vec3.atBottomCenterOf(villagePoi.pos());
        }
        //IronsSpellbooks.LOGGER.debug("RoamVillageGoal: village poi null. going to: {}", super.getPosition());

        return super.getPosition();
    }

    @Override
    public boolean canUse() {
        if (villagePoi == null && searchCooldown-- <= 0) {
            //IronsSpellbooks.LOGGER.debug("RoamVillageGoal.trying to find village (expensive?)");
            findVillagePoi();
            searchCooldown = 200;
        }
        //TODO: distance check too?

        var canUse = (this.mob.level.isDay() || isDuringRaid()) && villagePoi != null && super.canUse();
        //IronsSpellbooks.LOGGER.debug("RoamVillageGoal.canuse: {}", canUse);

        return canUse;
    }

    private boolean isDuringRaid() {
        //TODO: find out if a current raid is going on
        return false;
    }

    protected void findVillagePoi() {
        if (mob.level instanceof ServerLevel serverLevel) {
//            MinecraftServer minecraftserver = serverLevel.getServer();
//            ServerLevel serverlevel = minecraftserver.getLevel(serverLevel.dimension());
            Optional<BlockPos> optional1 = serverLevel.getPoiManager().find((poiTypeHolder) -> poiTypeHolder.is(PoiTypes.MEETING),
                    (x) -> true, mob.blockPosition(), 100, PoiManager.Occupancy.ANY);
            optional1.ifPresent((blockPos -> this.villagePoi = GlobalPos.of(serverLevel.dimension(), blockPos)));

        }
    }
}