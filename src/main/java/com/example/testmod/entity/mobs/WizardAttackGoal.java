package com.example.testmod.entity.mobs;

import net.minecraft.world.entity.ai.goal.Goal;

public class WizardAttackGoal extends Goal {
    @Override
    public boolean canUse() {
        return false;
    }
}
