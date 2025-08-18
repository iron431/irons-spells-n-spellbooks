package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.fire_orb;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.effect.ImmolateEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.ICritablePartEntity;
import io.redspace.ironsspellbooks.network.particles.FieryExplosionParticlesPacket;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.UUID;

public class OminousFireOrbEntity extends Entity implements AntiMagicSusceptible, ICritablePartEntity {
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(OminousFireOrbEntity.class, EntityDataSerializers.INT);
    @javax.annotation.Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    float health = 100;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    float damage;
    float radius;

    public int getFuse() {
        return entityData.get(DATA_FUSE);
    }

    public void setFuse(int fuse) {
        entityData.set(DATA_FUSE, fuse);
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

//    @Override
//    public boolean isCurrentlyGlowing() {
//        return true;
//    }
//
//    @Override
//    public int getTeamColor() {
//        if (getTeam() == null) {
//            return 0xf2552e; // cinderous rarity color
//        }
//        return super.getTeamColor();
//    }

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
    //    float maxHealth;

    public OminousFireOrbEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public OminousFireOrbEntity(Level level) {
        this(EntityRegistry.OMINOUS_FIRE_ORB.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE, -1);
    }

    @Override
    public void tick() {
        super.tick();
        travel();
        if (level.isClientSide) {
            Vec3 motion = Utils.getRandomVec3(0.08);
            Vec3 movement = getDeltaMovement();
            level.addParticle(ParticleHelper.FIRE, getX() - movement.x, getY() + 2 - movement.y, getZ() - movement.z, motion.x, motion.y, motion.z);
        }
        if (getFuse() >= 0) {
            // sounds
            if (tickCount % 8 == 0) {
                float pitch = Mth.lerp(tickCount / (float) getFuse(), 0.5f, 2f);
                this.playSound(SoundRegistry.SCORCH_PREPARE.get(), 2 + pitch, pitch);
            }
            if (tickCount == getFuse() - 20) {
                this.playSound(SoundRegistry.HEAT_SURGE_PREPARE.get(), 4, 1);
            }
            if (!level.isClientSide && tickCount >= getFuse()) {
                doExplosion();
            }
        }
    }

    private void doExplosion() {
        Entity owner = getOwner();
        var source = new DamageSource(level.damageSources().damageTypes.getHolderOrThrow(ISSDamageTypes.FIRE_MAGIC), owner);
        var explosionRadiusSqr = radius * radius;
        var entities = level.getEntities(this, this.getBoundingBox().inflate(radius));
        Vec3 losPoint = Utils.raycastForBlock(level, this.position(), this.position().add(0, 1, 0), ClipContext.Fluid.NONE).getLocation();
        float baseDamage = this.damage;
        for (Entity entity : entities) {
            double distanceSqr = entity.distanceToSqr(this.position());
            if (!(entity instanceof OminousFireOrbEntity) && distanceSqr < explosionRadiusSqr && entity.canBeHitByProjectile() && !DamageSources.isFriendlyFireBetween(owner, entity) && Utils.hasLineOfSight(level, losPoint, entity.getBoundingBox().getCenter(), true)) {
                double p = (1 - distanceSqr / explosionRadiusSqr);
                float damage = (float) (baseDamage * p);
                if (entity.hurt(source, damage) && entity instanceof LivingEntity livingVictim) {
                    ImmolateEffect.addImmolateStack(livingVictim, owner);
                }
            }
        }
        PacketDistributor.sendToPlayersTrackingEntity(this, new FieryExplosionParticlesPacket(this.getBoundingBox().getCenter(), radius));
        CameraShakeManager.addCameraShake(new CameraShakeData(20 + (int) radius / 3, this.position(), this.radius + 15));
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), this.getSoundSource(), 4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
        discard();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public void travel() {
        move(MoverType.SELF, getDeltaMovement());
        Vec3 motion = this.getDeltaMovement();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(motion.x, motion.y - getDefaultGravity(), motion.z);
        }
        if (onGround()) {
            setDeltaMovement(motion.scale(getBlockStateOn().getFriction(level, this.blockPosition(), this)));
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return super.isAlliedTo(entity) || entity.getType().is(ModTags.INFERNAL_ALLIES);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level.isClientSide || this.isInvulnerableTo(source) || DamageSources.isFriendlyFireBetween(source.getEntity(), this)) {
            return false;
        } else {
            this.markHurt();
            this.health -= amount;
            MagicManager.spawnParticles(level, ParticleTypes.LAVA, this.getX(), this.getY() + 2, this.getZ(), (int) amount, 0.1, 0.1, 0.1, 0.5, false);
            playSound(SoundRegistry.KEEPER_HURT.get(), 1.5f, 1.7f);
            if (health <= 0) {
                //todo:death sound
                discard();
                MagicManager.spawnParticles(level, ParticleHelper.FIERY_SPARKS, getX(), getY() + 2, getZ(), 25, 0, 0, 0, 0.5, true);
            }
            return true;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.health = compound.getFloat("Health");
        this.damage = compound.getFloat("Damage");
        this.radius = compound.getFloat("Radius");
        this.tickCount = compound.getInt("Age");
        if (compound.contains("Fuse")) {
            setFuse(compound.getInt("Fuse"));
        }
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
            this.cachedOwner = null;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Health", this.health);
        compound.putFloat("Damage", this.damage);
        compound.putFloat("Radius", this.radius);
        compound.putInt("Age", this.tickCount);
        if (getFuse() >= 0) {
            compound.putInt("Fuse", getFuse());
        }
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        hurt(DamageSources.get(level, DamageTypes.MAGIC), 10);
    }
}
