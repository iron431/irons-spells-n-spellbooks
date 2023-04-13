package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class SummonedSkeleton extends Skeleton implements MagicSummon, IAnimatable {
    private static final EntityDataAccessor<Boolean> DATA_IS_ANIMATING_RISE = SynchedEntityData.defineId(SummonedZombie.class, EntityDataSerializers.BOOLEAN);

    public SummonedSkeleton(EntityType<? extends Skeleton> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 0;

    }

    public SummonedSkeleton(Level level, LivingEntity owner, boolean playRiseAnimation) {
        this(EntityRegistry.SUMMONED_SKELETON.get(), level);
        setSummoner(owner);
        if (playRiseAnimation)
            triggerRiseAnimation();
    }

    protected LivingEntity cachedSummoner;
    protected UUID summonerUUID;
    private int riseAnimTime = 80;

    @Override
    public void registerGoals() {

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new GenericFollowOwnerGoal(this, this::getSummoner, 0.9f, 15, 5, false, 25));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new GenericOwnerHurtByTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(2, new GenericOwnerHurtTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(3, new GenericCopyOwnerTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(4, (new GenericHurtByTargetGoal(this, (entity) -> entity == getSummoner())).setAlertOthers());

    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.summonerUUID = owner.getUUID();
            this.cachedSummoner = owner;
        }
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return super.isAlliedTo(pEntity) || pEntity == this.getSummoner();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!pSource.isBypassInvul()) {
            if (isAnimatingRise())
                return false;
            if (pSource instanceof EntityDamageSource && !ServerConfigs.CAN_ATTACK_OWN_SUMMONS.get())
                if (this.getSummoner() != null && (pSource.getEntity().equals(this.getSummoner()) || this.getSummoner().isAlliedTo(pSource.getEntity())))
                    return false;
        }

        return super.hurt(pSource, pAmount);
    }

    @Override
    public void tick() {
        if (isAnimatingRise()) {
            if (level.isClientSide)
                clientDiggingParticles(this);
            if (--riseAnimTime < 0) {
                entityData.set(DATA_IS_ANIMATING_RISE, false);
                //they do a weird head flick thing
                this.setXRot(0);
                this.setOldPosAndRot();
            }
        } else {
            super.tick();
        }
    }

    @Override
    public LivingEntity getSummoner() {
        if (this.cachedSummoner != null && this.cachedSummoner.isAlive()) {
            return this.cachedSummoner;
        } else if (this.summonerUUID != null && this.level instanceof ServerLevel) {
            if (((ServerLevel) this.level).getEntity(this.summonerUUID) instanceof LivingEntity livingEntity)
                this.cachedSummoner = livingEntity;
            return this.cachedSummoner;
        } else {
            return null;
        }
    }

    @Override
    public void onUnSummon() {
        if (!level.isClientSide) {
            MagicManager.spawnParticles(level, ParticleTypes.POOF, getX(), getY(), getZ(), 25, .4, .8, .4, .03, false);
            discard();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        //irons_spellbooks.LOGGER.debug("Reading Summoned Vex save data");

        if (compoundTag.hasUUID("Summoner")) {
            this.summonerUUID = compoundTag.getUUID("Summoner");
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        //irons_spellbooks.LOGGER.debug("Writing Summoned Vex save data");

        if (this.summonerUUID != null) {
            compoundTag.putUUID("Summoner", this.summonerUUID);
        }
    }


    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        RandomSource randomsource = pLevel.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        if (randomsource.nextDouble() < .3)
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

        this.reassessWeaponGoal();

        return pSpawnData;
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        return Utils.doMeleeAttack(this, pEntity, SpellType.RAISE_DEAD_SPELL.getDamageSource(this, getSummoner()), null);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    //
    //  Rise Animation Stuff
    //
    //

    protected void clientDiggingParticles(LivingEntity livingEntity) {
        RandomSource randomsource = livingEntity.getRandom();
        BlockState blockstate = livingEntity.getBlockStateOn();
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            for (int i = 0; i < 15; ++i) {
                double d0 = livingEntity.getX() + (double) Mth.randomBetween(randomsource, -0.5F, 0.5F);
                double d1 = livingEntity.getY();
                double d2 = livingEntity.getZ() + (double) Mth.randomBetween(randomsource, -0.5F, 0.5F);
                livingEntity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_IS_ANIMATING_RISE, false);
    }

    public boolean isAnimatingRise() {
        return entityData.get(DATA_IS_ANIMATING_RISE);
    }

    public void triggerRiseAnimation() {
        entityData.set(DATA_IS_ANIMATING_RISE, true);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isAnimatingRise();
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isAnimatingRise();
    }

    /*
    Geckolib
     */
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    //private final AnimationBuilder rise_animation = new AnimationBuilder().addAnimation("rise_from_ground", ILoopType.EDefaultLoopTypes.LOOP);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rise", 0, this::risePredicate));
    }

    private PlayState risePredicate(AnimationEvent event) {
        if (!isAnimatingRise())
            return PlayState.STOP;
        if (event.getController().getAnimationState() == AnimationState.Stopped) {
            String animation = new String[]{"rise_from_ground_01", "rise_from_ground_02", "rise_from_ground_03", "rise_from_ground_04"}[random.nextIntBetweenInclusive(0, 3)];
            event.getController().setAnimation(new AnimationBuilder().addAnimation(animation, ILoopType.EDefaultLoopTypes.LOOP));
        }
        return PlayState.CONTINUE;
    }

}
