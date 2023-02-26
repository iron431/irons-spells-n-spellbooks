package com.example.testmod.capabilities.magic;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.network.ClientboundSyncPlayerData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SyncedSpellData {
    public static final long ANGEL_WINGS = 1;
    public static final long EVASION = 2;
    public static final long HEARTSTOP = 4;
    public static final long ABYSSAL_SHROUD = 8;
    public static final long ASCENSION = 16;
    public static final long TRUE_INVIS = 32;

    //TODO: may want to switch this to ServerPlayer.UUID
    private final int serverPlayerId;
    private @Nullable LivingEntity livingEntity;

    private boolean isCasting;
    private int castingSpellId;
    private int castingSpellLevel;
    private long effectFlags;
    private float heartStopAccumulatedDamage;
    private int evasionHitsRemaining;

    //Use this on the client
    public SyncedSpellData(int serverPlayerId) {
        this.livingEntity = null;
        this.serverPlayerId = serverPlayerId;
        this.isCasting = false;
        this.castingSpellId = 0;
        this.castingSpellLevel = 0;
        this.effectFlags = 0;
        this.heartStopAccumulatedDamage = 0f;
        this.evasionHitsRemaining = 0;
    }

    //Use this on the server
    public SyncedSpellData(LivingEntity livingEntity) {
        this(livingEntity == null ? -1 : livingEntity.getId());
        this.livingEntity = livingEntity;
    }

    public static final EntityDataSerializer<SyncedSpellData> SYNCED_SPELL_DATA = new EntityDataSerializer.ForValueType<SyncedSpellData>() {
        public void write(FriendlyByteBuf buffer, SyncedSpellData data) {
            buffer.writeInt(data.serverPlayerId);
            buffer.writeBoolean(data.isCasting);
            buffer.writeInt(data.castingSpellId);
            buffer.writeInt(data.castingSpellLevel);
            buffer.writeLong(data.effectFlags);
            buffer.writeFloat(data.heartStopAccumulatedDamage);
            buffer.writeInt(data.evasionHitsRemaining);
        }

        public SyncedSpellData read(FriendlyByteBuf buffer) {
            var data = new SyncedSpellData(buffer.readInt());
            data.isCasting = buffer.readBoolean();
            data.castingSpellId = buffer.readInt();
            data.castingSpellLevel = buffer.readInt();
            data.effectFlags = buffer.readLong();
            data.heartStopAccumulatedDamage = buffer.readFloat();
            data.evasionHitsRemaining = buffer.readInt();
            return data;
        }
    };

    public SyncedSpellData deepClone() {
        var syncedSpellData = new SyncedSpellData(this.livingEntity);
        syncedSpellData.isCasting = this.isCasting;
        syncedSpellData.castingSpellId = this.castingSpellId;
        syncedSpellData.castingSpellLevel = this.castingSpellLevel;
        syncedSpellData.effectFlags = this.effectFlags;
        syncedSpellData.heartStopAccumulatedDamage = this.heartStopAccumulatedDamage;
        syncedSpellData.evasionHitsRemaining = this.evasionHitsRemaining;
        return syncedSpellData;
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putBoolean("isCasting", this.isCasting);
        compound.putInt("castingSpellId", this.castingSpellId);
        compound.putInt("castingSpellLevel", this.castingSpellLevel);
        compound.putLong("effectFlags", this.effectFlags);
        compound.putFloat("heartStopAccumulatedDamage", this.heartStopAccumulatedDamage);
        compound.putFloat("evasionHitsRemaining", this.evasionHitsRemaining);
    }

    public void loadNBTData(CompoundTag compound) {
        this.isCasting = compound.getBoolean("isCasting");
        this.castingSpellId = compound.getInt("castingSpellId");
        this.castingSpellLevel = compound.getInt("castingSpellLevel");
        this.effectFlags = compound.getLong("effectFlags");
        this.heartStopAccumulatedDamage = compound.getFloat("heartStopAccumulatedDamage");
        this.evasionHitsRemaining = compound.getInt("evasionHitsRemaining");
    }

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public boolean hasEffect(long effectFlags) {
        return (this.effectFlags & effectFlags) == effectFlags;
    }

    public float getHeartstopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    public void setHeartstopAccumulatedDamage(float damage) {
        heartStopAccumulatedDamage = damage;
        doSync();
    }

    public int getEvasionHitsRemaining() {
        return evasionHitsRemaining;
    }

    public void subtractEvasionHitsRemaining() {
        evasionHitsRemaining--;
        doSync();
    }

    public void setEvasionHitsRemaining(int hitsRemaining) {
        evasionHitsRemaining = hitsRemaining;
        doSync();
    }

    public void addHeartstopDamage(float damage) {
        heartStopAccumulatedDamage += damage;
        doSync();
    }

    public void addEffects(long effectFlags) {
        this.effectFlags |= effectFlags;
        doSync();
    }

    public void removeEffects(long effectFlags) {
        this.effectFlags &= ~effectFlags;
        doSync();
    }

    public void doSync() {
        //this.player will only be null on the client side
        TestMod.LOGGER.debug("SyncedSpellData.doSync livingEntity:{} {}", livingEntity, this);

        if (livingEntity instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientboundSyncPlayerData(this), serverPlayer);
            Messages.sendToPlayersTrackingEntity(new ClientboundSyncPlayerData(this), serverPlayer);
        } else if (livingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            abstractSpellCastingMob.doSyncSpellData();
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientboundSyncPlayerData(this), serverPlayer);
    }

    public void setIsCasting(boolean isCasting, int castingSpellId, int castingSpellLevel) {
        this.isCasting = isCasting;
        this.castingSpellId = castingSpellId;
        this.castingSpellLevel = castingSpellLevel;
        doSync();
    }

    public boolean isCasting() {
        return isCasting;
    }

    public int getCastingSpellId() {
        return castingSpellId;
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    public SpellType getCastingSpellType() {
        return SpellType.values()[castingSpellId];
    }


    @Override
    protected SyncedSpellData clone() {
        return new SyncedSpellData(this.livingEntity);
    }

    @Override
    public String toString() {
        return String.format("isCasting:%s, spellID:%d, spellLevel:%d, effectFlags:%d",
                isCasting,
                castingSpellId,
                castingSpellLevel,
                effectFlags);
    }
}
