package com.example.testmod.player;

import com.example.testmod.capabilities.magic.data.PlayerCooldowns;
import com.example.testmod.spells.SpellType;

public class ClientMagicData {
    static {
        ClientMagicData.playerCooldowns = new PlayerCooldowns();
        //ClientMagicData.playerCooldowns.setTickBuffer(-2);
    }

    private static int playerMana;
    private static PlayerCooldowns playerCooldowns;

    public static void setMana(int playerMana) {
        ClientMagicData.playerMana = playerMana;
    }

    public static PlayerCooldowns getCooldowns() {
        return ClientMagicData.playerCooldowns;
    }

    public static float getCooldownPercent(SpellType spellType) {
        return playerCooldowns.getCooldownPercent(spellType);
    }

    public static int getPlayerMana() {
        return playerMana;
    }

    public static SpinAttackType lastSpinAttack = SpinAttackType.RIPTIDE;

    public enum SpinAttackType {
        RIPTIDE,
        FIRE
    }
}