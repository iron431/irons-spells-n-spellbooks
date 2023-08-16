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

    private double originalX;
    private double originalY;
    private double originalZ;
    private double ticks;

    @Override
    public void setOnGround(boolean pOnGround) {
    }

    @Override
    public boolean isOnGround() {
        return this.position().y < originalY;
    }

    public VisualFallingBlockEntity(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
        this(EntityRegistry.FALLING_BLOCK.get(), pLevel);

        originalX = pX;
        originalY = pY;
        originalZ = pZ;
        ticks = 0;

        this.blocksBuilding = false;
        this.blockState = pState;
        this.setPos(pX, pY, pZ);
        setDeltaMovement(0, .25, 0);
        this.xo = pX;
        this.yo = pY;
        this.zo = pZ;
        this.setStartPos(this.blockPosition());

        this.dropItem = false;
        this.cancelDrop = true;
    }

    @Override
    public void tick() {
        super.tick();

        if(ticks++ >= 3){
            setDeltaMovement(0, -.25, 0);
        }
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
