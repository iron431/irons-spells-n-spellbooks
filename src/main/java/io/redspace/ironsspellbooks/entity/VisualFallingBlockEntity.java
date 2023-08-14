package io.redspace.ironsspellbooks.entity;

import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class VisualFallingBlockEntity extends FallingBlockEntity {
    public VisualFallingBlockEntity(EntityType<? extends VisualFallingBlockEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public VisualFallingBlockEntity(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
        this(EntityRegistry.FALLING_BLOCK.get(), pLevel);
        this.blocksBuilding = false;
        this.blockState = pState;
        this.setPos(pX, pY, pZ);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = pX;
        this.yo = pY;
        this.zo = pZ;
        this.setStartPos(this.blockPosition());

        this.dropItem = false;
        this.cancelDrop = true;
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
