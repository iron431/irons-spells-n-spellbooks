package io.redspace.ironsspellbooks.entity.spells.echoing_strikes;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class EchoingSword extends AbstractMagicProjectile implements IAnimatedAttacker, GeoEntity {

    public EchoingSword(EntityType<? extends EchoingSword> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    private final static int ANIM_LENGTH = 45;
    private final static int ANIM_HIT_TIMESTAMP = 25;

    public float range = 2;
    public Vec3 lastHomingPos = Vec3.ZERO;

    @Override
    public void tick() {
        this.baseTick();
        var target = getHomingTarget();
        if (lastHomingPos == Vec3.ZERO && target == null) {
            die();
            return;
        } else if (target != null) {
            lastHomingPos = target.getBoundingBox().getCenter();
        }
        this.setOldPosAndRot();
        if (tickCount == 1) {
            playAnimation("summoned_sword_basic_downswing");
        }
        moveAndRotateTowards(lastHomingPos);
        if (tickCount == ANIM_HIT_TIMESTAMP) {
            performHit();
        }
        if (tickCount >= ANIM_LENGTH) {
            die();
        }
    }

    public void moveAndRotateTowards(Vec3 target) {
        Vec3 vec3 = target.subtract(this.position()).normalize();
        Vec3 wantedPos = target.subtract(vec3.scale(range * .6f));
        Vec3 wantedMotion = wantedPos.subtract(this.position()).scale(0.1f);
        this.setDeltaMovement(getDeltaMovement().lerp(wantedMotion, 0.05f));
        this.move(MoverType.SELF, getDeltaMovement());
        this.rotateWithMotion();
    }

    private void die() {
        if (!level.isClientSide) {
            MagicManager.spawnParticles(level, ParticleHelper.ENDER_SPARKS, getX(), getY(), getZ(), 25, .1, .1, .1, 0.25, false);
        }
        this.discard();
    }

    private void performHit() {
        this.playSound(SoundRegistry.ECHOING_STRIKE.get(), 1.5f, Utils.random.nextIntBetweenInclusive(8, 12) * .1f);
        if (level.isClientSide) {
            return;
        }
        Vec3 vec3 = lastHomingPos.subtract(this.position()).normalize();
        float range = 2f;
        Vec3 center = this.position().add(vec3.scale(range));
        AABB collider = AABB.ofSize(center, explosionRadius * 2, explosionRadius * 2, explosionRadius * 2).inflate(1);
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, center.x, center.y, center.z, 25, 0, 0, 0, .18, false);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSchoolType().getTargetingColor(), explosionRadius * .9f), center.x, center.y, center.z, 1, 0, 0, 0, 0, true);
        var explosionRadiusSqr = explosionRadius * explosionRadius;
        var entities = level.getEntities(this, collider);
        for (Entity entity : entities) {
            double distanceSqr = entity.distanceToSqr(center);
            if (distanceSqr < explosionRadiusSqr && canHitEntity(entity) && Utils.hasLineOfSight(level, this.position(), entity.getBoundingBox().getCenter(), true)) {
                double p = Mth.clamp((1 - distanceSqr / explosionRadiusSqr) + .4f, 0, 1);
                float damage = (float) (this.damage * p);
                DamageSources.applyDamage(entity, damage, SpellRegistry.ECHOING_STRIKES_SPELL.get().getDamageSource(this, getOwner()));
            }
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    RawAnimation animationToPlay = null;
    private final AnimationController<EchoingSword> meleeController = new AnimationController<>(this, "keeper_animations", 0, this::predicate);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void playAnimation(String animationId) {
        animationToPlay = RawAnimation.begin().thenPlay(animationId);
    }

    private PlayState predicate(AnimationState<EchoingSword> animationEvent) {
        var controller = animationEvent.getController();

        if (this.animationToPlay != null) {
            controller.forceAnimationReset();
            controller.setAnimation(animationToPlay);
            animationToPlay = null;
        }
        return PlayState.CONTINUE;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(meleeController);
    }
}