package io.redspace.skillcasting.data.cast;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public record VirtualCasterRef(VirtualCaster caster) implements CasterRef {

    @Override
    public CasterId.Virtual id() {
        return new CasterId.Virtual(caster.level().dimension(), caster.id());
    }

    @Override
    public @Nullable Entity owner() {
        return caster.owner();
    }

    @Override
    public boolean isValid() {
        return !caster.isRemoved() && caster.level().isLoaded(BlockPos.containing(caster.position()));
    }

    @Override
    public Level level() {
        return caster.level();
    }

    // a point has no body, so every anchor is the point itself
    @Override
    public Vec3 position(PositionAnchor anchor) {
        return caster.position();
    }

    @Override
    public Vec3 forward() {
        return caster.forward();
    }

    @Override
    public void distributeToClients(CustomPacketPayload payload) {
        if (caster.level() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(BlockPos.containing(caster.position())), payload);
        }
    }

    @Override
    public IAttachmentHolder get() {
        return caster;
    }
}
