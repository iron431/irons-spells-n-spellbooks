package com.example.testmod.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.player.Player;

//https://github.com/cleannrooster/Spellblade-1.19.2/search?q=MobEffect
//https://github.com/LittleEzra/Augment-1.19.2/blob/334dc95462a3e6b25e6f73d3d909d012d63be109/src/main/java/com/littleezra/augment/item/enchantment/RecoilCurseEnchantment.java
//DamageSource
//StatusEffect
//MobEffect: https://forge.gemwire.uk/wiki/Mob_Effects/1.18

public class DamageSources {

    public static final String BLOOD_MAGIC_ID = "blood_magic";

    public static EntityDamageSource bloodSlash(Player player) {
        return new EntityDamageSource(BLOOD_MAGIC_ID, player);
    }

    public static DamageSource BLOOD_MAGIC = new DamageSource(BLOOD_MAGIC_ID);
}
