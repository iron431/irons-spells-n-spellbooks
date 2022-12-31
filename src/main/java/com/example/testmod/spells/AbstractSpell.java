package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.capabilities.magic.network.PacketCastSpell;
import com.example.testmod.capabilities.magic.network.PacketCastingState;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import com.example.testmod.spells.fire.TeleportSpell;
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
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int castTime;
    protected int spellPowerPerLevel;
    protected int cooldown;

    public AbstractSpell(SpellType spellEnum) {
        this.spellType = spellEnum;
    }

    public int getID() {
        return this.spellType.getValue();
    }

    public SpellType getSpellType() {
        return this.spellType;
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

    private int getEffectiveSpellCooldown(double playerCooldownModifier) {
        return (int) (cooldown * (2 - playerCooldownModifier));
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public static AbstractSpell getSpell(SpellType spellType, int level) {
        switch (spellType) {
            case BURNING_DASH_SPELL -> {
                return new BurningDashSpell(level);
            }
            case FIREBALL_SPELL -> {
                return new FireballSpell(level);
            }
            case TELEPORT -> {
                return new TeleportSpell(level);
            }
            case NONE -> {
                return new NoneSpell(0);
            }
            default -> {
                return new NoneSpell(0);
            }
        }
    }

    public static AbstractSpell getSpell(int spellId, int level) {
        return getSpell(SpellType.values()[spellId], level);
    }

    //returns true/false for success/failure to cast
    public boolean attemptCast(ItemStack stack, Level world, Player player) {
        if (world.isClientSide) {
            return false;
        }

        var serverPlayer = world.getServer().getPlayerList().getPlayer(player.getUUID());

        if (serverPlayer != null) {
            MagicManager magicManager = MagicManager.get(world);
            var playerMagicData = magicManager.getPlayerMagicData(serverPlayer);
            int playerMana = playerMagicData.getMana();

            if (playerMana <= 0) {
                player.sendMessage(new TextComponent("Out of mana").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else if (playerMana - getManaCost() < 0) {
                player.sendMessage(new TextComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else if (playerMagicData.getPlayerCooldowns().isOnCooldown(spellType)) {
                player.sendMessage(new TextComponent(this.spellType.getDisplayName().getString() + " is on cooldown").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else if (playerMagicData.isCasting()) {
                player.sendMessage(new TextComponent(this.spellType.getDisplayName().getString() + " is already casting").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else if (castTime > 0) {
                playerMagicData.setCasting(true);
                playerMagicData.setCastDuration(castTime);
                playerMagicData.setCastDurationRemaining(castTime);
                Messages.sendToPlayer(new PacketCastingState(getID(), castTime, false), serverPlayer);
            } else {
                return finishCasting(world, serverPlayer, magicManager, playerMagicData);
            }
        }
        return false;
    }

    public boolean finishCasting(Level world,ServerPlayer serverPlayer, MagicManager magicManager, PlayerMagicData playerMagicData) {
        int newMana = playerMagicData.getMana() - getManaCost();
        double playerCooldownModifier = serverPlayer.getAttributeValue(COOLDOWN_REDUCTION.get());
        int effectiveCooldown = getEffectiveSpellCooldown(playerCooldownModifier);
        magicManager.setPlayerCurrentMana(serverPlayer, newMana);
        TestMod.LOGGER.info("setting cooldown: spell cooldown:" + cooldown + " effective spell cooldown:" + effectiveCooldown);
        playerMagicData.getPlayerCooldowns().addCooldown(spellType, effectiveCooldown);
        onCast(world, serverPlayer);
        Messages.sendToPlayer(new PacketCastSpell(getID(), effectiveCooldown, newMana), serverPlayer);
        return true;
    }

    public abstract void onCast(Level world, Player player);

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (this == null || o == null)
            return this == null && o == null;
        if (this.spellType == o.spellType && this.level == o.level) {
            return true;
        }
        return false;
    }
}
