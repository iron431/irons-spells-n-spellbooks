package io.redspace.ironsspellbooks.capabilities.magic;

public class CooldownInstance {
    private int cooldownRemaining;
    private final int spellCooldown;

    public CooldownInstance(int spellCooldown) {
        this.spellCooldown = spellCooldown;
        this.cooldownRemaining = spellCooldown;
    }

    public CooldownInstance(int spellCooldown, int cooldownRemaining) {
        this.spellCooldown = spellCooldown;
        this.cooldownRemaining = cooldownRemaining;
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