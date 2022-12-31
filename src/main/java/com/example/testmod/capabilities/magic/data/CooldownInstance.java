package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;

public class CooldownInstance {
    private int cooldownRemaining;
    private final int spellCooldown;
    private final boolean isCasting;

    public CooldownInstance(int spellCooldown, boolean isCasting) {
        this.cooldownRemaining = spellCooldown;
        this.spellCooldown = spellCooldown;
        this.isCasting = isCasting;
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

    public boolean getIsCasting() {
        return isCasting;
    }

    public float getCooldownPercent() {
        if (cooldownRemaining == 0) {
            return 0;
        }

        return cooldownRemaining / (float) spellCooldown;
    }
}