package io.redspace.skillcasting.data.cast;

import net.minecraft.core.SectionPos;
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
        // only the chunk matters: Level#isLoaded also rejects points outside the build height
        // fixme: as per manager, is strictly limiting casters to chunk limit necessary?
        Vec3 position = caster.position();
        return !caster.isRemoved() && caster.level().hasChunk(SectionPos.blockToSectionCoord(position.x), SectionPos.blockToSectionCoord(position.z));
    }

    @Override
    public Level level() {
        return caster.level();
    }

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
