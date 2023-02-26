package com.example.testmod.entity.mobs;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.entity.mobs.goals.*;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class SummonedPolarBear extends PolarBear implements MagicSummon {
    public SummonedPolarBear(EntityType<? extends PolarBear> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        maxUpStep = 1f;
        xpReward = 0;
    }

    public SummonedPolarBear(Level pLevel, LivingEntity owner) {
        this(EntityRegistry.SUMMONED_POLAR_BEAR.get(), pLevel);
        setSummoner(owner);
    }

    protected LivingEntity cachedSummoner;
    protected UUID summonerUUID;

    @Override
    public void registerGoals() {

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PolarBearMeleeAttackGoal());
        this.goalSelector.addGoal(7, new GenericFollowOwnerGoal(this, this::getSummoner, 0.9f, 15, 5, false, 25));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new GenericOwnerHurtByTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(2, new GenericOwnerHurtTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(3, new GenericCopyOwnerTargetGoal(this, this::getSummoner));
        this.targetSelector.addGoal(4, (new GenericHurtByTargetGoal(this, (entity) -> entity == getSummoner())).setAlertOthers());

    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.isVehicle()) {
            return super.mobInteract(pPlayer, pHand);
        }
        if (pPlayer == getSummoner()) {
            this.doPlayerRide(pPlayer);
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Nullable
    public Entity getControllingPassenger() {
        return this.getFirstPassenger();
    }

    protected void doPlayerRide(Player pPlayer) {
        this.setStanding(false);
        if (!this.level.isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }

    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        return Utils.doMeleeAttack(this, pEntity, SpellType.SUMMON_POLAR_BEAR_SPELL.getDamageSource(this, getSummoner()), null);
    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.summonerUUID = owner.getUUID();
            this.cachedSummoner = owner;
        }
    }

    @Override
    public LivingEntity getSummoner() {
        if (this.cachedSummoner != null && !this.cachedSummoner.isRemoved()) {
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
        //TestMod.LOGGER.debug("Reading Summoned Vex save data");

        if (compoundTag.hasUUID("Summoner")) {
            this.summonerUUID = compoundTag.getUUID("Summoner");
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        //TestMod.LOGGER.debug("Writing Summoned Vex save data");

        if (this.summonerUUID != null) {
            compoundTag.putUUID("Summoner", this.summonerUUID);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                //Health and Damage overridden by summoning via spell
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        Entity conductor = this.getControllingPassenger();
        if (this.isVehicle() && conductor instanceof LivingEntity livingEntity) {
            this.yRotO = this.getYRot();
            this.setYRot(livingEntity.getYRot());
            this.setXRot(livingEntity.getXRot());
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.yRotO;
            this.yHeadRot = this.getYRot();
            float f = livingEntity.xxa * 0.5F;
            float f1 = livingEntity.zza;
            if (this.isControlledByLocalInstance()) {
                this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * .55f);
                super.travel(new Vec3((double) f, pTravelVector.y, (double) f1));
            }
        } else {
            super.travel(pTravelVector);
        }
    }

    class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
        public PolarBearMeleeAttackGoal() {
            super(SummonedPolarBear.this, 1.25D, true);
        }

        protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
            double d0 = this.getAttackReachSqr(pEnemy);
            if (pDistToEnemySqr <= d0 && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(pEnemy);
                SummonedPolarBear.this.setStanding(false);
            } else if (pDistToEnemySqr <= d0 * 2.0D) {
                if (this.isTimeToAttack()) {
                    SummonedPolarBear.this.setStanding(false);
                    this.resetAttackCooldown();
                }

                if (this.getTicksUntilNextAttack() <= 10) {
                    SummonedPolarBear.this.setStanding(true);
                    SummonedPolarBear.this.playWarningSound();
                }
            } else {
                this.resetAttackCooldown();
                SummonedPolarBear.this.setStanding(false);
            }

        }

        @Override
        public void stop() {
            SummonedPolarBear.this.setStanding(false);
            super.stop();
        }
    }
}
