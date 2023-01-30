package com.example.testmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

public class SummonedVex extends Vex {
    public SummonedVex(Level pLevel, LivingEntity owner) {
        super(EntityType.VEX, pLevel);
    }
}
