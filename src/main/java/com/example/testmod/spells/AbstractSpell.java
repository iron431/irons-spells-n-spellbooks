package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.capabilities.magic.network.PacketSyncMagicDataToClient;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractSpell {
    private final SpellType spellType;
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    protected int cooldown;
    public final TranslatableComponent displayName;

    public AbstractSpell(SpellType spellEnum, TranslatableComponent displayName) {
        this.spellType = spellEnum;
        this.displayName = displayName;
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

    public String getSpellName() {
        return this.displayName.getKey();
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
                player.sendMessage(new TextComponent(displayName.getKey() + " is on cooldown").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else {
                int newMana = playerMana - getManaCost();
                magicManager.setPlayerCurrentMana(serverPlayer, newMana);
                TestMod.LOGGER.info("setting cooldown:" + cooldown);
                playerMagicData.getPlayerCooldowns().addCooldown(spellType, cooldown);
                onCast(stack, world, player);
                Messages.sendToPlayer(new PacketSyncMagicDataToClient(playerMagicData), serverPlayer);
                return true;
            }
        }
        return false;
    }

    public abstract void onCast(ItemStack stack, Level world, Player player);

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (this.spellType == o.spellType && this.level == o.level) {
            return true;
        }
        return false;
    }
}
