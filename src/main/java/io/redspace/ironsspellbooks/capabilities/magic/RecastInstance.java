package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.network.ISerializable;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;

public class RecastInstance implements ISerializable, INBTSerializable<CompoundTag> {
    protected String spellId;
    protected int spellLevel;
    protected int remainingRecasts;
    protected int totalRecasts;
    protected ICastDataSerializable castData;
    protected int ticksToLive;
    protected long expireOnTick = 0;

    //Only used when syncing to the client. Don't ever use this.
    protected long ticksRemaining = 0;

    public RecastInstance() {
    }

    public RecastInstance(String spellId, int spellLevel, int remainingRecasts, int ticksToLive, ICastDataSerializable castData) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.remainingRecasts = remainingRecasts;
        this.totalRecasts = remainingRecasts + 1;
        this.ticksToLive = ticksToLive;
        this.castData = castData;
    }

    public String getSpellId() {
        return spellId;
    }

    public int getSpellLevel() {
        return spellLevel;
    }

    public int getRemainingRecasts() {
        return remainingRecasts;
    }

    public int getTotalRecasts() {
        return totalRecasts;
    }

    public int getTicksToLive() {
        return ticksToLive;
    }

    public void setExpireOnTick(long expireOnTick) {
        this.expireOnTick = expireOnTick;
    }

    public ICastDataSerializable getCastData() {
        return castData;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(spellId);
        buffer.writeInt(spellLevel);
        buffer.writeInt(remainingRecasts);
        buffer.writeInt(totalRecasts);
        buffer.writeInt(ticksToLive);
        buffer.writeLong(ticksRemaining);

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
        totalRecasts = buffer.readInt();
        ticksToLive = buffer.readInt();
        ticksRemaining = buffer.readLong();

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
        tag.putInt("total", totalRecasts);
        tag.putInt("ticks", ticksToLive);
        tag.putLong("remaining", ticksRemaining);

        if (castData != null) {
            tag.put("cd", castData.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        spellId = compoundTag.getString("id");
        spellLevel = compoundTag.getInt("lvl");
        remainingRecasts = compoundTag.getInt("cnt");
        totalRecasts = compoundTag.getInt("total");
        ticksToLive = compoundTag.getInt("ticks");
        ticksRemaining = compoundTag.getLong("remaining");

        if (compoundTag.contains("cd")) {
            castData = SpellRegistry.getSpell(spellId).getEmptyCastData();
            if (castData != null) {
                castData.deserializeNBT((CompoundTag) compoundTag.get("cd"));
            }
        }
    }

    @Override
    public String toString() {
        var cd = castData == null ? "" : castData.serializeNBT().toString();
        return String.format("spellId:%s, spellLevel:%d, remaining:%d, total:%d, ttl:%d, expireOn:%d, remain:%d, castData:%s", spellId, spellLevel, remainingRecasts, totalRecasts, ticksToLive, expireOnTick, ticksRemaining, cd);
    }
}
