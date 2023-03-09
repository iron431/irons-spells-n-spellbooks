package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class PatrolNearLocationGoal extends Goal {

    private final PathfinderMob mob;
    private final int maxRadius;
    private final float speedModifier;
    private Vec3 patrolLocationCenter = null;
    private final Random random = new Random();
    private BlockPos targeBlock;
    private long cooldownUntil;

    public PatrolNearLocationGoal(PathfinderMob mob, int maxRadius, float speedModifier) {
        this.mob = mob;
        this.maxRadius = maxRadius;
        this.speedModifier = speedModifier;
        this.patrolLocationCenter = null;
        this.cooldownUntil = 0;

        //irons_spellbooks.LOGGER.debug("PNLG: {}", mob.position());
    }

    @Override
    public boolean canUse() {
        boolean isOnCooldown = this.mob.level.getGameTime() < this.cooldownUntil;
        //irons_spellbooks.LOGGER.debug("PNLG.canUse: {}, {}, {}", this.mob.level.getGameTime(), cooldownUntil, (this.mob.getTarget() == null && !isOnCooldown));
        return this.mob.getTarget() == null && !isOnCooldown;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        //irons_spellbooks.LOGGER.debug("PNLG.stop");
    }

    public void tick() {
        PathNavigation pathnavigation = this.mob.getNavigation();
        if (patrolLocationCenter == null || pathnavigation.isDone()) {
            getNextTargetBlock();
            if (!pathnavigation.moveTo(targeBlock.getX(), targeBlock.getY(), targeBlock.getZ(), speedModifier)) {
                getNextTargetBlock();
                this.cooldownUntil = this.mob.level.getGameTime() + 200L;
            }
        }
    }

    private void getNextTargetBlock() {
        if (patrolLocationCenter == null) {
            patrolLocationCenter = mob.getEyePosition();
        }

        Vec3 pos = patrolLocationCenter.add(getRandomPosInRadius());
        this.targeBlock = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(pos));
        //irons_spellbooks.LOGGER.debug("PNLG.getNextTargetBlock: center:{} target:{}", patrolLocationCenter, targeBlock);
    }

    private Vec3 getRandomPosInRadius() {
        return new Vec3(
                random.nextInt(maxRadius * 2) - maxRadius,
                10,
                random.nextInt(maxRadius * 2) - maxRadius
        );
    }

    /**
     * Return true to set given position as destination
     */
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        //irons_spellbooks.LOGGER.debug("PNLG.isValidTarget");
        return true;
    }
}