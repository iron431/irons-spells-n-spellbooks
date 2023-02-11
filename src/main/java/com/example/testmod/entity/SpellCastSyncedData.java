package com.example.testmod.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

public class SpellCastSyncedData {
    public int spellId;
    public int spellLevel;
    public boolean usePosition;
    public boolean hasEvasion;
    public int x;
    public int y;
    public int z;

    public static final EntityDataSerializer<SpellCastSyncedData> SPELL_SYNCED_DATA = new EntityDataSerializer.ForValueType<SpellCastSyncedData>() {
        public void write(FriendlyByteBuf buffer, SpellCastSyncedData data) {
            buffer.writeInt(data.spellId);
            buffer.writeInt(data.spellLevel);
            buffer.writeBoolean(data.hasEvasion);
            buffer.writeBoolean(data.usePosition);
            buffer.writeInt(data.x);
            buffer.writeInt(data.y);
            buffer.writeInt(data.z);
        }

        public SpellCastSyncedData read(FriendlyByteBuf buffer) {
            var data = new SpellCastSyncedData();
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

