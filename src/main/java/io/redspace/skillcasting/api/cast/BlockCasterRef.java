package io.redspace.skillcasting.api.cast;

import io.redspace.skillcasting.api.PositionAnchor;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

public record BlockCasterRef(BlockEntity blockEntity) implements CasterRef {

    @Override
    public CasterId id() {
        return new CasterId.Block(Objects.requireNonNull(blockEntity.getLevel(), "Cannot ID a block entity outside of a level").dimension(), blockEntity.getBlockPos());
    }

    @Override
    public boolean isValid() {
        return !blockEntity.isRemoved();
    }

    @Override
    public Level level() {
        return blockEntity.getLevel();
    }

    @Override
    public Vec3 position(PositionAnchor anchor) {
        return switch (anchor) {
            case CENTER -> Vec3.atCenterOf(blockEntity.getBlockPos());
            case CASTING_POSITION -> Vec3.atCenterOf(blockEntity.getBlockPos()).add(forward().scale(0.51));
            default -> Vec3.atLowerCornerOf(blockEntity.getBlockPos());
        };
    }

    @Override
    public Vec3 forward() {
        return Vec3.atLowerCornerOf(blockEntity.getBlockState().getOptionalValue(BlockStateProperties.FACING).orElse(Direction.NORTH).getNormal());
    }

    @Override
    public void distributeToClients(CustomPacketPayload payload) {
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(blockEntity.getBlockPos()), payload);
        }
    }

    @Override
    public IAttachmentHolder get() {
        return blockEntity;
    }
}
