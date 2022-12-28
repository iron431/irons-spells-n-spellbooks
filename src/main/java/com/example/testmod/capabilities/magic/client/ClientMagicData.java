package com.example.testmod.capabilities.magic.client;

import com.example.testmod.capabilities.magic.data.CooldownInstance;
import com.example.testmod.capabilities.magic.data.PlayerCooldowns;
import com.example.testmod.spells.SpellType;
import com.google.common.collect.Maps;

import java.util.Map;

public class ClientMagicData {
    private static int playerMana;
    private static PlayerCooldowns playerCooldowns;

    public static void setMana(int playerMana) {
        ClientMagicData.playerMana = playerMana;
    }

    public static void setCooldowns(Map<SpellType, CooldownInstance> spellCooldowns) {
        ClientMagicData.playerCooldowns = new PlayerCooldowns(spellCooldowns);
    }

    public static PlayerCooldowns getCooldowns() {
        return ClientMagicData.playerCooldowns;
    }

    public static int getPlayerMana() {
        return playerMana;
    }
}