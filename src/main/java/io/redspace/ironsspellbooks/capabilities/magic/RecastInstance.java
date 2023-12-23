package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.network.ISerializable;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;

public class RecastInstance implements ISerializable, INBTSerializable<CompoundTag> {
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

        if (castData != null) {
            buffer.writeBoolean(true);
            castData.writeToBuffer(buffer);
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        spellId = buffer.readUtf();
        spellLevel = buffer.readInt();
        remainingRecasts = buffer.readInt();

        var hasCastData = buffer.readBoolean();
        if (hasCastData) {
            var tmpCastData = SpellRegistry.getSpell(spellId).getEmptyCastData();
            tmpCastData.readFromBuffer(buffer);
            castData = tmpCastData;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("id", spellId);
        tag.putInt("lvl", spellLevel);
        tag.putInt("cnt", remainingRecasts);

        if (castData != null) {
            tag.put("cd", castData.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        spellId = compoundTag.getString("id");
        spellLevel = compoundTag.getInt("lvl");
        remainingRecasts = compoundTag.getInt("cd");

        if (compoundTag.contains("cd")) {
            castData = SpellRegistry.getSpell(spellId).getEmptyCastData();
            castData.deserializeNBT((CompoundTag) compoundTag.get("cd"));
        }
    }

    @Override
    public String toString() {
        return String.format("spellId: %s, spellLevel: %d, remaining: %d, castData: %s", spellId, spellLevel, remainingRecasts, castData.serializeNBT().toString());
    }
}
