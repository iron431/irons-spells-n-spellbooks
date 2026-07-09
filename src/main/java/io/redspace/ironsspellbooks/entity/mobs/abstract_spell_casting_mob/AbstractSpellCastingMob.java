package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public abstract class AbstractSpellCastingMob extends PathfinderMob implements GeoEntity/*, IMagicEntity */ {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/abstract_casting_mob.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/abstract_casting_mob/abstract_casting_mob.png");
    public static final ResourceLocation animationInstantCast = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/casting_animations.json");
    //private static final EntityDataAccessor<SyncedSpellData> DATA_SPELL = SynchedEntityData.defineId(AbstractSpellCastingMob.class, SyncedSpellData.SYNCED_SPELL_DATA);
    private static final EntityDataAccessor<Boolean> DATA_DRINKING_POTION = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.BOOLEAN);
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(IronsSpellbooks.id("potion_slowdown"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private int drinkTime;
    public boolean hasUsedSingleAttack;

    protected AbstractSpellCastingMob(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        this.lookControl = createLookControl();
    }

    public boolean getHasUsedSingleAttack() {
        return hasUsedSingleAttack;
    }

    public void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
        this.hasUsedSingleAttack = hasUsedSingleAttack;
    }

    //FIXME: 1.21: is #getPassengerRidingPosition the new name for this method?
    //@Override
    //public double getMyRidingOffset() {
    //    return -0.5;
    //}

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof PathfinderMob pathfindermob) {
            pathfindermob.yBodyRot = this.yBodyRot;
        }
    }

    protected LookControl createLookControl() {
        return new LookControl(this) {
            @Override
            protected boolean resetXRotOnTick() {
                return getTarget() == null;
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        //pBuilder.define(DATA_SPELL, new SyncedSpellData(-1));
        pBuilder.define(DATA_DRINKING_POTION, false);
    }

    public boolean isDrinkingPotion() {
        return entityData.get(DATA_DRINKING_POTION);
    }

    protected void setDrinkingPotion(boolean drinkingPotion) {
        this.entityData.set(DATA_DRINKING_POTION, drinkingPotion);
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public void startDrinkingPotion() {
        if (!level.isClientSide) {
            setDrinkingPotion(true);
            drinkTime = 35;
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
            attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
        }
    }

    private void finishDrinkingPotion() {
        setDrinkingPotion(false);
        this.heal(Math.min(Math.max(10, getMaxHealth() / 10), getMaxHealth() / 4));
        this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
        if (!this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("usedSpecial", hasUsedSingleAttack);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        hasUsedSingleAttack = pCompound.getBoolean("usedSpecial");
    }

    public boolean isCasting() {
        return SkillcastingData.get(this).isCasting();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (isDrinkingPotion()) {
            if (drinkTime-- <= 0) {
                finishDrinkingPotion();
            } else if (drinkTime % 4 == 0) {
                if (!this.isSilent()) {
                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, this.getSoundSource(), 1.0F, Utils.random.nextFloat() * 0.1F + 0.9F);
                }
            }
        }


// fixme: full delete? do goals handle this sufficiently?
//        this.forceLookAtTarget(getTarget());

    }

    public void initiateCastSpell(@Nullable AbstractSpellSkill spell, int spellLevel) {
        if (spell == null) {
            return;
        }

        if (level.isClientSide) {
            cancelCastAnimation = false;
            if (spell.getCastType() == CastType.INSTANT) {
                instantCastSpellType = spell;
            }
            return;
        }

        if (getTarget() != null) {
            forceLookAtTarget(getTarget());
        }

        CasterRef casterRef = CasterRef.entity(this);
        var castContext = SkillcastingManager.buildCastContext(casterRef, SkillRegistry.holder(spell), spellLevel, io.redspace.skillcasting.data.CastSource.of(CastSource.MOB.name()), false);
        castContext.set(io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes.IGNORE_MANA, Unit.INSTANCE);
        castContext.set(io.redspace.skillcasting.registry.SkillcastingComponentTypes.IGNORE_COOLDOWN, Unit.INSTANCE);

        if (!spell.checkPreCastConditions(castContext)) {
            return;
        }

        SkillcastingManager.initiateCast(casterRef, castContext);
    }

    public void cancelCast() {
        SkillcastingManager.cancelCast(CasterRef.entity(this), CastEndReason.INTERRUPTED);
    }

    @Nullable
    private AbstractSpellSkill getCastingSpellSkill() {
        ActiveCast activeCast = SkillcastingData.get(this).getActiveCast();
        if (activeCast == null) {
            return null;
        }
        AbstractSkill skill = activeCast.context().skill().value();
        return skill instanceof AbstractSpellSkill spellSkill ? spellSkill : null;
    }

    public void notifyDangerousProjectile(Projectile projectile) {
    }

    public boolean setTeleportLocationBehindTarget(int distance) {
        var target = getTarget();
        boolean valid = false;
//        if (target != null) {
//            var rotation = target.getLookAngle().normalize().scale(-distance);
//            var pos = target.position();
//            var teleportPos = rotation.add(pos);
//
//            for (int i = 0; i < 24; i++) {
//                Vec3 randomness = Utils.getRandomVec3(.15f * i).multiply(1, 0, 1);
//                teleportPos = Utils.moveToRelativeGroundLevel(level, target.position().subtract(new Vec3(0, 0, distance / (float) (i / 7 + 1)).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD)).add(randomness), 5);
//                teleportPos = new Vec3(teleportPos.x, teleportPos.y + .1f, teleportPos.z);
//                var reposBB = this.getBoundingBox().move(teleportPos.subtract(this.position()));
//                //IronsSpellbooks.LOGGER.debug("setTeleportLocationBehindTarget attempt to teleport to {}:", reposBB.getCenter());
//                if (!level.collidesWithSuffocatingBlock(this, reposBB.inflate(-.05f))) {
//                    //IronsSpellbooks.LOGGER.debug("\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\nsetTeleportLocationBehindTarget: {} {} {} empty. teleporting\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\n", reposBB.minX, reposBB.minY, reposBB.minZ);
//                    valid = true;
//                    break;
//                }
//                //IronsSpellbooks.LOGGER.debug("fail");
//
//            }
//            if (valid) {
//                if (Log.SPELL_DEBUG) {
//                    //IronsSpellbooks.LOGGER.debug("ASCM.setTeleportLocationBehindTarget: valid, pos:{}, isClient:{}", teleportPos, level.isClientSide());
//                }
//                playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
//            } else {
//                if (Log.SPELL_DEBUG) {
//                    //IronsSpellbooks.LOGGER.debug("ASCM.setTeleportLocationBehindTarget: invalid, pos:{}, isClient:{}", teleportPos, level.isClientSide());
//                }
//                playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
//
//            }
//        } else {
//            if (Log.SPELL_DEBUG) {
//                //IronsSpellbooks.LOGGER.debug("ASCM.setTeleportLocationBehindTarget: no target, isClient:{}", level.isClientSide());
//            }
//            playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
//        }
        return valid;
    }

    private void forceLookAtTarget(LivingEntity target) {
        if (target != null) {
            double d0 = target.getX() - this.getX();
            double d2 = target.getZ() - this.getZ();
            double d1 = target.getEyeY() - this.getEyeY();

            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            float f1 = (float) (-(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
            this.setXRot(f1 % 360);
            this.setYRot(f % 360);
        }
    }

    /**
     * GeckoLib Animations
     **/
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private AbstractSpellSkill lastCastSpellType;
    @Nullable
    private AbstractSpellSkill instantCastSpellType;
    private boolean cancelCastAnimation = false;
    private boolean animatingLegs = false;
    private final AnimationController animationControllerOtherCast = new AnimationController(this, "other_casting", 0, this::otherCastingPredicate);
    private final AnimationController animationControllerInstantCast = new AnimationController(this, "instant_casting", 0, this::instantCastingPredicate);
    private final AnimationController animationControllerLongCast = new AnimationController(this, "long_casting", 0, this::longCastingPredicate);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void triggerAnim(@org.jetbrains.annotations.Nullable String controllerName, String animName) {
        GeoEntity.super.triggerAnim(controllerName, animName);
    }

    @Override
    public double getBoneResetTime() {
        return 5;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(animationControllerOtherCast);
        controllerRegistrar.add(animationControllerInstantCast);
        controllerRegistrar.add(animationControllerLongCast);
        //controllerRegistrar.add(new AnimationController(this, "idle", 0, this::idlePredicate));
    }

    private PlayState instantCastingPredicate(AnimationState event) {
        if (cancelCastAnimation) {
            return PlayState.STOP;
        }

        var controller = event.getController();
        if (instantCastSpellType != null && controller.getAnimationState() == AnimationController.State.STOPPED) {
            setStartAnimationFromSpell(controller, instantCastSpellType);
            instantCastSpellType = null;
        }
        return PlayState.CONTINUE;
    }

    private PlayState longCastingPredicate(AnimationState event) {
        var controller = event.getController();

        //fixme: mob animations

//        if (cancelCastAnimation || (controller.getAnimationState() == AnimationController.State.STOPPED && !(isCasting() && castingSpell != null && castingSpell.getSpell().getCastType() == CastType.LONG))) {
//            return PlayState.STOP;
//        }

        if (isCasting()) {
            AbstractSpellSkill castingSpell = getCastingSpellSkill();
            if (castingSpell != null && controller.getAnimationState() == AnimationController.State.STOPPED) {
                setStartAnimationFromSpell(controller, castingSpell);
            }
        } else if (lastCastSpellType != null && lastCastSpellType.getCastType() == CastType.LONG) {
            setFinishAnimationFromSpell(controller, lastCastSpellType);
        }

        return PlayState.CONTINUE;
    }

    private PlayState otherCastingPredicate(AnimationState event) {
        if (cancelCastAnimation) {
            return PlayState.STOP;
        }

        var controller = event.getController();
        if (isCasting() && controller.getAnimationState() == AnimationController.State.STOPPED) {
            AbstractSpellSkill castingSpell = getCastingSpellSkill();
            if (castingSpell != null && castingSpell.getCastType() == CastType.CONTINUOUS) {
                setStartAnimationFromSpell(controller, castingSpell);
            }
            return PlayState.CONTINUE;
        }

        if (isCasting()) {
            return PlayState.CONTINUE;
        } else {
            return PlayState.STOP;
        }
    }

    private void setStartAnimationFromSpell(AnimationController controller, AbstractSpellSkill spell) {
        AnimationHolder animation = spell.getCastStartAnimation();
        if (animation.getType() != AnimationHolder.Type.ANIMATION) {
            cancelCastAnimation = true;
            return;
        }
        animation.getAnimationResource().ifPresentOrElse(resourceLocation -> {
            controller.forceAnimationReset();
            controller.setAnimation(RawAnimation.begin().thenPlay(resourceLocation.getPath()));
            lastCastSpellType = spell;
            cancelCastAnimation = false;
            animatingLegs = animation.isAnimatesLegs();
        }, () -> cancelCastAnimation = true);
    }

    private void setFinishAnimationFromSpell(AnimationController controller, AbstractSpellSkill spell) {
        AnimationHolder finishAnimation = spell.getCastFinishAnimation();
        if (finishAnimation.getType() == AnimationHolder.Type.PASS) {
            cancelCastAnimation = false;
            return;
        }
        finishAnimation.getAnimationResource().ifPresentOrElse(resourceLocation -> {
            controller.forceAnimationReset();
            controller.setAnimation(RawAnimation.begin().thenPlay(resourceLocation.getPath()));
            lastCastSpellType = null;
            cancelCastAnimation = false;
        }, () -> cancelCastAnimation = true);
    }

    public boolean isAnimating() {
        return isCasting()
                || (animationControllerLongCast.getAnimationState() == AnimationController.State.RUNNING)
                || (animationControllerOtherCast.getAnimationState() == AnimationController.State.RUNNING)
                || (animationControllerInstantCast.getAnimationState() == AnimationController.State.RUNNING);
    }

    public boolean shouldBeExtraAnimated() {
        return true;
    }

    public boolean shouldAlwaysAnimateHead() {
        return true;
    }

    public boolean shouldAlwaysAnimateLegs() {
        return !animatingLegs;
    }

    public boolean shouldPointArmsWhileCasting() {
        return true;
    }

    public boolean bobBodyWhileWalking() {
        return true;
    }

    public boolean shouldSheathSword() {
        return false;
    }
}
