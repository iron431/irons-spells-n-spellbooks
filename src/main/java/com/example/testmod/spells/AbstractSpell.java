package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.client.ClientManaData;
import com.example.testmod.capabilities.mana.data.ManaManager;
import com.example.testmod.capabilities.mana.data.PlayerMana;
import com.example.testmod.capabilities.mana.network.PacketCastSpell;
import com.example.testmod.capabilities.mana.network.PacketSyncManaToClient;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfig;

import java.util.List;

public abstract class AbstractSpell {
    private final SpellType spellType;
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    protected int cooldown;
    protected int cooldownRemaining;
    public final TranslatableComponent displayName;

    public AbstractSpell(SpellType spellEnum, TranslatableComponent displayName) {
        this.spellType = spellEnum;
        this.displayName = displayName;
    }

    public int getID() {
        return this.spellType.getValue();
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
            default -> {
                return new FireballSpell(level);
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

        MinecraftServer server = world.getServer();
        List<ServerPlayer> serverPlayers = server.getPlayerList().getPlayers();

        ServerPlayer serverPlayer = null;
        for (ServerPlayer sp : serverPlayers) {
            if (sp.getId() == player.getId()) {
                serverPlayer = sp;
                break;
            }
        }

        if (serverPlayer != null) {
            ManaManager manaManager = ManaManager.get(world);
            PlayerMana playerMana = manaManager.getFromPlayerCapability(serverPlayer);

            if (playerMana.getMana() <= 0) {
                player.sendMessage(new TextComponent("Out of mana").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else if (playerMana.getMana() - getManaCost() < 0) {
                player.sendMessage(new TextComponent("Not enough mana to cast spell").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            } else {
                onCast(stack, world, player);
                int newMana = playerMana.getMana() - getManaCost();
                manaManager.setPlayerCurrentMana(serverPlayer, newMana);
                Messages.sendToPlayer(new PacketSyncManaToClient(newMana), serverPlayer);
            }
            return true;
        }
        return false;
    }

    public abstract void onCast(ItemStack stack, Level world, Player player);

    public float getPercentCooldown() {
        //return 0.75f;
        return Mth.clamp(cooldownRemaining / ((float) cooldown), 0, 1);
    }

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (this.spellType == o.spellType && this.level == o.level) {
            return true;
        }
        return false;
    }
}
