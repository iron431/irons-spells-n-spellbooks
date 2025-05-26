package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class SerializedTargetData implements ICastDataSerializable {
    protected UUID targetUUID;

    public SerializedTargetData(Entity target) {
        this.targetUUID = target.getUUID();
    }

    public SerializedTargetData() {
        this.targetUUID = null;
    }

    @Override
    public void reset() {

    }

    @Nullable
    public Entity getTarget(ServerLevel level) {
        return level.getEntity(targetUUID);
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    @Nullable
    public Vec3 getTargetPosition(ServerLevel level) {
        var target = getTarget(level);
        return target == null ? null : target.position();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.targetUUID);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.targetUUID = buffer.readUUID();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("target", this.targetUUID);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.targetUUID = nbt.getUUID("target");
    }
}