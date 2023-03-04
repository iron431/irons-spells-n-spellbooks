package com.example.testmod.entity.mobs.debug_wizard;

import com.example.testmod.entity.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.DebugTargetClosestEntityGoal;
import com.example.testmod.entity.mobs.goals.WizardDebugAttackGoal;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class DebugWizard extends AbstractSpellCastingMob implements Enemy, IAnimatable {

    private SpellType spellType;
    private int spellLevel;
    private boolean targetsPlayer;

    public DebugWizard(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public DebugWizard(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel, SpellType spellType, int spellLevel, boolean targetsPlayer) {
        super(pEntityType, pLevel);

        this.targetsPlayer = targetsPlayer;
        this.spellLevel = spellLevel;
        this.spellType = spellType;
        initGoals();
    }

    private void initGoals() {
        this.goalSelector.addGoal(10, new WizardDebugAttackGoal(this, this.spellType, this.spellLevel));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.35D));

        if (this.targetsPlayer) {
            this.targetSelector.addGoal(1, new DebugTargetClosestEntityGoal(this));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("spellType", spellType.getValue());
        pCompound.putInt("spellLevel", spellLevel);
        pCompound.putBoolean("targetsPlayer", targetsPlayer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        spellType = SpellType.getTypeFromValue(pCompound.getInt("spellType"));
        spellLevel = pCompound.getInt("spellLevel");
        targetsPlayer = pCompound.getBoolean("targetsPlayer");

        initGoals();
    }

    @Override
    protected void registerGoals() {

    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, .4);
    }


    /**
     * EXPERIMENTAL
     **/

    @SuppressWarnings("removal")
    private final AnimationFactory factory = new AnimationFactory(this);

    @SuppressWarnings("removal")
    private final AnimationBuilder animationBuilder = new AnimationBuilder().addAnimation("instant_cast", true);



    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(animationBuilder);
        return PlayState.CONTINUE;
    }
}
