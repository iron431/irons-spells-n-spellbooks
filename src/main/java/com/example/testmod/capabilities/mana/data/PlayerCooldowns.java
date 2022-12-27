package com.example.testmod.capabilities.mana.data;

import com.example.testmod.spells.SpellType;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

public class PlayerCooldowns {

    //spell type and for how many more ticks it will be on cooldown
    private final Map<SpellType,CooldownInstance> spellCooldowns = Maps.newHashMap();

    public void tick(){
        spellCooldowns.forEach((spell,cooldown)->{
            boolean cooldownOver = decrementCooldown(cooldown);
            if(cooldownOver)
                spellCooldowns.remove(spell);
        });
    }
    public void addCooldown(SpellType spell, int duration){
        if(!spellCooldowns.containsKey(spell))
            spellCooldowns.put(spell,new CooldownInstance(duration));
    }
    public boolean isOnCooldown(SpellType spell){
        return spellCooldowns.containsKey(spell);
    }
    public float getCooldownPercent(SpellType spell){
        if(!spellCooldowns.containsKey(spell))
            return 0f;
        return spellCooldowns.get(spell).getCooldownPercent();
    }
    private boolean decrementCooldown(CooldownInstance c){
        c.decrement();
        return c.getCooldown()<=0;
    }
    class CooldownInstance{
        private int cooldownLeft;
        private int totalCooldown;
        public CooldownInstance(int duration){
            cooldownLeft = duration;
            totalCooldown = duration;
        }
        public void decrement(){
            cooldownLeft--;
        }
        public int getCooldown(){
            return cooldownLeft;
        }
        public float getCooldownPercent(){
            return cooldownLeft / (float)totalCooldown;
        }
    }
}
