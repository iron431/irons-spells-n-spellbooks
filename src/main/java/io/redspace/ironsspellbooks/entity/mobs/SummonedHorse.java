package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericFollowOwnerGoal;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.OwnerHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SummonedHorse extends AbstractHorse implements MagicSummon {
    public SummonedHorse(EntityType<? extends AbstractHorse> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        //randomizeAttributes(Utils.random);

    }

    public SummonedHorse(Level pLevel) {
        this(EntityRegistry.SPECTRAL_STEED.get(), pLevel);
        //randomizeAttributes(Utils.random);

    }

    public SummonedHorse(Level pLevel, LivingEntity owner) {
        this(pLevel);
        setOwnerUUID(owner.getUUID());
        setSummoner(owner);
    }

    protected LivingEntity cachedSummoner;

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GenericFollowOwnerGoal(this, this::getSummoner, 0.8f, 12, 4, false, 32));
        this.goalSelector.addGoal(3, new PanicGoal(this, 0.9f));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

    }

    @Override
    public void openCustomInventoryScreen(Player pPlayer) {
        return;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 15)
                .add(Attributes.JUMP_STRENGTH, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override
    public void tick() {
        spawnParticles();
        super.tick();
    }

    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.HORSE_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.HORSE_DEATH;
    }

    public void onUnSummon() {
        if (!level.isClientSide) {
            MagicManager.spawnParticles(level, ParticleTypes.POOF, getX(), getY(), getZ(), 25, .4, .8, .4, .03, false);
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (shouldIgnoreDamage(pSource))
            return false;
        return super.hurt(pSource, pAmount);
    }

    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.HORSE_HURT;
    }

    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.HORSE_ANGRY;
    }

    public void spawnParticles() {

        if (level.isClientSide) {
            if (Utils.random.nextFloat() < .25f) {
                float radius = .75f;
                Vec3 vec = new Vec3(
                        random.nextFloat() * 2 * radius - radius,
                        random.nextFloat() * 2 * radius - radius,
                        random.nextFloat() * 2 * radius - radius
                );
                level.addParticle(ParticleTypes.ENCHANT, this.getX() + vec.x, this.getY() + vec.y + 1, this.getZ() + vec.z, vec.x * .01f, .08 + vec.y * .01f, vec.z * .01f);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.isVehicle()) {
            return super.mobInteract(pPlayer, pHand);
        }
        if (pPlayer == getSummoner()) {
            this.doPlayerRide(pPlayer);
        } else {
            this.makeMad();
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override
    public LivingEntity getSummoner() {
        return OwnerHelper.getAndCacheOwner(level, cachedSummoner, getOwnerUUID());
    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner != null) {
            setOwnerUUID(owner.getUUID());
            this.cachedSummoner = owner;
        }
    }

    @Override
    public void die(DamageSource pDamageSource) {
        this.onDeathHelper();
        super.die(pDamageSource);
    }

    @Override
    public void onRemovedFromWorld() {
        this.onRemovedHelper(this, MobEffectRegistry.SUMMON_HORSE_TIMER.get());
        super.onRemovedFromWorld();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setOwnerUUID(OwnerHelper.deserializeOwner(compoundTag));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        OwnerHelper.serializeOwner(compoundTag, getOwnerUUID());
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    @Override
    protected boolean canParent() {
        return false;
    }

    @Override
    public boolean isSaddled() {
        return true;
    }

    @Override
    public boolean isTamed() {
        return true;
    }


}
