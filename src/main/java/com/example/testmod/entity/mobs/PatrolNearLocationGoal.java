package com.example.testmod.entity.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class PatrolNearLocationGoal extends MoveToBlockGoal {

    private final SimpleWizard simpleWizard;
    private final int maxRadius;
    private final Vec3 patrolLocationCenter;
    private final Random random = new Random();

    public PatrolNearLocationGoal(SimpleWizard simpleWizard, Vec3 patrolLocationCenter, int maxRadius) {
        super(simpleWizard, .8, 16);
        this.simpleWizard = simpleWizard;
        this.maxRadius = maxRadius;
        this.patrolLocationCenter = patrolLocationCenter;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        super.stop();
        BlockEntity be = mob.level.getBlockEntity(blockPos);
        if (be instanceof ChestBlockEntity) {
            mob.level.blockEvent(blockPos, be.getBlockState().getBlock(), 1, 0);
        }
    }

    public void tick() {
        super.tick();
        if (isReachedTarget()) {
            //BlockEntity be = mob.level.getBlockEntity(blockPos);
        }
    }


    /**
     * Return true to set given position as destination
     */
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        if (!pLevel.isEmptyBlock(pPos.above())) {
            return false;
        } else {
            BlockState blockstate = pLevel.getBlockState(pPos);
            return blockstate.is(Blocks.CHEST);
        }
    }
}