package com.example.testmod.player;

import com.example.testmod.capabilities.magic.data.CooldownInstance;
import com.example.testmod.capabilities.magic.data.PlayerCooldowns;
import com.example.testmod.spells.CastType;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.capabilities.magic.data.PlayerMagicProvider;
import com.example.testmod.spells.SpellType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ClientMagicData {
    static {
        ClientMagicData.playerCooldowns = new PlayerCooldowns();

        getPlayerMagicData(Minecraft.getInstance().player).getPlayerCooldowns()
                .getSpellCooldowns()
                .forEach((k, v) -> {
                    ClientMagicData.playerCooldowns.getSpellCooldowns().put(k, new CooldownInstance(v.getSpellCooldown(), v.getCooldownRemaining()));
                });
    }

    /**
     * COOLDOWNS
     *************************/
    private static PlayerCooldowns playerCooldowns;

    public static PlayerCooldowns getCooldowns() {
        return ClientMagicData.playerCooldowns;
    }

    public static float getCooldownPercent(SpellType spellType) {
        return playerCooldowns.getCooldownPercent(spellType);
    }


    /**
     * MANA
     *************************/
    private static int playerMana;

    public static int getPlayerMana() {
        return playerMana;
    }

    public static void setMana(int playerMana) {
        ClientMagicData.playerMana = playerMana;
    }


    /**
     * CASTING
     *************************/
    public static boolean isCasting = false;
    public static int castDurationRemaining = 0;
    public static int castDuration = 0;
    public static CastType castType = CastType.NONE;

    public static float getCastCompletionPercent() {
        return 1 - (castDurationRemaining / (float) castDuration);
    }


    /**
     * SPIN ATTACK
     *************************/
    public static SpinAttackType lastSpinAttack = SpinAttackType.RIPTIDE;

    public enum SpinAttackType {
        RIPTIDE,
        FIRE
    }

    /**
     * HELPER
     *************************/
    public static PlayerMagicData getPlayerMagicData(Player player) {
        if (player == null) {
            var capContainer = player.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
            if (capContainer.isPresent()) {
                return capContainer.resolve().orElse(new PlayerMagicData());
            }
        }
        return new PlayerMagicData();
    }
}