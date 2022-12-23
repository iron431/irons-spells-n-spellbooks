package com.example.testmod.player;

public class ClientPlayerData {
    public enum SpinAttackType{
        RIPTIDE,
        FIRE
    }
    public static SpinAttackType lastSpinAttack = SpinAttackType.RIPTIDE;
}
