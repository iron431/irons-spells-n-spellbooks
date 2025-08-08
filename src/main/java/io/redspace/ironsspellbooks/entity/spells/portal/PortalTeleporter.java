package io.redspace.ironsspellbooks.entity.spells.portal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class PortalTeleporter implements ITeleporter {
    private final Vec3 destinationPosition;
    private final Optional<Float> rotation;

    public PortalTeleporter(Vec3 destinationPosition) {
        this.destinationPosition = destinationPosition;
        this.rotation = Optional.empty();
    }

    public PortalTeleporter(Vec3 destinationPosition, float rotation) {
        this.destinationPosition = destinationPosition;
        this.rotation = Optional.of(rotation);
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        entity.fallDistance = 0;
        return repositionEntity.apply(false);
    }

    @Override
    public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(destinationPosition, Vec3.ZERO, rotation.orElse(entity.getYRot()), entity.getXRot());
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
