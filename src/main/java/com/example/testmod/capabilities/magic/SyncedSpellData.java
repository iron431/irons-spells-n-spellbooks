package com.example.testmod.capabilities.magic;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.network.ClientBoundSyncPlayerData;
import com.example.testmod.setup.Messages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class SyncedSpellData {

    //TODO: may want to switch this to ServerPlayer.UUID
    private final int serverPlayerId;
    private LivingEntity livingEntity;

    /**
     * REMINDER: Need to update ClientBoundSyncPlayerData when adding fields to this class
     **/
    private boolean hasAngelWings;
    private boolean hasEvasion;
    private boolean hasHeartstop;
    private float heartStopDamage;

    //Use this on the client
    public SyncedSpellData(int serverPlayerId) {
        this.livingEntity = null;
        this.serverPlayerId = serverPlayerId;
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
            buffer.writeBoolean(data.hasAngelWings);
            buffer.writeBoolean(data.hasEvasion);
            buffer.writeBoolean(data.hasHeartstop);
            buffer.writeFloat(data.heartStopDamage);
        }

        public SyncedSpellData read(FriendlyByteBuf buffer) {
            var data = new SyncedSpellData(buffer.readInt());
            data.hasAngelWings = buffer.readBoolean();
            data.hasEvasion = buffer.readBoolean();
            data.hasHeartstop = buffer.readBoolean();
            data.heartStopDamage = buffer.readFloat();
            return data;
        }
    };

    public void saveNBTData(CompoundTag compound) {
        compound.putBoolean("hasAngelWings", this.hasAngelWings);
        compound.putBoolean("hasEvasion", this.hasEvasion);
        compound.putBoolean("hasHeartstop", this.hasHeartstop);
        compound.putFloat("heartStopDamage", this.heartStopDamage);
    }

    public void loadNBTData(CompoundTag compound) {
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
            Messages.sendToPlayer(new ClientBoundSyncPlayerData(this), serverPlayer);
            Messages.sendToPlayersTrackingEntity(new ClientBoundSyncPlayerData(this), serverPlayer);
        } else if (livingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            abstractSpellCastingMob.doSyncSpellData();
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientBoundSyncPlayerData(this), serverPlayer);
    }

    public boolean getHasAngelWings() {
        return hasAngelWings;
    }

    public void setHasAngelWings(boolean hasAngelWings) {
        this.hasAngelWings = hasAngelWings;
        doSync();
    }

    public boolean getHasEvasion() {
        return hasEvasion;
    }

    public void setHasEvasion(boolean hasEvasion) {
        this.hasEvasion = hasEvasion;
        doSync();
    }

    public boolean getHasHeartstop() {
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
}
