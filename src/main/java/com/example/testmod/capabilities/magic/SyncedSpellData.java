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

    //TODO: may want to switch this to ServerPlayer.UUID
    private final int serverPlayerId;
    private @Nullable LivingEntity livingEntity;

    private boolean isCasting;
    private int castingSpellId;
    private boolean hasAngelWings;
    private boolean hasEvasion;
    private boolean hasHeartstop;
    private float heartStopDamage;

    //Use this on the client
    public SyncedSpellData(int serverPlayerId) {
        this.livingEntity = null;
        this.serverPlayerId = serverPlayerId;
        this.isCasting = false;
        this.castingSpellId = 0;
        this.hasAngelWings = false;
        this.hasEvasion = false;
        this.hasHeartstop = false;
        this.heartStopDamage = 0;
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
            buffer.writeBoolean(data.hasAngelWings);
            buffer.writeBoolean(data.hasEvasion);
            buffer.writeBoolean(data.hasHeartstop);
            buffer.writeFloat(data.heartStopDamage);
        }

        public SyncedSpellData read(FriendlyByteBuf buffer) {
            var data = new SyncedSpellData(buffer.readInt());
            data.isCasting = buffer.readBoolean();
            data.castingSpellId = buffer.readInt();
            data.hasAngelWings = buffer.readBoolean();
            data.hasEvasion = buffer.readBoolean();
            data.hasHeartstop = buffer.readBoolean();
            data.heartStopDamage = buffer.readFloat();
            return data;
        }
    };

    public void saveNBTData(CompoundTag compound) {
        compound.putBoolean("isCasting", this.isCasting);
        compound.putInt("castingSpellId", this.castingSpellId);
        compound.putBoolean("hasAngelWings", this.hasAngelWings);
        compound.putBoolean("hasEvasion", this.hasEvasion);
        compound.putBoolean("hasHeartstop", this.hasHeartstop);
        compound.putFloat("heartStopDamage", this.heartStopDamage);
    }

    public void loadNBTData(CompoundTag compound) {
        this.isCasting = compound.getBoolean("isCasting");
        this.castingSpellId = compound.getInt("castingSpellId");
        this.hasAngelWings = compound.getBoolean("hasAngelWings");
        this.hasEvasion = compound.getBoolean("hasEvasion");
        this.hasHeartstop = compound.getBoolean("hasHeartstop");
        this.heartStopDamage = compound.getFloat("heartStopDamage");
    }

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public void doSync() {
        //this.player will only be null on the client side
        TestMod.LOGGER.debug("SyncedSpellData.doSync livingEntity:{}", livingEntity);

        if (livingEntity instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientboundSyncPlayerData(this), serverPlayer);
            Messages.sendToPlayersTrackingEntity(new ClientboundSyncPlayerData(this), serverPlayer);
        } else if (livingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            abstractSpellCastingMob.doSyncSpellData();
        }

        TestMod.LOGGER.debug("doSync {}", this);
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientboundSyncPlayerData(this), serverPlayer);
    }

    public boolean hasAngelWings() {
        return hasAngelWings;
    }

    public void setHasAngelWings(boolean hasAngelWings) {
        this.hasAngelWings = hasAngelWings;
        doSync();
    }

    public void setIsCasting(boolean isCasting, int castingSpellId) {
        this.isCasting = isCasting;
        this.castingSpellId = castingSpellId;
        doSync();
    }

    public boolean isCasting() {
        return isCasting;
    }

    public int getCastingSpellId() {
        return castingSpellId;
    }

    public SpellType getCastingSpellType() {
        return SpellType.values()[castingSpellId];
    }

    public boolean hasEvasion() {
        return hasEvasion;
    }

    public void setHasEvasion(boolean hasEvasion) {
        this.hasEvasion = hasEvasion;
        doSync();
    }

    public boolean hasHeartstop() {
        return hasHeartstop;
    }

    public void setHasHeartstop(boolean hasHeartstop) {
        this.hasHeartstop = hasHeartstop;
        doSync();
    }

    public float getHeartstopAccumulatedDamage() {
        return heartStopDamage;
    }

    public void setHeartstopAccumulatedDamage(float damage) {
        this.heartStopDamage = damage;
        doSync();
    }

    public void addHeartstopDamage(float amount) {
        this.heartStopDamage += amount;
        doSync();
    }

    @Override
    protected SyncedSpellData clone() {
        return new SyncedSpellData(this.livingEntity);
    }

    @Override
    public String toString() {
        return String.format("isCasting:%s, spellID:%d, hasAngelWings:%s, hasEvasion:%s, hasHeartstop:%s, heartStopDamage:%s",
                isCasting,
                castingSpellId,
                hasAngelWings,
                hasEvasion,
                hasHeartstop,
                heartStopDamage);
    }
}
