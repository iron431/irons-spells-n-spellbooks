package io.redspace.ironsspellbooks.api.magic;

import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class MagicData {

    /*
     * New Stuff
     */
    // todo: put these into constructor
    private SpinAttackType spinAttackType = SpinAttackType.RIPTIDE;
    private LearnedSpellData learnedSpellData = new LearnedSpellData();
    private float heartStopAccumulatedDamage;
    private int evasionHitsRemaining;

    public float getHeartStopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    public void setHeartStopAccumulatedDamage(float heartStopAccumulatedDamage) {
        this.heartStopAccumulatedDamage = heartStopAccumulatedDamage;
    }

    public int getEvasionHitsRemaining() {
        return evasionHitsRemaining;
    }

    public void setEvasionHitsRemaining(int evasionHitsRemaining) {
        this.evasionHitsRemaining = evasionHitsRemaining;
    }

    public SpinAttackType getSpinAttackType() {
        return spinAttackType;
    }

    public void setSpinAttackType(SpinAttackType spinAttackType) {
        this.spinAttackType = spinAttackType;
        // todo: full skillcasting takeover
        this.syncedSpellData.setSpinAttackType(spinAttackType);
    }

    public LearnedSpellData getLearnedSpellData() {
        return learnedSpellData;
    }

    /*
     * New Stuff End
     */

    @Deprecated(forRemoval = true)
    private boolean isMob = false;

    @Deprecated(forRemoval = true)
    public MagicData(boolean isMob) {
        this.isMob = isMob;
    }

    @Deprecated(forRemoval = true)
    public MagicData() {
        this(false);
    }

    @Deprecated(forRemoval = true)
    public MagicData(ServerPlayer serverPlayer) {
        this(false);
        this.serverPlayer = serverPlayer;
        this.playerRecasts = new PlayerRecasts(serverPlayer);
    }

    @Deprecated(forRemoval = true)
    public void setServerPlayer(ServerPlayer serverPlayer) {
        if (this.serverPlayer == null && serverPlayer != null) {
            this.serverPlayer = serverPlayer;
            this.playerRecasts = new PlayerRecasts(serverPlayer);
        }
    }

    @Deprecated(forRemoval = true)
    private ServerPlayer serverPlayer = null;
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";
    public static final String RECASTS = "recasts";

    /********* MANA *******************************************************/

    private float mana;

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        //Event will not get posted if the server player is null
        ChangeManaEvent e = new ChangeManaEvent(this.serverPlayer, this, this.mana, mana);
        if (this.serverPlayer == null || !NeoForge.EVENT_BUS.post(e).isCanceled()) {
            this.mana = e.getNewMana();
        }
        if (this.serverPlayer != null) {
            float maxMana = (float) serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA);
            if (this.mana > maxMana) {
                this.mana = maxMana;
            }
        }
    }

    public void addMana(float mana) {
        setMana(this.mana + mana);
    }

    /********* SYNC DATA *******************************************************/

    @Deprecated(forRemoval = true)
    private SyncedSpellData syncedSpellData;

    @Deprecated(forRemoval = true)
    public SyncedSpellData getSyncedData() {
        if (syncedSpellData == null) {
            syncedSpellData = new SyncedSpellData(serverPlayer);
        }

        return syncedSpellData;
    }

    @Deprecated(forRemoval = true)
    public void setSyncedData(SyncedSpellData syncedSpellData) {
        this.syncedSpellData = syncedSpellData;
    }

    /********* CASTING *******************************************************/

    @Deprecated(forRemoval = true)
    private int castingSpellLevel = 0;
    @Deprecated(forRemoval = true)
    private int castDuration = 0;
    @Deprecated(forRemoval = true)
    private int castDurationRemaining = 0;
    @Deprecated(forRemoval = true)
    private CastSource castSource;
    @Deprecated(forRemoval = true)
    private CastType castType;
    @Deprecated(forRemoval = true)
    private @Nullable ICastData additionalCastData;

    private int poisonedTimestamp; //Poison does not have a damage source, so we mark when we are poisoned to ignore if instead of cancelling our long cast

    @Deprecated(forRemoval = true)
    private ItemStack castingItemStack = ItemStack.EMPTY;


    @Deprecated(forRemoval = true)
    public void resetCastingState() {
        //Ironsspellbooks.logger.debug("PlayerMagicData.resetCastingState: serverPlayer:{}", serverPlayer);
        this.castingSpellLevel = 0;
        this.castDuration = 0;
        this.castDurationRemaining = 0;
        this.castSource = CastSource.NONE;
        this.castType = CastType.NONE;
        this.getSyncedData().setIsCasting(false, "", 0, getCastingEquipmentSlot());
        resetAdditionalCastData();

        if (serverPlayer != null) {
            serverPlayer.stopUsingItem();
        }
    }

    @Deprecated(forRemoval = true)
    public void initiateCast(AbstractSpell spell, int spellLevel, int castDuration, CastSource castSource, String castingEquipmentSlot) {
        this.castingSpellLevel = spellLevel;
        this.castDuration = castDuration;
        this.castDurationRemaining = castDuration;
        this.castSource = castSource;
        this.castType = spell.getCastType();
        this.syncedSpellData.setIsCasting(true, spell.getSpellId(), spellLevel, castingEquipmentSlot);
    }

    @Deprecated(forRemoval = true)
    public ICastData getAdditionalCastData() {
        return additionalCastData;
    }

    @Deprecated(forRemoval = true)
    public void setAdditionalCastData(ICastData newCastData) {
        additionalCastData = newCastData;
    }

    @Deprecated(forRemoval = true)
    public void resetAdditionalCastData() {
        if (additionalCastData != null) {
            additionalCastData.reset();
            additionalCastData = null;
        }
    }

    @Deprecated(forRemoval = true)
    public boolean isCasting() {
        return getSyncedData().isCasting();
    }

    @Deprecated(forRemoval = true)
    public String getCastingEquipmentSlot() {
        return getSyncedData().getCastingEquipmentSlot();
    }

    @Deprecated(forRemoval = true)
    public String getCastingSpellId() {
        return getSyncedData().getCastingSpellId();
    }

    @Deprecated(forRemoval = true)
    public SpellData getCastingSpell() {
        return new SpellData(SpellRegistry.getSpell(getSyncedData().getCastingSpellId()), castingSpellLevel);
    }

    @Deprecated(forRemoval = true)
    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    @Deprecated(forRemoval = true)
    public CastSource getCastSource() {
        if (castSource == null) {
            return CastSource.NONE;
        }

        return castSource;
    }

    @Deprecated(forRemoval = true)
    public CastType getCastType() {
        return castType;
    }

    @Deprecated(forRemoval = true)
    public float getCastCompletionPercent() {
        if (castDuration == 0) {
            return 1;
        }

        return 1 - (castDurationRemaining / (float) castDuration);
    }

    @Deprecated(forRemoval = true)
    public int getCastDurationRemaining() {
        return castDurationRemaining;
    }

    @Deprecated(forRemoval = true)
    public int getCastDuration() {
        return castDuration;
    }

    @Deprecated(forRemoval = true)
    public void handleCastDuration() {
        castDurationRemaining--;

        if (castDurationRemaining <= 0) {
            castDurationRemaining = 0;
        }
    }

    @Deprecated(forRemoval = true)
    public void setPlayerCastingItem(ItemStack itemStack) {
        this.castingItemStack = itemStack;
    }

    @Deprecated(forRemoval = true)
    public ItemStack getPlayerCastingItem() {
        return this.castingItemStack;
    }

    public void markPoisoned() {
        if (this.serverPlayer != null) {
            this.poisonedTimestamp = serverPlayer.tickCount;
        }
    }

    public boolean popMarkedPoison() {
        if (this.serverPlayer != null) {
            boolean poisoned = this.serverPlayer.tickCount - poisonedTimestamp <= 1;
            //reset so magic damage on the same tick does not get marked as poison
            poisonedTimestamp = 0;
            return poisoned;
        }
        return false;
    }

    /********* COOLDOWNS *******************************************************/

    @Deprecated(forRemoval = true)
    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    @Deprecated(forRemoval = true)
    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    /********* RECASTS *******************************************************/

    @Deprecated(forRemoval = true)
    private PlayerRecasts playerRecasts = new PlayerRecasts();

    @Deprecated(forRemoval = true)
    public PlayerRecasts getPlayerRecasts() {
        // mobs cannot support the more advanced state tracking of recasts, provide no-op data holder instead
        // preserves maximum functionality
        return isMob ? new PlayerRecasts() : this.playerRecasts;
    }

    @OnlyIn(Dist.CLIENT)
    @Deprecated(forRemoval = true)
    public void setPlayerRecasts(PlayerRecasts playerRecasts) {
        this.playerRecasts = playerRecasts;
    }

    /********* SYSTEM *******************************************************/

    @Deprecated(forRemoval = true)
    public static MagicData getPlayerMagicData(LivingEntity livingEntity) {
        return livingEntity.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    public static MagicData get(IAttachmentHolder holder) {
        return holder.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    @Deprecated(forRemoval = true)
    public void saveNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putInt(MANA, (int) mana);

        if (playerCooldowns.hasCooldownsActive()) {
            compound.put(COOLDOWNS, playerCooldowns.saveNBTData());
        }

        if (playerRecasts.hasRecastsActive()) {
            compound.put(RECASTS, playerRecasts.saveNBTData(provider));
        }

        getSyncedData().saveNBTData(compound, provider);
    }

    @Deprecated(forRemoval = true)
    public void loadNBTData(CompoundTag compound, HolderLookup.Provider provider) {
        mana = compound.getInt(MANA);

        var listTag = (ListTag) compound.get(COOLDOWNS);
        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

        listTag = (ListTag) compound.get(RECASTS);
        if (listTag != null && !listTag.isEmpty()) {
            playerRecasts.loadNBTData(listTag, provider);
        }

        getSyncedData().loadNBTData(compound, provider);
    }

    @Override
    @Deprecated(forRemoval = true)
    public String toString() {
        return String.format("isCasting:%s, spellID:%s], spellLevel:%s, duration:%s, durationRemaining:%s, source:%s, type:%s",
                getSyncedData().isCasting(),
                getSyncedData().getCastingSpellId(),
                castingSpellLevel,
                castDuration,
                castDurationRemaining,
                castSource,
                castType);
    }
}
