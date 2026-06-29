package io.redspace.ironsspellbooks.entity.spells.scapegoat;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.NotIdioticNavigation;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class ScapegoatEntity extends PathfinderMob implements AntiMagicSusceptible, GeoEntity {
    class RunToTargetGoal extends Goal {
        int pathingDelay;

        @Override
        public boolean canUse() {
            return targetPos != null && ScapegoatEntity.this.distanceToSqr(ScapegoatEntity.this.targetPos.getCenter()) > 1.5 * 1.5;
        }

        @Override
        public void tick() {
            if (targetPos == null) {
                // logically impossible
                return;
            }
            var path = ScapegoatEntity.this.navigation.getPath();
            if (path != null && path.isDone() || (path == null && (pathingDelay-- <= 0 || tickCount < 20))) {
                pathingDelay = 30;
                Vec3 flee = ScapegoatEntity.this.targetPos.getCenter();
//                Vec3 flee = Optional.ofNullable(DefaultRandomPos.getPosTowards(ScapegoatEntity.this, 16, 7, ScapegoatEntity.this.targetPos.getCenter(), 1f)).orElse(ScapegoatEntity.this.targetPos.getCenter());
                ScapegoatEntity.this.getNavigation().moveTo(flee.x, flee.y, flee.z, 1);
            }
        }

        @Override
        public void stop() {
            ScapegoatEntity.this.navigation.stop();
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new NotIdioticNavigation(this, level);
    }

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    @Nullable
    protected BlockPos targetPos;

    protected int jumpDelay;

    protected int durationRemaining;

    public int getDurationRemaining() {
        return durationRemaining;
    }

    public void setDurationRemaining(int durationRemaining) {
        this.durationRemaining = durationRemaining;
    }

    public ScapegoatEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public ScapegoatEntity(Level level) {
        this(EntityRegistry.SCAPEGOAT.get(), level);
    }

    public void setTargetPos(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RunToTargetGoal());
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (jumpDelay > 0) {
            jumpDelay--;
        } else if (jumpDelay == 0) {
            jumpDelay = 20;
            this.playSound(SoundEvents.GOAT_DEATH, 2f, 1f + random.nextFloat() * .2f);
            if (level.isClientSide) {
                animationToPlay = RawAnimation.begin().thenPlay("scapegoat_run_accent");
            }
        }
        if (tickCount % 5 == 0) {
            poofParticles(1, 1);
        }
        if (durationRemaining > 0) {
            if (--durationRemaining == 0) {
                poofAndDiscard();
            }
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level instanceof ServerLevel serverlevel) {
            this.cachedOwner = serverlevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!DamageSources.isFriendlyFireBetween(source.getEntity(), this.getOwner()) && super.hurt(source, amount)) {
            if (level instanceof ServerLevel serverLevel) {
                poofParticles(10, 0.4f);
            }
            return true;
        }
        return false;
    }

    public void poofAndDiscard() {
        if (level instanceof ServerLevel serverLevel) {
            poofParticles(30, 1f);
            discard();
        }
    }

    @Override
    public float getVoicePitch() {
        return 0.5f;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.BREEZE_WIND_CHARGE_BURST.value();
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        this.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 1, 0.75f);
    }

    public void poofParticles(int amount, float strength) {
        if (level.isClientSide) return;
        Vec3 pos = this.getBoundingBox().getCenter();
        MagicManager.spawnParticles(level, ParticleRegistry.FALLING_SPARKLE_PARTICLE.get(), pos.x, pos.y + 0.5, pos.z, amount, .1, .1, .1, .3 * strength, false);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        poofAndDiscard();
    }

    @Override
    public void die(DamageSource damageSource) {
        // preserve event firing with super call, but immediately discard without animation
        super.die(damageSource);
        if (dead) {
            poofAndDiscard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        if (this.targetPos != null) {
            compound.put("scapegoat_target", NbtUtils.writeBlockPos(this.targetPos));
        }
        if (durationRemaining > 0) {
            compound.putInt("duration_remaining", durationRemaining);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
            this.cachedOwner = null;
        }
        NbtUtils.readBlockPos(compound, "scapegoat_target").ifPresent(p_325838_ -> this.targetPos = p_325838_);
        this.durationRemaining = compound.getInt("duration_remaining");
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 4)
                .add(Attributes.STEP_HEIGHT, 1)
                .add(Attributes.MOVEMENT_SPEED, .35);
    }

    /*
     Geckolib Impl
     */
    RawAnimation animationToPlay = null;

    private PlayState animationPredicate(AnimationState<ScapegoatEntity> event) {
        var controller = event.getController();
        if (this.animationToPlay != null) {
            controller.forceAnimationReset();
            controller.setAnimation(animationToPlay);
            animationToPlay = null;
        }
        return PlayState.CONTINUE;
    }

    private final RawAnimation ANIMATION = RawAnimation.begin().thenPlay("emerge");

    private final AnimationController controller = new AnimationController(this, "controller", 0, this::animationPredicate);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
}
