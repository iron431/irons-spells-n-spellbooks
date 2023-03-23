package io.redspace.ironsspellbooks.entity.spectral_hammer;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import software.bernie.geckolib3.core.AnimationState;
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

public class SpectralHammer extends LivingEntity implements IAnimatable {

    @Nullable
    private UUID ownerUUID;

    @Nullable
    private Entity cachedOwner;

    private float damageAmount;
    private boolean playSwingAnimation = true;

    public SpectralHammer(EntityType<? extends SpectralHammer> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public SpectralHammer(Level levelIn, LivingEntity owner, BlockHitResult blockHitResult, float damageAmount) {
        this(EntityRegistry.SPECTRAL_HAMMER.get(), levelIn);
        this.damageAmount = damageAmount;

        setOwner(owner);

        var xRot = owner.getXRot();
        var yRot = owner.getYRot();
        var yHeadRot = owner.getYHeadRot();

        this.setYRot(yRot);
        this.setXRot(xRot);
        this.setYBodyRot(yRot);
        this.setYHeadRot(yHeadRot);

        IronsSpellbooks.LOGGER.debug("SpectralHammer: owner - xRot:{}, yRot:{}, yHeadRot:{}", xRot, yRot, yHeadRot);
        IronsSpellbooks.LOGGER.debug("SpectralHammer: this - xRot:{}, yRot:{}, look:{}", this.getXRot(), this.getYRot(), this.getLookAngle());
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            //spawnParticles();
        } else {
            if (!playSwingAnimation && animationController.getAnimationState() == AnimationState.Stopped) {
                this.discard();
            }
            //this.playSound(SpectralHammerSpell.getImpactSound(), 1.0f, 1.0f);
        }
    }



    public void setOwner(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height * 0.6F;
    }


    @Override
    public boolean isNoGravity() {
        return true;
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

    //https://forge.gemwire.uk/wiki/Particles
    public void spawnParticles() {
        for (int i = 0; i < 2; i++) {
            double speed = .02;
            double dx = level.random.nextDouble() * 2 * speed - speed;
            double dy = level.random.nextDouble() * 2 * speed - speed;
            double dz = level.random.nextDouble() * 2 * speed - speed;
            var tmp = ParticleHelper.UNSTABLE_ENDER;
            IronsSpellbooks.LOGGER.debug("WispEntity.spawnParticles isClientSide:{}, position:{}, {} {} {}", this.level.isClientSide, this.position(), dx, dy, dz);
            level.addParticle(ParticleHelper.WISP, this.xOld - dx, this.position().y + .3, this.zOld - dz, dx, dy, dz);
            //level.addParticle(ParticleHelper.UNSTABLE_ENDER, this.getX() + dx / 2, this.getY() + dy / 2, this.getZ() + dz / 2, dx, dy, dz);
        }
    }

    @SuppressWarnings("removal")
    private final AnimationFactory factory = new AnimationFactory(this);

    @SuppressWarnings("removal")
    private final AnimationBuilder animationBuilder = new AnimationBuilder().addAnimation("hammer_swing", false);
    private final AnimationController animationController = new AnimationController(this, "controller", 0, this::predicate);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationController);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        if (event.getController().getAnimationState() == AnimationState.Stopped) {
            if (playSwingAnimation) {
                event.getController().setAnimation(animationBuilder);
                playSwingAnimation = false;
            }
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
