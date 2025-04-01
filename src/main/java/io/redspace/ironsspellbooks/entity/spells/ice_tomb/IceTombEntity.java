package io.redspace.ironsspellbooks.entity.spells.ice_tomb;

import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class IceTombEntity extends Entity implements PreventDismount {
    @Nullable
    private Entity cachedOwner;
    @Nullable
    private UUID ownerUUID;
    /**
     * evil tombs hurt, versus heal
     */
    private boolean evil;
    private float health;

    public IceTombEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public IceTombEntity(Level level, Entity owner) {
        super(EntityRegistry.ICE_TOMB.get(), level);
        setOwner(owner);
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return isPassengerOfSameVehicle(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverlevel) {
            this.cachedOwner = serverlevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    public float getScale() {
        var passengers = getPassengers();
        if (passengers.isEmpty() || !(passengers.getFirst() instanceof LivingEntity livingEntity)) {
            return 1;
        } else {
            return livingEntity.getBbWidth() * 1.7f;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (evil) {
            getPassengers().forEach(this::doNegativeEffects);
        } else {
            if (tickCount % 20 == 0) {
                getPassengers().forEach(this::doPositiveEffects);
            }
        }
        this.applyGravity();
        this.move(MoverType.SELF, getDeltaMovement());
        if (onGround()) {
            this.setDeltaMovement(getDeltaMovement().scale(0.7));
        } else {
            this.setDeltaMovement(getDeltaMovement().multiply(0.95, 1, 0.95));
        }
    }

    @Override
    protected double getDefaultGravity() {
        return LivingEntity.DEFAULT_BASE_GRAVITY;
    }

    public void doPositiveEffects(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            float heal = 2;
            NeoForge.EVENT_BUS.post(new SpellHealEvent(livingEntity, livingEntity, heal, SchoolRegistry.ICE.get()));
            livingEntity.heal(heal);
        }
    }

    public void doNegativeEffects(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (!livingEntity.hasEffect(MobEffectRegistry.CHILLED)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED, 200, 0, false, false, true));
            }
        }
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() * 2, entity.getTicksFrozen() + 10));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level.isClientSide) {
            if (!isInvulnerableTo(source) && (source.getEntity() == null || !isPassengerOfSameVehicle(source.getEntity()))) {
                health -= amount;
                if (health <= 0) {
                    kill();
                }
                return true;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void kill() {
        //todo : damage source and stuff?
        if (evil) {
            getPassengers().forEach(Entity::kill);
        }
        destroyTomb();
    }

    @Override
    public boolean canEntityDismount(Entity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        refreshDimensions();
        passenger.setInvulnerable(true);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        refreshDimensions();
        passenger.setInvulnerable(false);
        destroyTomb();
    }

    public void destroyTomb() {
        if (!level.isClientSide) {
            this.ejectPassengers();
            this.playSound(SoundEvents.GLASS_BREAK, 2, 1);
            MagicManager.spawnParticles(level, ParticleHelper.SNOW_DUST, getX(), getY() + 1, getZ(), 50, 0.2, 0.2, 0.2, 0.2, false);
            MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, getX(), getY() + 1, getZ(), 50, 0.2, 0.2, 0.2, 0.2, false);
            this.discard();
        }
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity pEntity) {
        return this.position();
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction p_19958_) {
        passenger.setPos(this.getX(), this.getY(), this.getZ());
    }


    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }

        compound.putBoolean("evil", this.evil);
        compound.putFloat("health", this.health);
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
            this.cachedOwner = null;
        }
        this.evil = compound.getBoolean("evil");
        this.health = compound.getFloat("health");
    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return super.getDimensions(pPose).scale(getScale());
    }

    @Override
    public boolean canCollideWith(@NotNull Entity pEntity) {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void push(@NotNull Entity pEntity) {

    }
}
