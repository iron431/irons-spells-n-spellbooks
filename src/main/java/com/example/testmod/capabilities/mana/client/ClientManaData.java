package com.example.testmod.capabilities.mana.client;

public class ClientManaData {
    private static int playerMana;

    public static void set(int playerMana) {
        ClientManaData.playerMana = playerMana;
    }

    public static int getPlayerMana() {
        return playerMana;
    }
}