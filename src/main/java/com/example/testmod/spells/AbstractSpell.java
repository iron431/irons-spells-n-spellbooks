package com.example.testmod.spells;

import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.capabilities.magic.network.PacketCastingState;
import com.example.testmod.capabilities.magic.network.PacketSyncManaToClient;
import com.example.testmod.setup.Messages;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.example.testmod.registries.AttributeRegistry.COOLDOWN_REDUCTION;

public abstract class AbstractSpell {
    private final SpellType spellType;
    private final CastType castType;
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    //All time values in ticks
    protected int castTime;
    protected int cooldown;

    public AbstractSpell(SpellType spellType) {
        this.spellType = spellType;
        this.castType = spellType.getCastType();
    }

    public int getID() {
        return this.spellType.getValue();
    }

    public SpellType getSpellType() {
        return this.spellType;
    }

    public CastType getCastType() {
        return this.castType;
    }

    public SchoolType getSchoolType() {
        return spellType.getSchoolType();
    }

    public int getLevel() {
        return this.level;
    }

    public int getManaCost() {
        return baseManaCost + manaCostPerLevel * (level - 1);
    }

    public int getSpellPower() {
        return baseSpellPower + spellPowerPerLevel * (level - 1);
    }

    public int getSpellCooldown() {
        return this.cooldown;
    }

    public int getCastTime() {
        return this.castTime;
    }


    public void setLevel(int level) {
        this.level = level;
    }

    public static AbstractSpell getSpell(SpellType spellType, int level) {
        return spellType.getSpellForType(level);
    }

    public static AbstractSpell getSpell(int spellId, int level) {
        return getSpell(SpellType.values()[spellId], level);
    }

    //returns true/false for success/failure to cast
    public boolean attemptInitiateCast(ItemStack stack, Level world, Player player, boolean consumeMana, boolean triggerCooldown) {
        if (world.isClientSide) {
            //TODO: handle client/server delineation in onCast, not here; this breaks all client side spells
            return false;
        }
        var serverPlayer = Utils.getServerPlayer(world, player.getUUID());

        if (serverPlayer != null) {
            MagicManager magicManager = MagicManager.get(world);
            var playerMagicData = magicManager.getPlayerMagicData(serverPlayer);
            int playerMana = playerMagicData.getMana();

            boolean hasEnoughMana = playerMana - getManaCost() >= 0;
            boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(spellType);
            boolean isAlreadyCasting = playerMagicData.isCasting();

            if (!hasEnoughMana) {
                player.sendMessage(new TextComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return false;
            }
            if (isSpellOnCooldown) {
                player.sendMessage(spellType.getDisplayName().append(" is on cooldown").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return false;
            }
            if (isAlreadyCasting) {
                return false;
            }

            if (this.castType == CastType.INSTANT) {
                return castSpell(world, serverPlayer, consumeMana, triggerCooldown);
            } else if (this.castType == CastType.LONG || this.castType == CastType.CONTINUOUS) {
                playerMagicData.initiateCast(getID(), level, castTime);
                Messages.sendToPlayer(new PacketCastingState(getID(), castTime, castType, false), serverPlayer);
            }
//            if (playerMana - getManaCost() < 0) {
//                player.sendMessage(new TextComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
//            } else if (playerMagicData.getPlayerCooldowns().isOnCooldown(spellType)) {
//                player.sendMessage(new TextComponent(this.spellType.getDisplayName().getString() + " is on cooldown").withStyle(ChatFormatting.RED), Util.NIL_UUID);
//            } else if (playerMagicData.isCasting()) {
//                //player.sendMessage(new TextComponent(this.spellType.getDisplayName().getString() + " is already casting").withStyle(ChatFormatting.RED), Util.NIL_UUID);
//            } else if (castTime > 0) {
//                playerMagicData.setCasting(true);
//                playerMagicData.setCastingSpellId(getID());
//                playerMagicData.setCastingSpellLevel(level);
//                playerMagicData.setCastDuration(castTime);
//                playerMagicData.setCastDurationRemaining(castTime);
//                Messages.sendToPlayer(new PacketCastingState(getID(), castTime, castType, false), serverPlayer);
//            } else {
//            }
        }
        return false;
    }

    public boolean castSpell(Level world, ServerPlayer serverPlayer, boolean consumeMana, boolean triggerCooldown) {
        MagicManager magicManager = MagicManager.get(serverPlayer.level);
        PlayerMagicData playerMagicData = magicManager.getPlayerMagicData(serverPlayer);

        int newMana = playerMagicData.getMana();
        if (consumeMana) {
            newMana -= getManaCost();
            magicManager.setPlayerCurrentMana(serverPlayer, newMana);
        }

        if (triggerCooldown) {
            MagicManager.get(serverPlayer.level).addCooldown(serverPlayer, spellType);
        }

        onCast(world, serverPlayer);
        Messages.sendToPlayer(new PacketSyncManaToClient(playerMagicData), serverPlayer);

        return true;
    }

    private int getCooldownLength(ServerPlayer serverPlayer) {
        double playerCooldownModifier = serverPlayer.getAttributeValue(COOLDOWN_REDUCTION.get());
        int effectiveCooldown = MagicManager.getEffectiveSpellCooldown(cooldown, playerCooldownModifier);
        return effectiveCooldown;
    }

    protected abstract void onCast(Level world, Player player);

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (o == null)
            return false;
        return this.spellType == o.spellType && this.level == o.level;
    }
}
