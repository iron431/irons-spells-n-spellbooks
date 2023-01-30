package com.example.testmod.entity.wisp;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.goals.AcquireTargetNearLocationGoal;
import com.example.testmod.entity.mobs.goals.WispAttackGoal;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

public class WispEntity extends PathfinderMob implements IAnimatable {

    @Nullable
    private UUID ownerUUID;

    @Nullable
    private Entity cachedOwner;

    @SuppressWarnings("removal")
    private final AnimationFactory factory = new AnimationFactory(this);

    @SuppressWarnings("removal")
    private final AnimationBuilder animationBuilder = new AnimationBuilder().addAnimation("animation.wisp.flying", true);

    private Vec3 targetSearchStart;
    private int durationToLive;

    public WispEntity(EntityType<? extends WispEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public WispEntity(Level levelIn, LivingEntity owner, Vec3 targetSearchStart, int durationToLive) {
        this(EntityRegistry.WISP.get(), levelIn);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.targetSearchStart = targetSearchStart;
        this.durationToLive = durationToLive;
        setOwner(owner);

        var xRot = owner.getXRot();
        var yRot = owner.getYRot();

        this.setYRot(yRot);
        this.setXRot(xRot);
        this.setYBodyRot(yRot);
        this.setYHeadRot(yRot);

        TestMod.LOGGER.debug("WispEntity: Owner - xRot:{}, yRot:{}", xRot, yRot);
        TestMod.LOGGER.debug("WispEntity: Wisp - xRot:{}, yRot:{}, look:{}", this.getXRot(), this.getYRot(), this.getLookAngle());
    }

    @Override
    protected void registerGoals() {
        TestMod.LOGGER.debug("WispEntity.registerGoals");
        this.goalSelector.addGoal(2, new WispAttackGoal(this, .5));
        this.targetSelector.addGoal(1, new AcquireTargetNearLocationGoal<>(
                this,
                LivingEntity.class,
                0,
                false,
                true,
                this::isValidTarget));
    }

    public boolean isValidTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null &&
                livingEntity.isAlive() &&
                (livingEntity instanceof Monster || livingEntity instanceof Player) &&
                livingEntity.getUUID() != ownerUUID) {
            return true;
        }
        return false;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public LivingEntity getTarget() {
        return super.getTarget();
    }

    @Override
    public void tick() {
        super.tick();
        durationToLive--;
        if (durationToLive <= 0) {
            kill();
        }
    }

    public void setOwner(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel) {
            public boolean isStableDestination(BlockPos blockPos) {
                return !this.level.getBlockState(blockPos.below()).isAir();
            }

            public void tick() {
                super.tick();
            }
        };
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height * 0.6F;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale((double) 0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            } else {
                this.moveRelative(this.getSpeed(), pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale((double) 0.91F));
            }
        }

        this.calculateEntityAnimation(this, false);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {
        super.setTarget(target);

        TestMod.LOGGER.debug("WispEntity.setTarget: {}", target);
    }

    @Override
    protected void customServerAiStep() {
        if (this.cachedOwner == null || !this.cachedOwner.isAlive()) {
            this.kill();
        }
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(animationBuilder);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.FLYING_SPEED, .2)
                .add(Attributes.MOVEMENT_SPEED, .2);

    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }
}
