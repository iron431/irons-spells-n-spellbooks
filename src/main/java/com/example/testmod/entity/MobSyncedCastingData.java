package com.example.testmod.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

public class MobSyncedCastingData {
    public int spellId;
    public int spellLevel;
    public boolean usePosition;
    public boolean hasEvasion;
    public int x;
    public int y;
    public int z;

    public static final EntityDataSerializer<MobSyncedCastingData> MOB_SYNCED_CASTING_DATA = new EntityDataSerializer.ForValueType<MobSyncedCastingData>() {
        public void write(FriendlyByteBuf buffer, MobSyncedCastingData data) {
            buffer.writeInt(data.spellId);
            buffer.writeInt(data.spellLevel);
            buffer.writeBoolean(data.hasEvasion);
            buffer.writeBoolean(data.usePosition);
            buffer.writeInt(data.x);
            buffer.writeInt(data.y);
            buffer.writeInt(data.z);
        }

        public MobSyncedCastingData read(FriendlyByteBuf buffer) {
            var data = new MobSyncedCastingData();
            data.spellId = buffer.readInt();
            data.spellLevel = buffer.readInt();
            data.hasEvasion = buffer.readBoolean();
            data.usePosition = buffer.readBoolean();
            data.x = buffer.readInt();
            data.y = buffer.readInt();
            data.z = buffer.readInt();
            return data;
        }
    };

    @Override
    public String toString() {
        return String.format("spellId: %s, spellLevel:%s, usePosition:%s, x:%s, y:%s, z:%s", spellId, spellLevel, usePosition, x, y, z);
    }
}

