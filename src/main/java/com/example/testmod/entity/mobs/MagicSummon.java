package com.example.testmod.entity.mobs;

import net.minecraft.world.entity.LivingEntity;

public interface MagicSummon {
    LivingEntity getSummoner();

    void onUnSummon();
}
