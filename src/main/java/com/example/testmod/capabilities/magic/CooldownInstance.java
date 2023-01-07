package com.example.testmod.capabilities.magic;

public class CooldownInstance {
    private int cooldownRemaining;
    private final int spellCooldown;

    public CooldownInstance(int spellCooldown) {
        this.cooldownRemaining = spellCooldown;
        this.spellCooldown = spellCooldown;
    }

    public CooldownInstance(int spellCooldown, int cooldownRemaining) {
        this.cooldownRemaining = spellCooldown;
        this.spellCooldown = cooldownRemaining;
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