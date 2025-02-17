package io.redspace.ironsspellbooks.entity.spells.ice_block;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IceBlockProjectile extends AbstractMagicProjectile implements GeoEntity, IEntityWithComplexSpawn {

    private UUID targetUUID;
    private Entity cachedTarget;
    private List<Entity> victims;

    public IceBlockProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        victims = new ArrayList<>();

        this.setNoGravity(true);
    }

    public IceBlockProjectile(Level pLevel, LivingEntity owner, LivingEntity target) {
        this(EntityRegistry.ICE_BLOCK_PROJECTILE.get(), pLevel);
        this.setOwner(owner);
        this.setTarget(target);
    }

    int airTime;

    public void setAirTime(int airTimeInTicks) {
        airTime = airTimeInTicks;
    }

    public void setTarget(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.targetUUID = pOwner.getUUID();
            this.cachedTarget = pOwner;
        }

    }

    @Nullable
    public Entity getTarget() {
        if (this.cachedTarget != null && !this.cachedTarget.isRemoved()) {
            return this.cachedTarget;
        } else if (this.targetUUID != null && this.level instanceof ServerLevel) {
            this.cachedTarget = ((ServerLevel) this.level).getEntity(this.targetUUID);
            return this.cachedTarget;
        } else {
            return null;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetUUID != null) {
            tag.putUUID("Target", this.targetUUID);
        }
        if (this.airTime > 0) {
            tag.putInt("airTime", airTime);
        }
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) {
            this.targetUUID = tag.getUUID("Target");
        }
        if (tag.contains("airTime")) {
            this.airTime = tag.getInt("airTime");
        }
    }

    @Override
    public void trailParticles() {
        for (int i = 0; i < 1; i++) {
            Vec3 random = new Vec3(
                    Utils.getRandomScaled(this.getBbWidth() * .5f),
                    0,
                    Utils.getRandomScaled(this.getBbWidth() * .5f)
            );
            level.addParticle(ParticleTypes.SNOWFLAKE, getX() + random.x, getY(), getZ() + random.z, 0, -.05, 0);
        }
    }

    private void doFallingDamage(Entity target) {
        if (level.isClientSide)
            return;
        if (!canHitEntity(target) || victims.contains(target))
            return;
        boolean flag = DamageSources.applyDamage(target, getDamage() / 2, SpellRegistry.ICE_BLOCK_SPELL.get().getDamageSource(this, getOwner()));
        if (flag) {
            victims.add(target);
        }
        //Ironsspellbooks.logger.debug("IceBlockProjectile.doFallingDamage: {}", target.getName().getString());

    }

    private void doImpactDamage() {
        float explosionRadius = 3.5f;
        level.getEntities(this, this.getBoundingBox().inflate(explosionRadius)).forEach((entity) -> {
            if (canHitEntity(entity)) {
                double distance = entity.distanceToSqr(position());
                if (distance < explosionRadius * explosionRadius) {
                    double p = (1 - Math.pow(Math.sqrt(distance) / (explosionRadius), 3));
                    float damage = (float) (this.damage * p);
                    //Ironsspellbooks.logger.debug("IceBlockProjectile.doImpactDamage distance: {} p: {}", Math.sqrt(distance), p);

                    DamageSources.applyDamage(entity, damage, SpellRegistry.ICE_BLOCK_SPELL.get().getDamageSource(this, getOwner()));
                }
            }

        });
    }

    @Override
    public void tick() {
        this.firstTick = false;
        if (airTime-- > 0) {
            handleFloating();
        } else {
            handleFalling();
        }
        //handle target tracking
        var target = getTarget();
        if (target != null) {
            Vec3 diff = target.position().subtract(this.position());
            var distance = diff.horizontalDistanceSqr();
            var factor = Math.clamp(distance / 16.0, 0, 1);
            if (diff.horizontalDistanceSqr() > 0.1) {
                this.setDeltaMovement(getDeltaMovement().add(diff.multiply(1, 0, 1).normalize().scale(.025f * ((airTime <= 0 ? 2 : 1) + factor * 2))));
            }
        }
        if (noPhysics) {
            this.noPhysics = level.noBlockCollision(this, this.getBoundingBox());
        }

        move(MoverType.SELF, getDeltaMovement());
    }

    private void handleFloating() {
        boolean tooHigh = false;
        this.setDeltaMovement(getDeltaMovement().multiply(.95f, .75f, .95f));
        // target synced to client + server
        var target = getTarget();
        if (target != null) {
            if (this.getY() - target.getY() > 3.5) {
                tooHigh = true;
            }
        } else {
            if (airTime % 3 == 0) {
                HitResult ground = Utils.raycastForBlock(level, position(), position().subtract(0, 3.5, 0), ClipContext.Fluid.ANY);
                if (ground.getType() == HitResult.Type.MISS) {
                    tooHigh = true;
                }
            }
        }
        // adjust tracking to ground (loosely)
        if (tooHigh) {
            this.setDeltaMovement(getDeltaMovement().add(0, -.005, 0));
        } else {
            this.setDeltaMovement(getDeltaMovement().add(0, .01, 0));
        }
        if (airTime == 0) {
            // pop upwards on fall trigger
            this.setDeltaMovement(0, 0.5, 0);
        }
    }

    private void handleFalling() {
        this.setDeltaMovement(getDeltaMovement().add(0, -.15, 0));
        //server logic only
        if (!level.isClientSide) {
            if (onGround()) {
                doImpactDamage();
                playSound(SoundRegistry.ICE_BLOCK_IMPACT.get(), 2.5f, .8f + random.nextFloat() * .4f);
                impactParticles(getX(), getY(), getZ());
                discard();
            } else {
                level.getEntities(this, getBoundingBox().inflate(0.35)).forEach(this::doFallingDamage);
            }
        }
    }

    @Override
    public void setXRot(float pXRot) {
//        IronsSpellbooks.LOGGER.debug("IceBlockProjectile: Something is trying to set my x rot! Ignoring.");
        //super.setXRot(pXRot);
    }

    @Override
    public void setYRot(float pYRot) {
//        super.setYRot(pYRot);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return pTarget != getOwner() && super.canHitEntity(pTarget);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleTypes.SNOWFLAKE, x, y, z, 50, .8, .1, .8, 0.2, false);
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, x, y, z, 25, .5, .1, .5, 0.3, false);
    }

    @Override
    public float getSpeed() {
        //unused
        return 0;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.airTime);
        buffer.writeInt(cachedTarget == null ? -1 : cachedTarget.getId());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        this.airTime = additionalData.readInt();
        int id = additionalData.readInt();
        if (id >= 0) {
            this.setTarget(level.getEntity(id));
        }
    }
}
