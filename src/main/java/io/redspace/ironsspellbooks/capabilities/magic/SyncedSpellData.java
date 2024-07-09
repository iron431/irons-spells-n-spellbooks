package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.LearnedSpellData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.network.ClientboundSyncEntityData;
import io.redspace.ironsspellbooks.network.ClientboundSyncPlayerData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SyncedSpellData {
    //syncedEffectFlags
    public static final long ANGEL_WINGS = 1;
    public static final long EVASION = 2;
    public static final long HEARTSTOP = 4;
    public static final long ABYSSAL_SHROUD = 8;
    public static final long ASCENSION = 16;
    public static final long TRUE_INVIS = 32;
    public static final long CHARGED = 64;
    public static final long PLANAR_SIGHT = 128;

    //localEffectFlags
    public static final long HEAL_TARGET = 1;

    //TODO: may want to switch this to ServerPlayer.UUID
    private final int serverPlayerId;
    private @Nullable LivingEntity livingEntity;

    private boolean isCasting;
    private String castingSpellId;
    private int castingSpellLevel;
    private long syncedEffectFlags;
    private long localEffectFlags;
    private float heartStopAccumulatedDamage;
    private int evasionHitsRemaining;
    private SpinAttackType spinAttackType;
    private LearnedSpellData learnedSpellData;
    private SpellSelection spellSelection;

    private String castingEquipmentSlot;

    //Use this on the client
    public SyncedSpellData(int serverPlayerId) {
        this.livingEntity = null;
        this.serverPlayerId = serverPlayerId;
        this.isCasting = false;
        this.castingSpellId = "";
        this.castingEquipmentSlot = "";
        this.castingSpellLevel = 0;
        this.syncedEffectFlags = 0;
        this.localEffectFlags = 0;
        this.heartStopAccumulatedDamage = 0f;
        this.evasionHitsRemaining = 0;
        this.spinAttackType = SpinAttackType.RIPTIDE;
        this.learnedSpellData = new LearnedSpellData();
        this.spellSelection = new SpellSelection();
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
            buffer.writeUtf(data.castingSpellId);
            buffer.writeInt(data.castingSpellLevel);
            buffer.writeLong(data.syncedEffectFlags);
            buffer.writeFloat(data.heartStopAccumulatedDamage);
            buffer.writeInt(data.evasionHitsRemaining);
            buffer.writeEnum(data.spinAttackType);
            buffer.writeUtf(data.castingEquipmentSlot);
            data.learnedSpellData.writeToBuffer(buffer);
            data.spellSelection.writeToBuffer(buffer);
        }

        public SyncedSpellData read(FriendlyByteBuf buffer) {
            var data = new SyncedSpellData(buffer.readInt());
            data.isCasting = buffer.readBoolean();
            data.castingSpellId = buffer.readUtf();
            data.castingSpellLevel = buffer.readInt();
            data.syncedEffectFlags = buffer.readLong();
            data.heartStopAccumulatedDamage = buffer.readFloat();
            data.evasionHitsRemaining = buffer.readInt();
            data.spinAttackType = buffer.readEnum(SpinAttackType.class);
            data.castingEquipmentSlot = buffer.readUtf();
            data.learnedSpellData.readFromBuffer(buffer);
            data.spellSelection.readFromBuffer(buffer);
            return data;
        }
    };

    public void saveNBTData(CompoundTag compound) {
        compound.putBoolean("isCasting", this.isCasting);
        compound.putString("castingSpellId", this.castingSpellId);
        compound.putString("castingEquipmentSlot", this.castingEquipmentSlot);
        compound.putInt("castingSpellLevel", this.castingSpellLevel);
        compound.putLong("effectFlags", this.syncedEffectFlags);
        compound.putFloat("heartStopAccumulatedDamage", this.heartStopAccumulatedDamage);
        compound.putFloat("evasionHitsRemaining", this.evasionHitsRemaining);

        //TODO: refactor learned spell data to use INBTSerializable instead of this custom deal
        learnedSpellData.saveToNBT(compound);
        compound.put("spellSelection", this.spellSelection.serializeNBT());
        //SpinAttack not saved
    }

    public void loadNBTData(CompoundTag compound) {
        this.isCasting = compound.getBoolean("isCasting");
        this.castingSpellId = compound.getString("castingSpellId");
        this.castingEquipmentSlot = compound.getString("castingEquipmentSlot");
        this.castingSpellLevel = compound.getInt("castingSpellLevel");
        this.syncedEffectFlags = compound.getLong("effectFlags");
        this.heartStopAccumulatedDamage = compound.getFloat("heartStopAccumulatedDamage");
        this.evasionHitsRemaining = compound.getInt("evasionHitsRemaining");
        //TODO: refactor learned spell data to use INBTSerializable instead of this custom deal
        this.learnedSpellData.loadFromNBT(compound);
        this.spellSelection.deserializeNBT(compound.getCompound("spellSelection"));
        //SpinAttack not saved

    }

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public boolean hasEffect(long effectFlags) {
        return (this.syncedEffectFlags & effectFlags) == effectFlags;
    }

    public String getCastingEquipmentSlot() {
        return castingEquipmentSlot;
    }

    public boolean hasLocalEffect(long effectFlags) {
        return (this.localEffectFlags & effectFlags) == effectFlags;
    }

    public void addLocalEffect(long effectFlags) {
        this.localEffectFlags |= effectFlags;
    }

    public void removeLocalEffect(long effectFlags) {
        this.localEffectFlags &= ~effectFlags;
    }

    public float getHeartstopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    public boolean hasDodgeEffect() {
        return hasEffect(EVASION) || hasEffect(ABYSSAL_SHROUD);
    }

    public void setHeartstopAccumulatedDamage(float damage) {
        heartStopAccumulatedDamage = damage;
        doSync();
    }

    public SpellSelection getSpellSelection() {
        return spellSelection;
    }

    public void setSpellSelection(SpellSelection spellSelection) {
        if (Log.SPELL_SELECTION) {
            IronsSpellbooks.LOGGER.debug("SyncedSpellData.setSpellSelection {}", spellSelection);
        }
        this.spellSelection = spellSelection;
        doSync();
    }

    public void learnSpell(AbstractSpell spell) {
        this.learnedSpellData.learnedSpells.add(spell.getSpellResource());
        doSync();
    }

    public void forgetAllSpells() {
        this.learnedSpellData.learnedSpells.clear();
        doSync();
    }

    public boolean isSpellLearned(AbstractSpell spell) {
        return !spell.needsLearning() || this.learnedSpellData.learnedSpells.contains(spell.getSpellResource());
    }

    public SpinAttackType getSpinAttackType() {
        return spinAttackType;
    }

    public void setSpinAttackType(SpinAttackType spinAttackType) {
        this.spinAttackType = spinAttackType;
        doSync();
    }

    public int getEvasionHitsRemaining() {
        return evasionHitsRemaining;
    }

    public void subtractEvasionHit() {
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
        this.syncedEffectFlags |= effectFlags;
        doSync();
    }

    public void removeEffects(long effectFlags) {
        this.syncedEffectFlags &= ~effectFlags;
        doSync();
    }

    public void doSync() {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientboundSyncPlayerData(this), serverPlayer);
            Messages.sendToPlayersTrackingEntity(new ClientboundSyncPlayerData(this), serverPlayer);
        } else if (livingEntity instanceof IMagicEntity abstractSpellCastingMob) {
                Messages.sendToPlayersTrackingEntity(new ClientboundSyncEntityData(this, abstractSpellCastingMob), livingEntity);
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientboundSyncPlayerData(this), serverPlayer);
    }

    public void setIsCasting(boolean isCasting, String castingSpellId, int castingSpellLevel, String castingEquipmentSlot) {
        this.isCasting = isCasting;
        this.castingSpellId = castingSpellId;
        this.castingSpellLevel = castingSpellLevel;
        this.castingEquipmentSlot = castingEquipmentSlot;
        doSync();
    }

    public boolean isCasting() {
        return isCasting;
    }

    public String getCastingSpellId() {
        return castingSpellId;
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    @Override
    protected SyncedSpellData clone() {
        return new SyncedSpellData(this.livingEntity);
    }

    @Override
    public String toString() {
        return String.format("isCasting:%s, spellID:%s, spellLevel:%d, effectFlags:%d",
                isCasting,
                castingSpellId,
                castingSpellLevel,
                syncedEffectFlags);
    }

    /**
     * @return Retuns a copy of this SyncedSpellData, but with only data for things that should be persisted after death.
     */
    public SyncedSpellData getPersistentData() {
        SyncedSpellData persistentData = new SyncedSpellData(this.livingEntity);
        persistentData.learnedSpellData.learnedSpells.addAll(this.learnedSpellData.learnedSpells);
        return persistentData;
    }
}
