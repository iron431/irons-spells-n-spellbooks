package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;

import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

import javax.annotation.Nullable;
import java.util.UUID;

public class SummonedSkeleton extends Skeleton implements MagicSummon {
    public SummonedSkeleton(EntityType<? extends Skeleton> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 0;

    }

    public SummonedSkeleton(Level level, LivingEntity owner) {
        this(EntityRegistry.SUMMONED_SKELETON.get(), level);
        setSummoner(owner);
    }

    protected LivingEntity cachedSummoner;
    protected UUID summonerUUID;

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
        if (!ServerConfigs.CAN_ATTACK_OWN_SUMMONS.get() && pSource instanceof EntityDamageSource && !pSource.isBypassInvul())
            if (pSource.getEntity().equals(this.getSummoner()) || this.getSummoner().isAlliedTo(pSource.getEntity()))
                return false;
        return super.hurt(pSource, pAmount);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
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

}
