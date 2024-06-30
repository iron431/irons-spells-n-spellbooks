package io.redspace.ironsspellbooks.api.magic;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicProvider;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

public class MagicData {

    private boolean isMob = false;

    public MagicData(boolean isMob) {
        this.isMob = isMob;
    }

    public MagicData() {
        this(false);
    }

    public MagicData(ServerPlayer serverPlayer) {
        this(false);
        this.serverPlayer = serverPlayer;
        this.playerRecasts = new PlayerRecasts(serverPlayer);
    }

    public void setServerPlayer(ServerPlayer serverPlayer) {
        if (this.serverPlayer == null && serverPlayer != null) {
            this.serverPlayer = serverPlayer;
            this.playerRecasts = new PlayerRecasts(serverPlayer);
        }
    }

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
        if (this.serverPlayer == null || !MinecraftForge.EVENT_BUS.post(e)) {
            this.mana = e.getNewMana();
        }
        if (this.serverPlayer != null) {
            float maxMana = (float) serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA.get());
            if (this.mana > maxMana) {
                this.mana = maxMana;
            }
        }
    }

    public void addMana(float mana) {
        setMana(this.mana + mana);
    }

    /********* SYNC DATA *******************************************************/

    private SyncedSpellData syncedSpellData;

    public SyncedSpellData getSyncedData() {
        if (syncedSpellData == null) {
            syncedSpellData = new SyncedSpellData(serverPlayer);
        }

        return syncedSpellData;
    }

    public void setSyncedData(SyncedSpellData syncedSpellData) {
        this.syncedSpellData = syncedSpellData;
    }

    /********* CASTING *******************************************************/

    private int castingSpellLevel = 0;
    private int castDuration = 0;
    private int castDurationRemaining = 0;
    private CastSource castSource;
    private CastType castType;
    private @Nullable ICastData additionalCastData;
    private int poisonedTimestamp; //Poison does not have a damage source, so we mark when we are poisoned to ignore if instead of cancelling our long cast

    private ItemStack castingItemStack = ItemStack.EMPTY;


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
        } else if (!isMob) {
            Minecraft.getInstance().player.stopUsingItem();
        }
    }

    public void initiateCast(AbstractSpell spell, int spellLevel, int castDuration, CastSource castSource, String castingEquipmentSlot) {
        this.castingSpellLevel = spellLevel;
        this.castDuration = castDuration;
        this.castDurationRemaining = castDuration;
        this.castSource = castSource;
        this.castType = spell.getCastType();
        this.syncedSpellData.setIsCasting(true, spell.getSpellId(), spellLevel, castingEquipmentSlot);
    }

    public ICastData getAdditionalCastData() {
        return additionalCastData;
    }

    public void setAdditionalCastData(ICastData newCastData) {
        additionalCastData = newCastData;
    }

    public void resetAdditionalCastData() {
        if (additionalCastData != null) {
            additionalCastData.reset();
            additionalCastData = null;
        }
    }

    public boolean isCasting() {
        return getSyncedData().isCasting();
    }

    public String getCastingEquipmentSlot() {
        return getSyncedData().getCastingEquipmentSlot();
    }

    public String getCastingSpellId() {
        return getSyncedData().getCastingSpellId();
    }

    public SpellData getCastingSpell() {
        return new SpellData(SpellRegistry.getSpell(getSyncedData().getCastingSpellId()), castingSpellLevel);
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    public CastSource getCastSource() {
        if (castSource == null) {
            return CastSource.NONE;
        }

        return castSource;
    }

    public CastType getCastType() {
        return castType;
    }

    public float getCastCompletionPercent() {
        if (castDuration == 0) {
            return 1;
        }

        return 1 - (castDurationRemaining / (float) castDuration);
    }

    public int getCastDurationRemaining() {
        return castDurationRemaining;
    }

    public int getCastDuration() {
        return castDuration;
    }

    public void handleCastDuration() {
        castDurationRemaining--;

        if (castDurationRemaining <= 0) {
            castDurationRemaining = 0;
        }
    }

    public void setPlayerCastingItem(ItemStack itemStack) {
        this.castingItemStack = itemStack;
    }

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

    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    /********* RECASTS *******************************************************/

    private PlayerRecasts playerRecasts = new PlayerRecasts();

    public PlayerRecasts getPlayerRecasts() {
        return this.playerRecasts;
    }

    @OnlyIn(Dist.CLIENT)
    public void setPlayerRecasts(PlayerRecasts playerRecasts) {
        this.playerRecasts = playerRecasts;
    }

    /********* SYSTEM *******************************************************/

    public static MagicData getPlayerMagicData(LivingEntity livingEntity) {
        if (livingEntity instanceof IMagicEntity magicEntity) {
            return magicEntity.getMagicData();
        } else if (livingEntity instanceof ServerPlayer serverPlayer) {

            var capContainer = serverPlayer.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
            if (capContainer.isPresent()) {
                var opt = capContainer.resolve();
                if (opt.isEmpty()) {
                    return new MagicData(serverPlayer);
                }

                var pmd = opt.get();
                pmd.setServerPlayer(serverPlayer);
                return pmd;
            }
            return new MagicData(serverPlayer);
        } else {
            return new MagicData(true);
        }
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putInt(MANA, (int) mana);

        if (playerCooldowns.hasCooldownsActive()) {
            compound.put(COOLDOWNS, playerCooldowns.saveNBTData());
        }

        if (playerRecasts.hasRecastsActive()) {
            compound.put(RECASTS, playerRecasts.saveNBTData());
        }

        getSyncedData().saveNBTData(compound);
    }

    public void loadNBTData(CompoundTag compound) {
        mana = compound.getInt(MANA);

        var listTag = (ListTag) compound.get(COOLDOWNS);
        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

        listTag = (ListTag) compound.get(RECASTS);
        if (listTag != null && !listTag.isEmpty()) {
            playerRecasts.loadNBTData(listTag);
        }

        getSyncedData().loadNBTData(compound);
    }

    @Override
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
