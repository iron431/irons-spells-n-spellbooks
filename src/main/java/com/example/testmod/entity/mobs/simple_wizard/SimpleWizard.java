package com.example.testmod.entity.mobs.simple_wizard;

import com.example.testmod.entity.mobs.PatrolNearLocationGoal;
import com.example.testmod.entity.mobs.WizardAttackGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SimpleWizard extends Animal {

    public SimpleWizard(EntityType<? extends Animal> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new PatrolNearLocationGoal(this, 30, .25f));
        this.goalSelector.addGoal(2, new WizardAttackGoal(this, .5, 60));
        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isValidTarget));
    }

    public boolean isValidTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.isAlive() && livingEntity instanceof Player) {
            return true;
        }
        return false;
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    public boolean save(CompoundTag tag) {
        return super.save(tag);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 1);
    }
}
