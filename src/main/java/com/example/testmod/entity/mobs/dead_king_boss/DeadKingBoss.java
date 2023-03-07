package com.example.testmod.entity.mobs.dead_king_boss;

import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.PatrolNearLocationGoal;
import com.example.testmod.registries.AttributeRegistry;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import java.util.List;

public class DeadKingBoss extends AbstractSpellCastingMob implements Enemy {
    public enum Phases {
        FirstPhase(0),
        Transitioning(1),
        FinalPhase(2);
        final int value;

        Phases(int value) {
            this.value = value;
        }
    }

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true).setCreateWorldFog(true);
    private final static EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(DeadKingBoss.class, EntityDataSerializers.INT);
    private final static EntityDataAccessor<Boolean> SLAM_ATTACKING = SynchedEntityData.defineId(DeadKingBoss.class, EntityDataSerializers.BOOLEAN);
    private int transitionAnimationTime = 140; // Animation Length in ticks

    public DeadKingBoss(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 60;
    }

    public DeadKingBoss(Level pLevel) {
        this(EntityRegistry.DEAD_KING.get(), pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DeadKingAnimatedWarlockAttackGoal(this, 1f, 40, 80, 3.5f).setSpellLevels(4, 4).setSpells(
                List.of(
                        SpellType.RAY_OF_SIPHONING_SPELL,
                        SpellType.BLOOD_SLASH_SPELL, SpellType.BLOOD_SLASH_SPELL,
                        SpellType.WITHER_SKULL_SPELL, SpellType.WITHER_SKULL_SPELL, SpellType.WITHER_SKULL_SPELL,
                        SpellType.RAISE_DEAD_SPELL,
                        SpellType.FANG_STRIKE_SPELL,
                        SpellType.MAGIC_ARROW_SPELL, SpellType.MAGIC_ARROW_SPELL
                ),
                List.of(SpellType.FANG_WARD_SPELL, SpellType.FANG_WARD_SPELL, SpellType.EVASION_SPELL),
                List.of(SpellType.BLOOD_STEP_SPELL),
                List.of(SpellType.RAY_OF_SIPHONING_SPELL)
        ));
        this.goalSelector.addGoal(5, new PatrolNearLocationGoal(this, 50, 0.9f));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));

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
        if (isPhase(Phases.FirstPhase))
            return SoundEvents.SKELETON_STEP;
        else
            return SoundEvents.SOUL_ESCAPE;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        RandomSource randomsource = pLevel.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemRegistry.BLOOD_STAFF.get()));
        this.setDropChance(EquipmentSlot.OFFHAND, 0f);
//        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.WANDERING_MAGICIAN_ROBE.get()));
//        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide)
            return;
        //TestMod.LOGGER.debug("DeadKingBoss.tick | Phase: {} | isTransitioning: {} | TransitionTime: {}", getPhase(), isPhaseTransitioning(), transitionAnimationTime);
        float halfHealth = this.getMaxHealth() / 2;
        if (isPhase(Phases.FirstPhase)) {
            this.bossEvent.setProgress((this.getHealth() - halfHealth) / (this.getMaxHealth() - halfHealth));
            if (this.getHealth() <= halfHealth) {
                setPhase(Phases.Transitioning);
                var player = level.getNearestPlayer(this, 16);
                if (player != null)
                    lookAt(player, 360, 360);
                setHealth(halfHealth);
                //Overriding isInvulnerable just doesn't seem to work
                setInvulnerable(true);
            }
        } else if (isPhase(Phases.Transitioning)) {
            if (--transitionAnimationTime <= 0) {
                setPhase(Phases.FinalPhase);
                setInvulnerable(false);
            }
        } else if (isPhase(Phases.FinalPhase)) {
            this.bossEvent.setProgress(this.getHealth() / (this.getMaxHealth() - halfHealth));
        }
    }

    //    @Override
//    public boolean hurt(DamageSource pSource, float pAmount) {
//        if (isPhaseTransitioning())
//            return false;
//        else
//            return super.hurt(pSource, pAmount);
//    }

    public boolean isPhase(Phases phase) {
        return phase.value == getPhase();
    }

//    @Override
//    public boolean isInvulnerable() {
//        return isPhaseTransitioning() || super.isInvulnerable();
//    }

    @Override
    protected boolean isImmobile() {
        return isPhase(Phases.Transitioning) || super.isImmobile();
    }

    public boolean isPhaseTransitioning() {
        return isPhase(Phases.Transitioning);
    }

//
//    @Override
//    public boolean isAnimating() {
//        return isPhaseTransitioning() || super.isAnimating();
//    }

    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(AttributeRegistry.SPELL_POWER.get(), 1.5)
                .add(Attributes.ARMOR, 8)
                .add(AttributeRegistry.SPELL_RESIST.get(), 1.2)
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, .125);
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    private void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    private void setPhase(Phases phase) {
        this.setPhase(phase.value);
    }

    private int getPhase() {
        return this.entityData.get(PHASE);
    }

    public void setSlamming(boolean slam) {
        this.entityData.set(SLAM_ATTACKING, slam);
    }

    private boolean isSlamming() {
        return this.entityData.get(SLAM_ATTACKING);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("phase", getPhase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        setPhase(pCompound.getInt("phase"));

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 0);
        this.entityData.define(SLAM_ATTACKING, false);
    }

    private final AnimationBuilder phase_transition_animation = new AnimationBuilder().addAnimation("dead_king_die", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder idle = new AnimationBuilder().addAnimation("dead_king_idle", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder melee = new AnimationBuilder().addAnimation("dead_king_melee", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder slam = new AnimationBuilder().addAnimation("dead_king_slam", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private final AnimationController animationController = new AnimationController(this, "dead_king_animations", 0, this::predicate);
    private final AnimationController idlePose = new AnimationController(this, "dead_king_idle", 0, this::idlePredicate);

    private PlayState predicate(AnimationEvent animationEvent) {
        var controller = animationEvent.getController();
        if (isPhaseTransitioning() && controller.getAnimationState() == AnimationState.Stopped) {
            controller.markNeedsReload();
            controller.setAnimation(phase_transition_animation);
            return PlayState.CONTINUE;
        }
        if (this.swinging && controller.getAnimationState() == AnimationState.Stopped) {
            controller.markNeedsReload();
            if (isSlamming())
                controller.setAnimation(slam);
            else
                controller.setAnimation(melee);
            return PlayState.CONTINUE;
        }
        return PlayState.CONTINUE;
    }

    private PlayState idlePredicate(AnimationEvent animationEvent) {
        if (isAnimating())
            return PlayState.STOP;
        if (animationEvent.getController().getAnimationState() == AnimationState.Stopped)
            animationEvent.getController().setAnimation(idle);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationController);
        data.addAnimationController(idlePose);
        super.registerControllers(data);
    }

    @Override
    public boolean shouldAlwaysAnimateHead() {
        return !isPhaseTransitioning();
    }

}
