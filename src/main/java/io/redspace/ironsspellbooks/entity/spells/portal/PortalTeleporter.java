package io.redspace.ironsspellbooks.entity.spells.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PortalTeleporter implements ITeleporter {
    private final Vec3 destinationPosition;

    PortalTeleporter(BlockPos destinationPosition) {
        this.destinationPosition = new Vec3(destinationPosition.getX(), destinationPosition.getY(), destinationPosition.getZ());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        if (destinationPosition != null) {
            entity.moveTo(destinationPosition);
        }
        return entity;
    }

    @Override
    public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return ITeleporter.super.getPortalInfo(entity, destWorld, defaultPortalInfo);
    }

    @Override
    public boolean isVanilla() {
        return false;
    }

    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
        return false;
    }
}
