package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IAnimatedCastingMob;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.component.CastComponentMap;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.level.Level;
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

public abstract class AbstractSpellCastingMob extends PathfinderMob implements GeoEntity, IAnimatedCastingMob, IDrinkPotions {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/abstract_casting_mob.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/abstract_casting_mob/abstract_casting_mob.png");
    public static final ResourceLocation animationInstantCast = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/casting_animations.json");
    private static final EntityDataAccessor<Boolean> DATA_DRINKING_POTION = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.BOOLEAN);
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(IronsSpellbooks.id("potion_slowdown"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private int drinkTime;
    public long singleAttackCooldownTimestamp;

    public ResourceLocation getCurrentAnimationFile() {
        return currentAnimationFile;
    }

    private ResourceLocation currentAnimationFile = animationInstantCast;

    protected AbstractSpellCastingMob(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        this.lookControl = createLookControl();
    }

    public boolean canUseSingleAttack() {
        return level.getGameTime() >= singleAttackCooldownTimestamp;
    }

    public void setSingleAttackCooldown() {
        setSingleAttackCooldown(5L * 20 * 60);
    }

    public void setSingleAttackCooldown(long delay) {
        this.singleAttackCooldownTimestamp = level.getGameTime() + delay;
    }

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
        pBuilder.define(DATA_DRINKING_POTION, false);
    }

    @Override
    public boolean isDrinkingPotion() {
        return entityData.get(DATA_DRINKING_POTION);
    }

    @Override
    public void setDrinkingPotion(boolean drinkingPotion) {
        this.entityData.set(DATA_DRINKING_POTION, drinkingPotion);
    }

    @Override
    public int getDrinkingTime() {
        return drinkTime;
    }

    @Override
    public void setDrinkingTime(int drinkingTime) {
        this.drinkTime = drinkingTime;
    }

    @Override
    public void startDrinkingPotion() {
        if (!level.isClientSide) {
            setDrinkingPotion(true);
            drinkTime = 35;
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
            attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
        }
    }

    @Override
    public void finishDrinkingPotion() {
        setDrinkingPotion(false);
        this.heal(Math.min(Math.max(10, getMaxHealth() / 10), getMaxHealth() / 4));
        this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
        if (!this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
    }

    @Override
    public @NotNull SoundEvent getPotionDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (!canUseSingleAttack()) {
            pCompound.putLong("usedSpecial", singleAttackCooldownTimestamp);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("usedSpecial")) {
            singleAttackCooldownTimestamp = pCompound.getLong("usedSpecial");
        }
    }

    public boolean isCasting() {
        return SkillcastingData.get(this).isCasting();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        handlePotionTick(this);


// fixme: full delete? do goals handle this sufficiently?
//        this.forceLookAtTarget(getTarget());

    }

    @Deprecated
    public boolean attemptInitiateCastSpell(AbstractSpell spell, int spellLevel, @Nullable CastComponentMap componentPatch) {
        return SkillcastingUtils.attemptInitiateMobCast(this, SkillRegistry.holder(spell), spellLevel, componentPatch);
    }

    public void cancelCast() {
        SkillcastingManager.cancelCast(CasterRef.entity(this), CastEndReason.INTERRUPTED);
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

    private boolean animatingLegs = false;

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getBoneResetTime() {
        return 5;
    }

    @Override
    public void playCastingAnimation(AnimationHolder animation) {
        if (animation.getType() != AnimationHolder.Type.PASS) {
            this.queuedCastingAnimation = animation;
            this.animatingLegs = animation.isAnimatesLegs();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(castingController);
    }

    AnimationHolder queuedCastingAnimation = null;
    private final AnimationController<AbstractSpellCastingMob> castingController = new AnimationController<>(this, "casting_controller", 0, this::castingAnimationPredicate);

    private PlayState castingAnimationPredicate(AnimationState<AbstractSpellCastingMob> event) {
        var controller = event.getController();
        if (this.queuedCastingAnimation != null) {
            if (this.queuedCastingAnimation.getAnimation().isPresent() && this.queuedCastingAnimation.getGeoFileResource().isPresent()) {
                controller.forceAnimationReset();
                this.currentAnimationFile = queuedCastingAnimation.getGeoFileResource().get();
                controller.setAnimation(RawAnimation.begin().thenPlay(queuedCastingAnimation.getAnimation().get().getPath()));
            } else {
                controller.stop();
            }
            queuedCastingAnimation = null;
        }
        return controller.getAnimationState() == AnimationController.State.STOPPED ? PlayState.STOP : PlayState.CONTINUE;
    }

    public boolean isAnimating() {
        return isCasting() || (castingController.getAnimationState() == AnimationController.State.RUNNING);
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

    public boolean bobBodyWhileWalking() {
        return true;
    }

    public boolean shouldSheathSword() {
        return false;
    }
}
