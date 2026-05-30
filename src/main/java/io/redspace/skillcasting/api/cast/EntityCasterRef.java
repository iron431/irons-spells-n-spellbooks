package io.redspace.skillcasting.api.cast;

import io.redspace.skillcasting.api.PositionAnchor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;

public record EntityCasterRef(Entity entity) implements CasterRef {

    @Override
    public CasterId id() {
        return new CasterId.Entity(entity.getId());
    }

    @Override
    public boolean isValid() {
        return !entity.isRemoved() && entity.isAlive();
    }

    @Override
    public Level level() {
        return entity.level();
    }

    @Override
    public Vec3 position(PositionAnchor anchor) {
        return switch (anchor) {
            case CENTER -> entity.getBoundingBox().getCenter();
            case CASTING_POSITION -> entity.getEyePosition();
            default -> entity.position();
        };
    }

    @Override
    public Vec3 forward() {
        return entity.getLookAngle();
    }

    @Override
    public void distributeToClients(CustomPacketPayload payload) {
        if (entity.level() instanceof ServerLevel) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
        }
    }

    @Override
    public IAttachmentHolder get() {
        return entity;
    }
}
