package com.example.testmod.entity.mobs;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface MagicSummon extends AntiMagicSusceptible {
    LivingEntity getSummoner();

    void onUnSummon();

    @Override
    default void onAntiMagic(PlayerMagicData playerMagicData) {
        onUnSummon();
    }

}
