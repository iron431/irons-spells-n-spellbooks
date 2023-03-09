package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import net.minecraft.world.entity.LivingEntity;

public interface MagicSummon extends AntiMagicSusceptible {
    LivingEntity getSummoner();

    void onUnSummon();

    @Override
    default void onAntiMagic(PlayerMagicData playerMagicData) {
        onUnSummon();
    }

}
