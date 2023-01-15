package com.example.testmod.entity.mobs.simple_wizard;

import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.PatrolNearLocationGoal;
import com.example.testmod.entity.mobs.goals.WizardAttackGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SimpleWizard extends AbstractSpellCastingMob {

    public SimpleWizard(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new PatrolNearLocationGoal(this, 30, .25f));
        this.goalSelector.addGoal(2, new WizardAttackGoal(this, .5, 600));
        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isValidTarget));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 1);
    }
}
