package com.example.testmod.capabilities.magic.data;

public class CooldownInstance {
    private int cooldownRemaining;
    private final int spellCooldown;

    public static CooldownInstance createCooldownInstance(int spellCooldown, int remainingCooldown) {
        return new CooldownInstance(spellCooldown, remainingCooldown);
    }

    public CooldownInstance(int spellCooldown) {
        this.cooldownRemaining = spellCooldown;
        this.spellCooldown = spellCooldown;
    }

    public CooldownInstance(int spellCooldown, int cooldownRemaining) {
        this.cooldownRemaining = cooldownRemaining;
        this.spellCooldown = spellCooldown;
    }

    public void decrement() {
        cooldownRemaining--;
    }

    public void decrementBy(int amount) {
        cooldownRemaining -= amount;
    }

    public int getCooldownRemaining() {
        return cooldownRemaining;
    }

    public int getSpellCooldown() {
        return spellCooldown;
    }

    public float getCooldownPercent() {
        if (cooldownRemaining == 0) {
            return 0;
        }

        return cooldownRemaining / (float) spellCooldown;
    }
}