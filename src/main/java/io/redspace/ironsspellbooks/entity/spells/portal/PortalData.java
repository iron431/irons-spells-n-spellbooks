package io.redspace.ironsspellbooks.entity.spells.portal;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.util.NBT;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.UUID;

public class PortalData implements ICastDataSerializable {
    public GlobalPos globalPos1;
    public UUID portalEntityId1;
    public GlobalPos globalPos2;
    public UUID portalEntityId2;
    public long expiresOnGameTick;

    public PortalData() {
    }

    public void setPortalDuration(int ticksToLive) {
        expiresOnGameTick = IronsSpellbooks.OVERWORLD.getGameTime() + ticksToLive;
    }

    public Optional<GlobalPos> getConnectedPortalPos(UUID portalId) {
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
        buffer.writeLong(expiresOnGameTick);

        if (globalPos1 != null && portalEntityId1 != null) {
            buffer.writeBoolean(true);
            buffer.writeGlobalPos(globalPos1);
            buffer.writeUUID(portalEntityId1);

            if (globalPos2 != null && portalEntityId2 != null) {
                buffer.writeBoolean(true);
                buffer.writeGlobalPos(globalPos2);
                buffer.writeUUID(portalEntityId2);
            } else {
                buffer.writeBoolean(false);
            }
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        expiresOnGameTick = buffer.readLong();
        if (buffer.readBoolean()) {
            globalPos1 = buffer.readGlobalPos();
            portalEntityId1 = buffer.readUUID();

            if (buffer.readBoolean()) {
                globalPos2 = buffer.readGlobalPos();
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
        tag.putLong("remainingTicks", expiresOnGameTick - IronsSpellbooks.OVERWORLD.getGameTime());

        if (globalPos1 != null) {
            tag.put("gp1", NBT.writeGlobalPos(globalPos1));
            tag.putUUID("pe1", portalEntityId1);

            if (globalPos2 != null) {
                tag.put("gp2", NBT.writeGlobalPos(globalPos2));
                tag.putUUID("pe2", portalEntityId2);
            }
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        var remainingTicks = compoundTag.getLong("remainingTicks");
        expiresOnGameTick = IronsSpellbooks.OVERWORLD.getGameTime() + remainingTicks;

        if (compoundTag.contains("gp1") && compoundTag.contains("pe1")) {
            this.globalPos1 = NBT.readGlobalPos(compoundTag.getCompound("gp1"));
            this.portalEntityId1 = compoundTag.getUUID("pe1");

            if (compoundTag.contains("gp2") && compoundTag.contains("pe2")) {
                this.globalPos2 = NBT.readGlobalPos(compoundTag.getCompound("gp2"));
                this.portalEntityId2 = compoundTag.getUUID("pe2");
            }
        }
    }
}
