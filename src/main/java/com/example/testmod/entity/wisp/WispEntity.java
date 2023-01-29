package com.example.testmod.entity.wisp;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.goals.AcquireTargetNearLocationGoal;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
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

    public WispEntity(EntityType<? extends WispEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public WispEntity(Level levelIn, LivingEntity owner, Vec3 targetSearchStart) {
        this(EntityRegistry.WISP.get(), levelIn);
        setOwner(owner);
        var lxRot = cachedOwner.getXRot();
        var lyRot = cachedOwner.getYRot();
        var llook = cachedOwner.getLookAngle();

        setYRot(lyRot);
        setXRot(lxRot);
        setYBodyRot(lyRot);

        TestMod.LOGGER.debug("WispEntity: Owner - xRot:{}, yRot:{}, look:{}", lxRot, lyRot, llook);
        TestMod.LOGGER.debug("WispEntity: Wisp - xRot:{}, yRot:{}, look:{}", getXRot(), getYRot(), getLookAngle());

        this.targetSearchStart = targetSearchStart;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new AcquireTargetNearLocationGoal<>(this, LivingEntity.class, 10, true, false, this::isValidTarget));
    }

    public boolean isValidTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.isAlive() && livingEntity instanceof Player && livingEntity.getUUID() != ownerUUID) {
            return true;
        }
        return false;
    }

    public void setOwner(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
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
                .add(Attributes.MOVEMENT_SPEED, 1);
    }

}
