package io.redspace.ironsspellbooks.entity;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SuspendedBlockEntity extends Entity {
    public BlockState blockState = Blocks.SAND.defaultBlockState();
    @Nullable
    public CompoundTag blockData;

    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(SuspendedBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    public void setStartPos(BlockPos pos) {
        this.entityData.set(DATA_START_POS, pos);
    }

    public SuspendedBlockEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        Vec3 deltaMovement = getDeltaMovement();
        this.move(MoverType.SELF, deltaMovement);
        Vec3 spawn = getStartPos().getCenter();
        Vec3 towardsSpawn = spawn.subtract(position()).normalize();
        if (tickCount < 20 * 10) {
            Vec3 wantedMotion = towardsSpawn.scale(3);
            float f = Mth.clamp((tickCount) / 200f, .1f, 1);
            this.setDeltaMovement(deltaMovement.add(wantedMotion.subtract(deltaMovement).scale(f * 0.15f)));
        } else {
            this.setDeltaMovement(towardsSpawn.scale(0.5f));
            this.noPhysics = true;
        }
        if (tickCount > 1.5 * 20) {
            this.noPhysics = true;
        }

        if (tickCount > 20) {
            //check for arriving back to spawn
            double distanceToSpawnSqr = this.position().distanceToSqr(spawn);
            double speedSqr = this.getDeltaMovement().lengthSqr();
            if (speedSqr > distanceToSpawnSqr) {
                // clip speed to land at spawn
                this.setDeltaMovement(spawn.subtract(this.position()));
            }
            double snapThreshold = 0.1;
//            if (deltaMovement.dot(towardsSpawn) < 0) {
//                // attempt to recover overshoot
//                snapThreshold = 1.5;
//                IronsSpellbooks.LOGGER.debug("overshootin");
//            }
            if (distanceToSpawnSqr < snapThreshold * snapThreshold) {
                placeSelfInWorld(getStartPos());
            }
//            double nextDistanceToSpawnSqr = this.position().add(deltaMovement).distanceToSqr(spawn);
//            double threshold;
//            if (tickCount < 10 * 20) {
//                threshold = 0.5;
//                if (deltaMovement.dot(towardsSpawn) < 0) {
//                    threshold = 1.5;
//                }
//            } else {
//                threshold = 5;
//            }
//            if (distanceToSpawnSqr < threshold * threshold) {
//                placeSelfInWorld(getStartPos());
//            } else if (nextDistanceToSpawnSqr < deltaMovement.lengthSqr()) {
//                setDeltaMovement(towardsSpawn.scale(deltaMovement.length()));
//            }
            return;
        }

    }

    public void placeSelfInWorld(BlockPos blockpos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.level().getFluidState(blockpos).getType() == Fluids.WATER) {
            this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        BlockState updatedState = Block.updateFromNeighbourShapes(this.blockState, level, blockpos);
        if (!updatedState.isAir()) {
            this.blockState = updatedState;
        }
        if (this.level().setBlock(blockpos, this.blockState, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS | Block.UPDATE_IMMEDIATE)) {
            serverLevel
                    .getChunkSource()
                    .chunkMap
                    .broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.level().getBlockState(blockpos)));

            if (this.blockData != null && this.blockState.hasBlockEntity()) {
                BlockEntity blockentity = this.level().getBlockEntity(blockpos);
                if (blockentity != null) {
                    CompoundTag compoundtag = blockentity.saveWithoutMetadata(this.level().registryAccess());

                    for (String s : this.blockData.getAllKeys()) {
                        compoundtag.put(s, this.blockData.get(s).copy());
                    }

                    try {
                        blockentity.loadWithComponents(compoundtag, this.level().registryAccess());
                    } catch (Exception exception) {
                        IronsSpellbooks.LOGGER.error("Failed to load block entity from falling block", (Throwable) exception);
                    }

                    blockentity.setChanged();
                }
            }
        } else if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(this.blockState.getBlock());
        }
        discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        this.blockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("BlockState"));
        this.tickCount = compound.getInt("Age");
        NbtUtils.readBlockPos(compound, "StartPos").ifPresentOrElse(this::setStartPos, () -> this.setStartPos(BlockPos.ZERO));
        if (compound.contains("TileEntityData", 10)) {
            this.blockData = compound.getCompound("TileEntityData").copy();
        } else {
            this.blockData = null;
        }
        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        compound.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        compound.putInt("Age", this.tickCount);
        compound.put("StartPos", NbtUtils.writeBlockPos(this.getStartPos()));
        if (this.blockData != null) {
            compound.put("TileEntityData", this.blockData);
        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.blockState = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        this.noPhysics = true;
        double d0 = packet.getX();
        double d1 = packet.getY();
        double d2 = packet.getZ();
        this.setPos(d0, d1, d2);
        this.setStartPos(this.blockPosition());
    }
}
