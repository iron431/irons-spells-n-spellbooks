package io.redspace.ironsspellbooks.entity.mobs.keeper;

import io.redspace.ironsspellbooks.entity.mobs.AnimatedAttacker;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class KeeperEntity extends AbstractSpellCastingMob implements Enemy, AnimatedAttacker {

    //private static final EntityDataAccessor<Integer> DATA_ATTACK_TYPE = SynchedEntityData.defineId(KeeperEntity.class, EntityDataSerializers.INT);

    public enum AttackType {
        Double_Slash,
        Triple_Slash,
        Slash_Stab,
        Lunge
    }

    public KeeperEntity(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 25;
        maxUpStep = 1f;

    }

    public KeeperEntity(Level pLevel) {
        this(EntityRegistry.KEEPER.get(), pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new KeeperAnimatedWarlockAttackGoal(this, 1f, 10, 30, 3.5f));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, true, (entity) -> !(entity instanceof KeeperEntity)));

        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        RandomSource randomsource = pLevel.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return pSpawnData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
//        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.WANDERING_MAGICIAN_ROBE.get()));
//        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FOLLOW_RANGE, 25.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.MOVEMENT_SPEED, .185);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }


    private final AnimationBuilder doubleSlash = new AnimationBuilder().addAnimation("sword_double_slash", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder lunge = new AnimationBuilder().addAnimation("sword_lunge", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder slashStab = new AnimationBuilder().addAnimation("sword_slash_stab", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder tripleSlash = new AnimationBuilder().addAnimation("sword_triple_slash", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    //In parallel with enum
    private final AnimationBuilder[] animations = new AnimationBuilder[]{
            doubleSlash, tripleSlash, slashStab, lunge
    };
    private final AnimationController meleeController = new AnimationController(this, "keeper_animations", 2, this::predicate);


    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return super.isInvulnerableTo(pSource) || pSource.isFall();
    }

    AnimationBuilder animationToPlay = null;

    @Override
    public void playAnimation(int animationId) {
        if (animationId >= 0 && animationId < animations.length)
            animationToPlay = animations[animationId];
    }

    private PlayState predicate(AnimationEvent<KeeperEntity> animationEvent) {
//        if(true)
//            return PlayState.STOP;

        var controller = animationEvent.getController();
//        if (this.swinging) {
//            controller.markNeedsReload();
//            switch (getNextAttackType()) {
//                case Double_Slash -> controller.setAnimation(doubleSlash);
//                case Lunge -> controller.setAnimation(lunge);
//                case Slash_Stab -> controller.setAnimation(slashStab);
//                case Triple_Slash -> controller.setAnimation(tripleSlash);
//            }
//            swinging = false;
//            return PlayState.CONTINUE;
//        }
        if (this.animationToPlay != null) {
            controller.markNeedsReload();
            controller.setAnimation(animationToPlay);
            animationToPlay = null;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(meleeController);
        super.registerControllers(data);
    }


    @Override
    public boolean isAnimating() {
        return meleeController.getAnimationState() != AnimationState.Stopped || super.isAnimating();
    }

    @Override
    public boolean shouldAlwaysAnimateLegs() {
        return false;
    }

    @Override
    public boolean shouldBeExtraAnimated() {
        return true/*!isAnimating()*/;
    }

    //    @Override
//    public boolean doHurtTarget(Entity pEntity) {
//        level.playSound(null, getX(), getY(), getZ(), SoundRegistry.DEAD_KING_HIT.get(), SoundSource.HOSTILE, 1, 1);
//        return super.doHurtTarget(pEntity);
//    }

}
