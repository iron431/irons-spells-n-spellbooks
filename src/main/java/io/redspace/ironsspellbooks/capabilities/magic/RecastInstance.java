package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.network.ISerializable;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.network.FriendlyByteBuf;

public class RecastInstance implements ISerializable {
    public String spellId;
    public int spellLevel;
    public int remainingRecasts;
    public ICastDataSerializable castData;

    public RecastInstance() {

    }

    public RecastInstance(String spellId, int spellLevel, int remainingRecasts, ICastDataSerializable castData) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.remainingRecasts = remainingRecasts;
        this.castData = castData;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(spellId);
        buffer.writeInt(spellLevel);
        buffer.writeInt(remainingRecasts);
        castData.writeToBuffer(buffer);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        spellId = buffer.readUtf();
        spellLevel = buffer.readInt();
        remainingRecasts = buffer.readInt();
        var tmpCastData = SpellRegistry.getSpell(spellId).getEmptyCastData();
        tmpCastData.readFromBuffer(buffer);
        castData = tmpCastData;
    }
}
