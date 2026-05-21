package io.redspace.ironsspellbooks.api.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.network.casting.SyncMagicDataPacket;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicData {
    public MagicData(@NotNull Entity entity) {
        this.owner = entity;
        this.castSource = CastSource.NONE;
        this.spinAttackType = SpinAttackType.RIPTIDE;
        this.learnedSpellData = new LearnedSpellData();
        this.spellSelection = new SpellSelection();
        this.castingEquipmentSlot = "";
        if (entity instanceof ServerPlayer serverPlayer) {
            this.playerRecasts = new PlayerRecasts(serverPlayer);
        } else {
            this.playerRecasts = new PlayerRecasts();
        }
    }

//    @Deprecated(forRemoval = true)
//    public MagicData(ServerPlayer serverPlayer) {
//        this((LivingEntity) serverPlayer);
//    }
//
//    @Deprecated(forRemoval = true)
//    public MagicData(boolean ignored) {
//        this((LivingEntity) null);
//    }
//
//    @Deprecated(forRemoval = true)
//    public MagicData() {
//        this((LivingEntity) null);
//    }

    public void recreateSpell(String id, int level, long start, long finish, String slot) {
        this.castingSpell = SpellRegistry.getSpell(id);
        this.castingSpellLevel = level;
        this.castStartTimestamp = start;
        this.castEndTimestamp = finish;
        this.castDuration = Math.toIntExact(finish - start);
        this.castingEquipmentSlot = slot;
    }

    private final Entity owner;
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";
    public static final String RECASTS = "recasts";

    /********* MANA *******************************************************/

    private float mana;

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            ChangeManaEvent e = new ChangeManaEvent(serverPlayer, this, this.mana, mana);
            if (!NeoForge.EVENT_BUS.post(e).isCanceled()) {
                this.mana = e.getNewMana();
            }
            float maxMana = (float) serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA);
            if (this.mana > maxMana) {
                this.mana = maxMana;
            }
        } else {
            this.mana = mana;
        }
    }

    public void addMana(float mana) {
        setMana(this.mana + mana);
    }

//    /********* SYNC DATA *******************************************************/
//
//    private SyncedSpellData syncedSpellData;
//
//    public SyncedSpellData getSyncedData() {
//        if (syncedSpellData == null) {
//            syncedSpellData = new SyncedSpellData(serverPlayer);
//        }
//
//        return syncedSpellData;
//    }
//
//    public void setSyncedData(SyncedSpellData syncedSpellData) {
//        this.syncedSpellData = syncedSpellData;
//    }

    /********* CASTING *******************************************************/
    private int castingSpellLevel = 0;
    private int castDuration = 0;
    //    private int castDurationRemaining = 0;
    private long castStartTimestamp = 0;

    public long getCastStartTimestamp() {
        return castStartTimestamp;
    }

    public long getCastEndTimestamp() {
        return castEndTimestamp;
    }

    private long castEndTimestamp = 0;
    private @NotNull CastSource castSource;
    private @Nullable ICastData additionalCastData;
    private int poisonedTimestamp; //Poison does not have a damage source, so we mark when we are poisoned to ignore if instead of cancelling our long cast
    /*
    Fields from synced spell data:
     */
//    private boolean isCasting;
//    private String castingSpellId;
    private float heartStopAccumulatedDamage;
    //    private int evasionHitsRemaining;
    private SpinAttackType spinAttackType;
    private LearnedSpellData learnedSpellData;
    private SpellSelection spellSelection;
    private String castingEquipmentSlot;
    /*
    reworking fields:
     */
    private @Nullable AbstractSpell castingSpell;

    private ItemStack castingItemStack = ItemStack.EMPTY;


    public void resetCastingState() {
        this.castingSpellLevel = 0;
        this.castDuration = 0;
//        this.castDurationRemaining = 0;
        castStartTimestamp = 0;
        castEndTimestamp = 0;
        this.castSource = CastSource.NONE;
        this.castingSpell = null;
        this.castingEquipmentSlot = "";
//        this.castType = CastType.NONE;
//        this.getSyncedData().setIsCasting(false, "", 0, getCastingEquipmentSlot());
        resetAdditionalCastData();
        doSync();

//        if (serverPlayer != null) {
//            serverPlayer.stopUsingItem();
//        }
    }

    public void initiateCast(AbstractSpell spell, int spellLevel, int castDuration, CastSource castSource, String castingEquipmentSlot) {
        this.castingSpellLevel = spellLevel;
        this.castDuration = castDuration;
//        this.castDurationRemaining = castDuration;
        this.castStartTimestamp = this.owner != null ? this.owner.level.getGameTime() : 0;
        this.castEndTimestamp = this.castStartTimestamp + castDuration;
        this.castSource = castSource;

        this.castingSpell = spell;
        this.castingEquipmentSlot = castingEquipmentSlot;
//        this.castType = spell.getCastType();
//        this.syncedSpellData.setIsCasting(true, spell.getSpellId(), spellLevel, castingEquipmentSlot);
        doSync();
    }

    public @Nullable ICastData getAdditionalCastData() {
        return additionalCastData;
    }

    public void setAdditionalCastData(@Nullable ICastData newCastData) {
        additionalCastData = newCastData;
    }

    public void resetAdditionalCastData() {
        if (additionalCastData != null) {
            additionalCastData.reset();
            additionalCastData = null;
        }
    }

    public boolean isCasting() {
//        return getSyncedData().isCasting();
        return castingSpell != null && castingSpell != SpellRegistry.none();
    }

    public String getCastingEquipmentSlot() {
//        return getSyncedData().getCastingEquipmentSlot();
        return castingEquipmentSlot;
    }

    public String getCastingSpellId() {
//        return getSyncedData().getCastingSpellId();
        return activeSpell().getSpellId();
    }

    public float getHeartstopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    public void setHeartstopAccumulatedDamage(float damage) {
        heartStopAccumulatedDamage = damage;
        doSync();
    }

    @NotNull
    public SpellData getCastingSpell() {
        return new SpellData(activeSpell(), castingSpellLevel);
    }

    @NotNull
    public AbstractSpell activeSpell() {
        return castingSpell == null ? SpellRegistry.none() : castingSpell;
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    public @NotNull CastSource getCastSource() {
        return castSource;
    }

    public CastType getCastType() {
        return activeSpell().getCastType();
    }

    public float getCastCompletionPercent() {
        if (castDuration == 0) {
            return 1;
        }
        return 1 - (getCastDurationRemaining() / (float) castDuration);
    }

    public int getCastDurationRemaining() {
        return Math.max(0, Math.toIntExact(castEndTimestamp - owner.level.getGameTime()));
    }

    public int getCastDuration() {
        return castDuration;
    }

    public void handleCastDuration() {
        // cast duration is timestamp-based now; this method remains for API compatibility.
    }

    public void setPlayerCastingItem(ItemStack itemStack) {
        this.castingItemStack = itemStack;
    }

    public ItemStack getPlayerCastingItem() {
        return this.castingItemStack;
    }

    public void markPoisoned() {
        this.poisonedTimestamp = owner.tickCount;
    }

    public boolean popMarkedPoison() {
        boolean poisoned = this.owner.tickCount - poisonedTimestamp <= 1;
        //reset so magic damage on the same tick does not get marked as poison
        poisonedTimestamp = 0;
        return poisoned;
    }

    /********* COOLDOWNS *******************************************************/

    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    /********* RECASTS *******************************************************/

    private PlayerRecasts playerRecasts = new PlayerRecasts();

    public PlayerRecasts getPlayerRecasts() {
        //todo: reevaluate need for fake data here
        // mobs cannot support the more advanced state tracking of recasts, provide no-op data holder instead
        // preserves maximum functionality
        return /*isMob ? new PlayerRecasts() :*/ this.playerRecasts;
    }

    @OnlyIn(Dist.CLIENT)
    public void setPlayerRecasts(PlayerRecasts playerRecasts) {
        this.playerRecasts = playerRecasts;
    }

    /********* SYSTEM *******************************************************/
    @Deprecated(forRemoval = true)
    public static MagicData getPlayerMagicData(LivingEntity livingEntity) {
        return livingEntity.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    public static MagicData getPlayerMagicData(Entity livingEntity) {
        return livingEntity.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    public SpellSelection getSpellSelection() {
        return spellSelection;
    }

    public void setSpellSelection(SpellSelection spellSelection) {
        if (Log.SPELL_SELECTION) {
            IronsSpellbooks.LOGGER.debug("MagicData.setSpellSelection {}", spellSelection);
        }
        this.spellSelection = spellSelection;
        doSync();
    }

    public LearnedSpellData getLearnedSpelLData() {
        return this.learnedSpellData;
    }

    public LearnedSpellData getLearnedSpellData() {
        return this.learnedSpellData;
    }

    public SpinAttackType getSpinAttackType() {
        return spinAttackType;
    }

    public void setSpinAttackType(SpinAttackType spinAttackType) {
        this.spinAttackType = spinAttackType;
        doSync();
    }

    public void doSync() {
        if (owner != null && !owner.level.isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(owner, new SyncMagicDataPacket(this, owner));
        }
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new SyncMagicDataPacket(this, owner));
    }

    public void copyPersistentDataFrom(MagicData source) {
        this.learnedSpellData.learnedSpells.clear();
        this.learnedSpellData.learnedSpells.addAll(source.getLearnedSpelLData().learnedSpells);
        var sourceSelection = source.getSpellSelection();
        this.spellSelection = new SpellSelection(sourceSelection.equipmentSlot, sourceSelection.index, sourceSelection.lastEquipmentSlot, sourceSelection.lastIndex);
    }

    public MagicData getPersistentData(ServerPlayer newOwner) {
        MagicData copy = new MagicData(newOwner);
        copy.copyPersistentDataFrom(this);
        return copy;
    }

    public void setSyncedData(MagicData source) {
        applyClientSyncData(source);
    }

    public void setSyncedData(SyncedSpellData source) {
        applyClientSyncData(source.asMagicData());
    }

    public SyncedSpellData getSyncedData() {
        return new SyncedSpellData(this);
    }

    public void applyClientSyncData(MagicData source) {
        this.recreateSpell(source.getCastingSpellId(), source.getCastingSpellLevel(), source.getCastStartTimestamp(), source.getCastEndTimestamp(), source.getCastingEquipmentSlot());
        this.heartStopAccumulatedDamage = source.getHeartstopAccumulatedDamage();
        this.spinAttackType = source.getSpinAttackType();
        this.learnedSpellData.learnedSpells.clear();
        this.learnedSpellData.learnedSpells.addAll(source.getLearnedSpelLData().learnedSpells);
        var sourceSelection = source.getSpellSelection();
        this.spellSelection = new SpellSelection(sourceSelection.equipmentSlot, sourceSelection.index, sourceSelection.lastEquipmentSlot, sourceSelection.lastIndex);
    }

    public void addHeartstopDamage(float damage) {
        this.heartStopAccumulatedDamage += damage;
        doSync();
    }

    public void saveNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        //todo: codecs would be nice. need to investigate 1.20.1 parity with them though
        compound.putInt(MANA, (int) mana);

        if (playerCooldowns.hasCooldownsActive()) {
            compound.put(COOLDOWNS, playerCooldowns.saveNBTData());
        }

        if (playerRecasts.hasRecastsActive()) {
            compound.put(RECASTS, playerRecasts.saveNBTData(provider));
        }

//        getSyncedData().saveNBTData(compound, provider);
//        compound.putString("castingSpellId", this.getCastingSpellId());
//        compound.putString("castingEquipmentSlot", this.castingEquipmentSlot);
//        compound.putInt("castingSpellLevel", this.castingSpellLevel);
        compound.putFloat("heartStopAccumulatedDamage", this.heartStopAccumulatedDamage);
//        compound.putFloat("evasionHitsRemaining", this.evasionHitsRemaining);

        //TODO: refactor learned spell data to use INBTSerializable instead of this custom deal
        learnedSpellData.saveToNBT(compound);
        compound.put("spellSelection", this.spellSelection.serializeNBT(provider));
    }

    public void loadNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        //todo: codecs would be nice. need to investigate 1.20.1 parity with them though
        mana = compound.getInt(MANA);

        var listTag = (ListTag) compound.get(COOLDOWNS);
        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

        listTag = (ListTag) compound.get(RECASTS);
        if (listTag != null && !listTag.isEmpty()) {
            playerRecasts.loadNBTData(listTag, provider);
        }

//        getSyncedData().loadNBTData(compound, provider);
//        this.castingSpellId = compound.getString("castingSpellId");
//        this.castingEquipmentSlot = compound.getString("castingEquipmentSlot");
//        this.castingSpellLevel = compound.getInt("castingSpellLevel");
        this.heartStopAccumulatedDamage = compound.getFloat("heartStopAccumulatedDamage");
//        this.evasionHitsRemaining = compound.getInt("evasionHitsRemaining");
        //TODO: refactor learned spell data to use INBTSerializable instead of this custom deal
        this.learnedSpellData.loadFromNBT(compound);
        this.spellSelection.deserializeNBT(provider, compound.getCompound("spellSelection"));
        //SpinAttack not saved
    }

    @Override
    public String toString() {
        return String.format("isCasting:%s, spellID:%s], spellLevel:%s, duration:%s, durationRemaining:%s, source:%s, type:%s",
                isCasting(),
                getCastingSpellId(),
                castingSpellLevel,
                castDuration,
                getCastDurationRemaining(),
                castSource,
                getCastType());
    }

    public @NotNull Entity getOwner() {
        return owner;
    }
}
