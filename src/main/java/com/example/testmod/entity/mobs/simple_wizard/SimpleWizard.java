package com.example.testmod.entity.mobs.simple_wizard;

import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.PatrolNearLocationGoal;
import com.example.testmod.entity.mobs.goals.WizardAttackGoal;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class SimpleWizard extends AbstractSpellCastingMob {

    public SimpleWizard(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        var wizardAttackGoal = new WizardAttackGoal(this, 1.25f, 25, 50);
        wizardAttackGoal.setSpells( List.of(SpellType.EVASION_SPELL), List.of(SpellType.EVASION_SPELL), List.of(), List.of());

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, wizardAttackGoal);
        this.goalSelector.addGoal(3, new PatrolNearLocationGoal(this, 30, .2f));
        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isValidTarget));
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, .4);
    }
}
