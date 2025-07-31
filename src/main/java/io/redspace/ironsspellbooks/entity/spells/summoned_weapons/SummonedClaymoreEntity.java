package io.redspace.ironsspellbooks.entity.spells.summoned_weapons;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackAnimationData;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.wizards.GenericAnimatedWarlockAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.AnimatedActionGoal;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SummonedClaymoreEntity extends SummonedWeaponEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_TAUNTING = SynchedEntityData.defineId(SummonedClaymoreEntity.class, EntityDataSerializers.BOOLEAN);

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.FLYING_SPEED, 1)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 4)
                .add(Attributes.MOVEMENT_SPEED, .2);

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_IS_TAUNTING, false);
    }

    public boolean isTaunting() {
        return entityData.get(DATA_IS_TAUNTING);
    }

    public void setTaunting(boolean taunting) {
        entityData.set(DATA_IS_TAUNTING, taunting);
    }

    public SummonedClaymoreEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SummonedClaymoreEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.SUMMONED_CLAYMORE.get(), level);
        setSummoner(owner);
    }

    @Override
    public GenericAnimatedWarlockAttackGoal<? extends SummonedWeaponEntity> makeAttackGoal() {
        return new GenericAnimatedWarlockAttackGoal<>/*DefendOwnerWarlockAttackGoal*/(this, 1.5, 20, 40)
                .setMoveset(List.of(
                        AttackAnimationData.builder("summoned_sword_pommel_strike")
                                .length(24).attacks(new AttackKeyframe(12, new Vec3(0, 0, .45f), new Vec3(0, 0, 1))).build(),
                        AttackAnimationData.builder("summoned_sword_basic_downswing")
                                .length(45).attacks(new AttackKeyframe(25, new Vec3(0, -0.2, .15f), new Vec3(0, 0, 1))).area(.7f).build()
                ));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isTaunting()) {
            zza = 0;
            xxa = 0;
            MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, getX(), getY(), getZ(), 3, .1, .1, .1, .2, false);
        }
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isTaunting();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new ClaymoreTauntGoal(this));
        super.registerGoals();
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        if (isTaunting()) {
            return;
        }
        super.move(pType, pPos);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (isTaunting()) {
            pAmount *= .2f;
        }
        return super.hurt(pSource, pAmount);
    }

    public static class ClaymoreTauntGoal extends AnimatedActionGoal<SummonedClaymoreEntity> {

        List<Entity> targets = null;

        public ClaymoreTauntGoal(SummonedClaymoreEntity mob) {
            super(mob);
        }

        @Override
        protected boolean canStartAction() {
            var target = mob.getTarget();
            if (target == null) {
                return false;
            }
            var entities = mob.level.getEntities(mob, mob.getBoundingBox().inflate(12, 6, 12), entity -> entity.getClass().isAssignableFrom(target.getClass()) || entity.isAlliedTo(target));
            if (entities.size() > 2) {
                targets = entities;
                return true;
            }
            return false;
        }

        @Override
        protected int getActionTimestamp() {
            return 20;
        }

        @Override
        protected int getActionDuration() {
            return 20 * 6;
        }

        @Override
        protected int getCooldown() {
            return 100;
        }

        @Override
        protected String getAnimationId() {
            return "claymore_taunt";
        }

        @Override
        public void tick() {
            super.tick();
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.8, 0.0, 0.8).add(0, -1, 0));
        }

        @Override
        protected void doAction() {
            mob.setTaunting(true);
            mob.playSound(SoundRegistry.ECHOING_STRIKE.get(),2,1);
            MagicManager.spawnParticles(mob.level, new BlastwaveParticleOptions(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSchoolType().getTargetingColor(), 3), mob.getX(), mob.getY(), mob.getZ(), 1, 0, 0, 0, 0, true);
            if (targets != null) {
                targets.forEach(entity -> {
                    if (entity instanceof Mob tauntmob) {
                        MagicManager.spawnParticles(mob.level, ParticleTypes.ANGRY_VILLAGER, tauntmob.getX(), tauntmob.getEyeY() + (tauntmob.getBoundingBox().maxY - tauntmob.getEyeY()) * 2, tauntmob.getZ(), 5, 0.3, 0.3, 0.3, 0, false);
                        tauntmob.setTarget(this.mob);
                    }
                });
            }
        }

        @Override
        public void stop() {
            super.stop();
            mob.setTaunting(false);
            targets = null;
        }
    }

//    class DefendOwnerWarlockAttackGoal extends GenericAnimatedWarlockAttackGoal<SummonedClaymoreEntity> {
//        public DefendOwnerWarlockAttackGoal(SummonedClaymoreEntity abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval) {
//            super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
//        }
//
//
//        //fixme: none of this works
//        @Override
//        protected void doMovement(double distanceSquared) {
//            super.doMovement(distanceSquared);
//            var owner = getSummoner();
//            if (owner != null) {
//                Vec3 vectorToTarget = target.position().subtract(mob.position());
//                Vec3 vectorToOwner = owner.position().subtract(mob.position());
//                float f = 1 - (float) Math.abs(vectorToTarget.normalize().dot(vectorToOwner.normalize()));
//                Vec3 unrotated = vectorToOwner.yRot(-mob.getYRot() * Mth.DEG_TO_RAD);
//                mob.getMoveControl().strafe(0, unrotated.x < 0 ? -f : f);
//            }
//        }
//
//        @Override
//        protected double movementSpeed() {
//            float f = 1;
//            var owner = getSummoner();
//            if (owner != null) {
//                float distance = mob.distanceTo(owner);
//                if (distance > 8 * 8) {
//                    f *= Math.clamp(1 - (distance - 64) / 9f, 0, 1);
//                }
//            }
//            return super.movementSpeed() * f;
//        }
//    }
}