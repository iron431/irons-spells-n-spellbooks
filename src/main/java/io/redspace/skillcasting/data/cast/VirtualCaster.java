package io.redspace.skillcasting.data.cast;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A self container point with casting capability
 */
public final class VirtualCaster extends AttachmentHolder {
    private final Level level;
    private final UUID id;
    private final Vec3 position;
    private final Vec3 forward;
    // uuid used on server, integer id on client
    private final @Nullable UUID ownerId;
    private final int ownerEntityId;
    private boolean removed;

    public VirtualCaster(Level level, UUID id, Vec3 position, Vec3 forward, @Nullable UUID ownerId, int ownerEntityId) {
        this.level = level;
        this.id = id;
        this.position = position;
        this.forward = forward;
        this.ownerId = ownerId;
        this.ownerEntityId = ownerEntityId;
    }

    public Level level() {
        return level;
    }

    public UUID id() {
        return id;
    }

    public Vec3 position() {
        return position;
    }

    public Vec3 forward() {
        return forward;
    }

    public int ownerEntityId() {
        return ownerEntityId;
    }

    public @Nullable Entity owner() {
        if (level instanceof ServerLevel serverLevel) {
            return ownerId == null ? null : serverLevel.getEntity(ownerId);
        }
        // fixme: is id = 0 valid? less than equal to?
        return ownerEntityId < 0 ? null : level.getEntity(ownerEntityId);
    }

    public boolean isRemoved() {
        return removed;
    }

    public void markRemoved() {
        this.removed = true;
    }
}
