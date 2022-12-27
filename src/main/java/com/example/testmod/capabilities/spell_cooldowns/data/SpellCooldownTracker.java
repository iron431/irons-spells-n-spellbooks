//package com.example.testmod.capabilities.spell_cooldowns.data;
//
//import com.example.testmod.TestMod;
//import com.example.testmod.spells.SpellType;
//
//import java.util.HashMap;
//
//public class SpellCooldownTracker {
//    private static int elapsedTicks;
//    private HashMap<SpellType, Integer> cooldowns;
//
//    public boolean isOnCooldown(SpellType spell) {
//        return false;
//    }
//    public static void tick(){
//        elapsedTicks++;
//        if(elapsedTicks%40==0)
//            TestMod.LOGGER.info("Tick");
//    }
//}
