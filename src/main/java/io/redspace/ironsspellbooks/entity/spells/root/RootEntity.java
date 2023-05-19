package io.redspace.ironsspellbooks.entity.spells.root;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class RootEntity extends LivingEntity implements IAnimatable, PreventDismount, AntiMagicSusceptible {
    @Nullable
    private LivingEntity owner;

    @Override
    public float getScale() {
        return target == null ? 1 : target.getScale();
    }

    @Nullable
    private UUID ownerUUID;
    private int duration;
    private boolean playSound = true;
    private LivingEntity target;

    public RootEntity(EntityType<? extends RootEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public RootEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.ROOT.get(), level);
        setOwner(owner);
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @Override
    public boolean canCollideWith(@NotNull Entity pEntity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void doPush(@NotNull Entity pEntity) {

    }

    @Override
    public void push(@NotNull Entity pEntity) {

    }

    @Override
    protected void pushEntities() {

    }

    @Override
    public boolean rideableUnderWater() {
        return true;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0d;
    }

    @Override
    public boolean shouldRiderFaceForward(@NotNull Player player) {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        var rooted = getFirstPassenger();

        if (rooted != null) {
            IronsSpellbooks.LOGGER.debug("getDimensions {}", rooted.getBbWidth());
            return EntityDimensions.fixed(rooted.getBbWidth() * 1.25f, .75f);
        }

        return super.getDimensions(pPose);
    }

    @Override
    public void tick() {
        super.tick();
        //IronsSpellbooks.LOGGER.debug("RootEntity.tick {}, {}", getFirstPassenger(), this.level.isClientSide);
        if (playSound) {
            this.refreshDimensions();
            playSound(SoundRegistry.ROOT_EMERGE.get(), 2f, 1);
            playSound = false;
        }

        if (!level.isClientSide) {
            if (tickCount > duration || (target != null && target.isDeadOrDying()) || !isVehicle()) {
                this.removeRoot();
            }
        } else {
            if (tickCount < 20) {
                clientDiggingParticles(this);
            }
        }
    }

    protected void clientDiggingParticles(LivingEntity livingEntity) {
        Random randomsource = livingEntity.getRandom();
        BlockState blockstate = this.getBlockStateOn();
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            for (int i = 0; i < 15; ++i) {
                double d0 = livingEntity.getX() + (double) Mth.randomBetween(randomsource, -0.5F, 0.5F);
                double d1 = livingEntity.getY();
                double d2 = livingEntity.getZ() + (double) Mth.randomBetween(randomsource, -0.5F, 0.5F);
                livingEntity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public void setOwner(@Nullable LivingEntity pOwner) {
        this.owner = pOwner;
        this.ownerUUID = pOwner == null ? null : pOwner.getUUID();
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel) {
            Entity entity = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }

        return this.owner;
    }

    public void removeRoot() {
        if (level.isClientSide) {
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleHelper.ROOT_FOG, getX() + Utils.getRandomScaled(.1f), getY() + Utils.getRandomScaled(.1f), getZ() + Utils.getRandomScaled(.1f), Utils.getRandomScaled(2f), -random.nextFloat() * .5f, Utils.getRandomScaled(2f));
            }
        }
        this.ejectPassengers();
        this.discard();
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Age", this.tickCount);
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
        pCompound.putInt("Duration", duration);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.tickCount = pCompound.getInt("Age");
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
        this.duration = pCompound.getInt("Duration");
    }

    @Override
    public void onAntiMagic(PlayerMagicData playerMagicData) {
        this.removeRoot();
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isDamageSourceBlocked(DamageSource pDamageSource) {
        return true;
    }

    @Override
    public boolean showVehicleHealth() {
        return false;
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {

    }

    @Override
    public void positionRider(Entity passenger) {
        //super.positionRider(pPassenger);
        int x = (int) (this.getX() - passenger.getX());
        int y = (int) (this.getY() - passenger.getY());
        int z = (int) (this.getZ() - passenger.getZ());
        x *= x;
        y *= y;
        z *= z;
        //probably teleported away
        if (x + y + z > 5 * 5)
            this.removeRoot();
        else
            passenger.setPos(this.getX(), this.getY(), this.getZ());

    }

    @Override
    protected boolean isImmobile() {
        return true;
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.isBypassInvul()) {
            this.removeRoot();
            return true;
        }
        return false;
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
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    private boolean played = false;

    private PlayState animationPredicate(AnimationEvent event) {
        var controller = event.getController();

        if (!played && controller.getAnimationState() == AnimationState.Stopped) {
            controller.markNeedsReload();
            controller.setAnimation(ANIMATION);
            played = true;
        }

        return PlayState.CONTINUE;
    }

    private final AnimationBuilder ANIMATION = new AnimationBuilder().playAndHold("emerge");

    private final AnimationController controller = new AnimationController(this, "root_controller", 0, this::animationPredicate);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(controller);
    }

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
