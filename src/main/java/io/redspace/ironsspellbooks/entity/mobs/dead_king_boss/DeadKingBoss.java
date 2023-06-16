package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.SpellBarrageGoal;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private final static EntityDataAccessor<Boolean> NEXT_SLAM = SynchedEntityData.defineId(DeadKingBoss.class, EntityDataSerializers.BOOLEAN);
    private int transitionAnimationTime = 140; // Animation Length in ticks
    private boolean isCloseToGround;

    public DeadKingBoss(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 60;
    }

    public DeadKingBoss(Level pLevel) {
        this(EntityRegistry.DEAD_KING.get(), pLevel);
    }

    @Override
    protected void registerGoals() {
        setFirstPhaseGoals();

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));

        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    private DeadKingAnimatedWarlockAttackGoal getCombatGoal() {
        return (DeadKingAnimatedWarlockAttackGoal) new DeadKingAnimatedWarlockAttackGoal(this, 1f, 55, 85, 3.5f).setSpellQuality(.3f, .5f).setSpells(
                List.of(
                        SpellType.RAY_OF_SIPHONING_SPELL,
                        SpellType.BLOOD_SLASH_SPELL, SpellType.BLOOD_SLASH_SPELL,
                        SpellType.WITHER_SKULL_SPELL, SpellType.WITHER_SKULL_SPELL, SpellType.WITHER_SKULL_SPELL,
                        SpellType.FANG_STRIKE_SPELL, SpellType.FANG_STRIKE_SPELL,
                        SpellType.POISON_ARROW_SPELL, SpellType.POISON_ARROW_SPELL,
                        SpellType.BLIGHT_SPELL,
                        SpellType.ACID_ORB_SPELL
                ),
                List.of(SpellType.FANG_WARD_SPELL, SpellType.BLOOD_STEP_SPELL),
                List.of(/*SpellType.BLOOD_STEP_SPELL*/),
                List.of()
        ).setMeleeBias(0.75f);
    }

    protected void setFirstPhaseGoals() {
        this.goalSelector.removeAllGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellBarrageGoal(this, SpellType.WITHER_SKULL_SPELL, 3, 4, 70, 140, 3));
        this.goalSelector.addGoal(2, new SpellBarrageGoal(this, SpellType.RAISE_DEAD_SPELL, 3, 5, 600, 900, 1));
        this.goalSelector.addGoal(3, new SpellBarrageGoal(this, SpellType.BLOOD_STEP_SPELL, 1, 1, 100, 180, 1));
        this.goalSelector.addGoal(4, getCombatGoal().setSingleUseSpell(SpellType.RAISE_DEAD_SPELL, 10, 50, 8, 8));
        this.goalSelector.addGoal(5, new PatrolNearLocationGoal(this, 32, 0.9f));
    }

    protected void setFinalPhaseGoals() {
        this.goalSelector.removeAllGoals();
        this.goalSelector.addGoal(1, new SpellBarrageGoal(this, SpellType.WITHER_SKULL_SPELL, 5, 5, 60, 140, 4));
        this.goalSelector.addGoal(2, new SpellBarrageGoal(this, SpellType.SUMMON_VEX_SPELL, 3, 5, 400, 600, 1));
        this.goalSelector.addGoal(3, new SpellBarrageGoal(this, SpellType.BLOOD_STEP_SPELL, 1, 1, 100, 180, 1));
        this.goalSelector.addGoal(4, getCombatGoal().setIsFlying().setSingleUseSpell(SpellType.BLAZE_STORM_SPELL, 10, 30, 10, 10));
        this.goalSelector.addGoal(5, new PatrolNearLocationGoal(this, 32, 0.9f));

        //this.goalSelector.addGoal(2, new VexRandomMoveGoal());
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundRegistry.DEAD_KING_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return SoundRegistry.DEAD_KING_DEATH.get();
    }

    @Override
    public boolean isPushable() {
        return !isPhaseTransitioning();
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
        return pSpawnData;
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return super.isAlliedTo(pEntity) || (pEntity instanceof MagicSummon summon && summon.getSummoner() == this);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemRegistry.BLOOD_STAFF.get()));
        this.setDropChance(EquipmentSlot.OFFHAND, 0f);
//        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.WANDERING_MAGICIAN_ROBE.get()));
//        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    //Instead of being undead (smite is ridiculous)
    @Override
    public boolean isInvertedHealAndHarm() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void tick() {
        if (isPhase(Phases.FinalPhase)) {
            //vex type beat
            setNoGravity(true);
            //this.noPhysics = true;
            if (tickCount % 10 == 0) {
                isCloseToGround = Utils.raycastForBlock(level, position(), position().subtract(0, 2.5, 0), ClipContext.Fluid.ANY).getType() == HitResult.Type.BLOCK;
            }
            Vec3 woosh = new Vec3(
                    Mth.sin((tickCount * 5) * Mth.DEG_TO_RAD),
                    (Mth.cos((tickCount * 3 + 986741) * Mth.DEG_TO_RAD) + (isCloseToGround ? .05 : -.185)) * .5f,
                    Mth.sin((tickCount * 1 + 465) * Mth.DEG_TO_RAD)
            );
            if (this.getTarget() == null)
                woosh = woosh.scale(.25f);
            this.setDeltaMovement(getDeltaMovement().add(woosh.scale(.0085f)));
        }
        super.tick();

        if (level.isClientSide) {
            if (isPhase(Phases.FinalPhase)) {
                if (!this.isInvisible()) {
                    float radius = .35f;
                    for (int i = 0; i < 5; i++) {
                        Vec3 random = position().add(new Vec3(
                                (this.random.nextFloat() * 2 - 1) * radius,
                                1 + (this.random.nextFloat() * 2 - 1) * radius,
                                (this.random.nextFloat() * 2 - 1) * radius
                        ));
                        level.addParticle(ParticleTypes.SMOKE, random.x, random.y, random.z, 0, -.1, 0);

                    }

                }
            }

        } else {
            //irons_spellbooks.LOGGER.debug("DeadKingBoss.tick | Phase: {} | isTransitioning: {} | TransitionTime: {}", getPhase(), isPhaseTransitioning(), transitionAnimationTime);
            float halfHealth = this.getMaxHealth() / 2;
            if (isPhase(Phases.FirstPhase)) {
                this.bossEvent.setProgress((this.getHealth() - halfHealth) / (this.getMaxHealth() - halfHealth));
                if (this.getHealth() <= halfHealth) {
                    setPhase(Phases.Transitioning);
                    var player = level.getNearestPlayer(this, 16);
                    if (player != null)
                        lookAt(player, 360, 360);
                    setHealth(halfHealth);
                    playSound(SoundRegistry.DEAD_KING_FAKE_DEATH.get());
                    //Overriding isInvulnerable just doesn't seem to work
                    setInvulnerable(true);
                }
            } else if (isPhase(Phases.Transitioning)) {
                if (--transitionAnimationTime <= 0) {
                    setPhase(Phases.FinalPhase);
                    MagicManager.spawnParticles(level, ParticleHelper.FIRE, position().x, position().y + 2.5, position().z, 80, .2, .2, .2, .25, true);
                    setFinalPhaseGoals();
                    setNoGravity(true);
                    playSound(SoundRegistry.DEAD_KING_EXPLODE.get());
                    level.getEntities(this, this.getBoundingBox().inflate(5), (entity) -> entity.distanceToSqr(position()) < 5 * 5).forEach(super::doHurtTarget);
                    setInvulnerable(false);
                }
            } else if (isPhase(Phases.FinalPhase)) {
                this.bossEvent.setProgress(this.getHealth() / (this.getMaxHealth() - halfHealth));
            }
        }
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (isPhase(Phases.FinalPhase))
            return false;
        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    public boolean isPhase(Phases phase) {
        return phase.value == getPhase();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        //reduces damage of projectiles and summons
        if (pSource instanceof IndirectEntityDamageSource)
            pAmount *= .75f;
        return super.hurt(pSource, pAmount);
    }

    @Override
    protected boolean isImmobile() {
        return isPhase(Phases.Transitioning) || super.isImmobile();
    }

    public boolean isPhaseTransitioning() {
        return isPhase(Phases.Transitioning);
    }

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
                .add(AttributeRegistry.SPELL_POWER.get(), 1.15)
                .add(Attributes.ARMOR, 15)
                .add(AttributeRegistry.SPELL_RESIST.get(), 1)
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ATTACK_KNOCKBACK, .6)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, .155);
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

    public void setNextSlam(boolean slam) {
        this.entityData.set(NEXT_SLAM, slam);
    }

    public boolean isNextSlam() {
        return this.entityData.get(NEXT_SLAM);
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
        if (isPhase(Phases.FinalPhase))
            setFinalPhaseGoals();

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 0);
        this.entityData.define(NEXT_SLAM, false);
    }

    private final AnimationBuilder phase_transition_animation = new AnimationBuilder().addAnimation("dead_king_die", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder idle = new AnimationBuilder().addAnimation("dead_king_idle", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder melee = new AnimationBuilder().addAnimation("dead_king_melee", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder slam = new AnimationBuilder().addAnimation("dead_king_slam", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private final AnimationController transitionController = new AnimationController(this, "dead_king_transition", 0, this::transitionPredicate);
    private final AnimationController meleeController = new AnimationController(this, "dead_king_animations", 0, this::predicate);
    private final AnimationController idleController = new AnimationController(this, "dead_king_idle", 0, this::idlePredicate);

    private PlayState predicate(AnimationEvent animationEvent) {
        var controller = animationEvent.getController();
//        if (isPhaseTransitioning() && controller.getAnimationState() == AnimationState.Stopped) {
//            controller.markNeedsReload();
//            controller.setAnimation(phase_transition_animation);
//            return PlayState.CONTINUE;
//        }
        if (this.swinging) {
            controller.markNeedsReload();
            if (isNextSlam()) {
                controller.setAnimation(slam);
            } else {
                controller.setAnimation(melee);
            }
            swinging = false;
            return PlayState.CONTINUE;
        }
        return PlayState.CONTINUE;
    }

    private PlayState transitionPredicate(AnimationEvent animationEvent) {
        var controller = animationEvent.getController();
        if (isPhaseTransitioning()) {
            controller.setAnimation(phase_transition_animation);
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
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
        data.addAnimationController(transitionController);
        data.addAnimationController(meleeController);
        data.addAnimationController(idleController);
        super.registerControllers(data);
    }

    @Override
    public boolean shouldAlwaysAnimateHead() {
        return !isPhaseTransitioning();
    }

    @Override
    public boolean isAnimating() {
        return meleeController.getAnimationState() != AnimationState.Stopped || super.isAnimating();
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        level.playSound(null, getX(), getY(), getZ(), SoundRegistry.DEAD_KING_HIT.get(), SoundSource.HOSTILE, 1, 1);
        return super.doHurtTarget(pEntity);
    }

}
