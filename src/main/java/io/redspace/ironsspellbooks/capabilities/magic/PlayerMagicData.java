package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PlayerMagicData extends AbstractMagicData {

    private boolean isMob = false;

    public PlayerMagicData(boolean isMob) {
        this.isMob = isMob;
    }

    public PlayerMagicData() {
        this(false);
    }

    public PlayerMagicData(ServerPlayer serverPlayer) {
        this(false);
        this.serverPlayer = serverPlayer;
    }

    public void setServerPlayer(ServerPlayer serverPlayer) {
        if (this.serverPlayer == null) {
            this.serverPlayer = serverPlayer;
        }
    }

    private ServerPlayer serverPlayer = null;
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";

    /********* MANA *******************************************************/

    private int mana;

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void addMana(int mana) {
        this.mana += mana;
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
    private @Nullable CastData additionalCastData;

    private ItemStack castingItemStack = ItemStack.EMPTY;


    public void resetCastingState() {
 //Ironsspellbooks.logger.debug("PlayerMagicData.resetCastingState: serverPlayer:{}", serverPlayer);
        this.castingSpellLevel = 0;
        this.castDuration = 0;
        this.castDurationRemaining = 0;
        this.castSource = CastSource.NONE;
        this.castType = CastType.NONE;
        this.getSyncedData().setIsCasting(false, SpellType.NONE_SPELL.getValue(), 0);
        resetAdditionalCastData();

        if (serverPlayer != null) {
            serverPlayer.stopUsingItem();
        } else if (!isMob) {
            Minecraft.getInstance().player.stopUsingItem();
        }
    }

    public void initiateCast(int spellId, int spellLevel, int castDuration, CastSource castSource) {
        this.castingSpellLevel = spellLevel;
        this.castDuration = castDuration;
        this.castDurationRemaining = castDuration;
        this.castSource = castSource;
        this.castType = AbstractSpell.getSpell(spellId, spellLevel).getCastType();
        this.syncedSpellData.setIsCasting(true, spellId, spellLevel);
    }

    public CastData getAdditionalCastData() {
        return additionalCastData;
    }

    public void setAdditionalCastData(CastData newCastData) {
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

    public int getCastingSpellId() {
        return getSyncedData().getCastingSpellId();
    }

    public AbstractSpell getCastingSpell() {
        return AbstractSpell.getSpell(getSyncedData().getCastingSpellId(), castingSpellLevel);
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

    /********* COOLDOWNS *******************************************************/

    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    /********* SYSTEM *******************************************************/

    public static PlayerMagicData getPlayerMagicData(LivingEntity livingEntity) {
        if (livingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            return abstractSpellCastingMob.getPlayerMagicData();
        } else if (livingEntity instanceof ServerPlayer serverPlayer) {

            var capContainer = serverPlayer.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
            if (capContainer.isPresent()) {
                var opt = capContainer.resolve();
                if (opt.isEmpty()) {
                    return new PlayerMagicData(serverPlayer);
                }

                var pmd = opt.get();
                pmd.setServerPlayer(serverPlayer);
                return pmd;
            }
            return new PlayerMagicData(serverPlayer);
        } else
            return new PlayerMagicData(null);


    }

    public void saveNBTData(CompoundTag compound) {
        compound.putInt(MANA, mana);

        if (playerCooldowns.hasCooldownsActive()) {
            ListTag listTag = new ListTag();
            playerCooldowns.saveNBTData(listTag);
            if (!listTag.isEmpty()) {
                compound.put(COOLDOWNS, listTag);
            }
        }

        getSyncedData().saveNBTData(compound);
    }

    public void loadNBTData(CompoundTag compound) {
        mana = compound.getInt(MANA);

        ListTag listTag = (ListTag) compound.get(COOLDOWNS);
        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

        getSyncedData().loadNBTData(compound);
    }

    @Override
    public String toString() {
        return String.format("isCasting:%s, spellID:%d, spellLevel:%s, duration:%s, durationRemaining:%s, source:%s, type:%s",
                getSyncedData().isCasting(),
                getSyncedData().getCastingSpellId(),
                castingSpellLevel,
                castDuration,
                castDurationRemaining,
                castSource,
                castType);
    }
}
