package io.redspace.ironsspellbooks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GhostFallingBlockEntity extends FallingBlockEntity {
    public GhostFallingBlockEntity(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
        super(pLevel, pX, pY, pZ, pState);
        this.dropItem = false;
        this.cancelDrop = true;
        this.setNoGravity(true);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void callOnBrokenAfterFall(Block pBlock, BlockPos pPos) {
        return;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

}
