package io.redspace.ironsspellbooks.entity.spells.portal;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.util.NBT;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class PortalData implements ICastDataSerializable {
    public PortalPos globalPos1;
    public UUID portalEntityId1;
    public PortalPos globalPos2;
    public UUID portalEntityId2;
    public int ticksToLive;

    public PortalData() {
    }

    public void setPortalDuration(int ticksToLive) {
        this.ticksToLive = ticksToLive;
    }

    public Optional<PortalPos> getConnectedPortalPos(UUID portalId) {
        if (portalEntityId1.equals(portalId)) {
            return Optional.of(globalPos2);
        } else if (portalEntityId2.equals(portalId)) {
            return Optional.of(globalPos1);
        }

        return Optional.empty();
    }

    public UUID getConnectedPortalUUID(UUID portalId) {
        if (portalEntityId1.equals(portalId)) {
            return portalEntityId2;
        } else if (portalEntityId2.equals(portalId)) {
            return portalEntityId1;
        }

        return null;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeLong(ticksToLive);

        if (globalPos1 != null && portalEntityId1 != null) {
            buffer.writeBoolean(true);
            writePortalPosToBuffer(buffer, globalPos1);
            buffer.writeUUID(portalEntityId1);

            if (globalPos2 != null && portalEntityId2 != null) {
                buffer.writeBoolean(true);
                writePortalPosToBuffer(buffer, globalPos2);
                buffer.writeUUID(portalEntityId2);
            } else {
                buffer.writeBoolean(false);
            }
        } else {
            buffer.writeBoolean(false);
        }
    }

    //TODO: make buffer utils class?
    private void writePortalPosToBuffer(FriendlyByteBuf buffer, PortalPos pos) {
        buffer.writeResourceKey(pos.dimension());
        Vec3 vec3 = pos.pos();
        buffer.writeInt((int) (vec3.x * 10));
        buffer.writeInt((int) (vec3.y * 10));
        buffer.writeInt((int) (vec3.z * 10));
        buffer.writeFloat(pos.rotation());
    }

    private PortalPos readPortalPosFromBuffer(FriendlyByteBuf buffer) {
        return PortalPos.of(buffer.readResourceKey(Registries.DIMENSION), new Vec3(buffer.readInt() / 10.0, buffer.readInt() / 10.0, buffer.readInt() / 10.0), buffer.readFloat());
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        ticksToLive = buffer.readInt();
        if (buffer.readBoolean()) {
            globalPos1 = readPortalPosFromBuffer(buffer);
            portalEntityId1 = buffer.readUUID();

            if (buffer.readBoolean()) {
                globalPos2 = readPortalPosFromBuffer(buffer);
                portalEntityId2 = buffer.readUUID();
            }
        }
    }

    @Override
    public void reset() {
        //nothing to clean up for Portal
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ticksToLive", ticksToLive);

        if (globalPos1 != null) {
            tag.put("gp1", NBT.writePortalPos(globalPos1));
            tag.putUUID("pe1", portalEntityId1);

            if (globalPos2 != null) {
                tag.put("gp2", NBT.writePortalPos(globalPos2));
                tag.putUUID("pe2", portalEntityId2);
            }
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        ticksToLive = compoundTag.getInt("ticksToLive");

        if (compoundTag.contains("gp1") && compoundTag.contains("pe1")) {
            this.globalPos1 = NBT.readPortalPos(compoundTag.getCompound("gp1"));
            this.portalEntityId1 = compoundTag.getUUID("pe1");

            if (compoundTag.contains("gp2") && compoundTag.contains("pe2")) {
                this.globalPos2 = NBT.readPortalPos(compoundTag.getCompound("gp2"));
                this.portalEntityId2 = compoundTag.getUUID("pe2");
            }
        }
    }
}
