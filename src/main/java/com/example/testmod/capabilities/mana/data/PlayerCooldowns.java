package com.example.testmod.capabilities.mana.data;

import com.example.testmod.spells.SpellType;
import com.google.common.collect.Maps;

import java.util.Map;

public class PlayerCooldowns {

    //spell type and for how many more ticks it will be on cooldown
    private final Map<SpellType, CooldownInstance> spellCooldowns = Maps.newHashMap();

    public void tick() {
        spellCooldowns.forEach((spell, cooldown) -> {
            boolean cooldownOver = decrementCooldown(cooldown);
            if (cooldownOver)
                spellCooldowns.remove(spell);
        });
    }

    public void addCooldown(SpellType spell, int duration) {
        if (!spellCooldowns.containsKey(spell))
            spellCooldowns.put(spell, new CooldownInstance(duration));
    }

    public boolean isOnCooldown(SpellType spell) {
        return spellCooldowns.containsKey(spell);
    }

    public float getCooldownPercent(SpellType spell) {
        return spellCooldowns.getOrDefault(spell, new CooldownInstance(0)).getCooldownPercent();
    }

    private boolean decrementCooldown(CooldownInstance c) {
        c.decrement();
        return c.getCooldown() <= 0;
    }

    class CooldownInstance {
        private int cooldownRemaining;
        private final int spellCooldown;

        public CooldownInstance(int duration) {
            cooldownRemaining = duration;
            spellCooldown = duration;
        }

        public void decrement() {
            cooldownRemaining--;
        }

        public int getCooldown() {
            return cooldownRemaining;
        }

        public float getCooldownPercent() {
            if (cooldownRemaining == 0) return 0;

            return cooldownRemaining / (float) spellCooldown;
        }
    }
}
