package io.redspace.ironsspellbooks.entity.mobs;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface SupportMob {
    @Nullable
    LivingEntity getSupportTarget();

    void setSupportTarget(LivingEntity target);
}
