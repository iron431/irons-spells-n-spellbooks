package com.example.testmod.entity.shield;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.entity.ShieldPart;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;

public class ShieldEntity extends Entity {
    protected final ShieldPart[] subEntities;
    protected final Vec3[] subPositions;
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(ShieldEntity.class, EntityDataSerializers.FLOAT);
    protected final int LIFETIME;
    protected int width;
    protected int height;
    protected int age;

    public ShieldEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        width = 4;
        height = 4;
        subEntities = new ShieldPart[width * height];
        subPositions = new Vec3[width * height];
        this.setHealth(100);
        //this.setXRot(45);
        //this.setYRot(45);
        LIFETIME = 20 * 20;
        createShield();

    }

    public ShieldEntity(Level level, float health) {
        this(EntityRegistry.SHIELD_ENTITY.get(), level);
        this.setHealth(health);
    }

    protected void createShield() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = x * height + y;
                subEntities[i] = new ShieldPart(this, "part" + (i + 1), 0.5F, 0.5F);
                subPositions[i] = new Vec3((x - width / 2f) * .5f + .25f, (y - height / 2f) * .5f, 0);//.xRot(getXRot()).yRot(getYRot());
            }
        }
    }

    public void setRotation(float x, float y) {
        this.setXRot(x);
        this.xRotO = x;
        this.setYRot(y);
        this.yRotO = y;
    }

    public void takeDamage(DamageSource source, float amount, @Nullable Vec3 location){
        if (!this.isInvulnerableTo(source)) {
            this.setHealth(this.getHealth() - amount);
        }
        if (!level.isClientSide && location != null)
            MagicManager.spawnParticles(level, ParticleTypes.ELECTRIC_SPARK, location.x,location.y, location.z, 30, .1, .1, .1, .5, false);
    }

    @Override
    public void tick() {
        if (getHealth() <= 0) {
            destroy();
        } else if (++age > LIFETIME) {
            if (!this.level.isClientSide) {
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.NEUTRAL, 1, 1.4f);
            }
            discard();
        } else {
            for (int i = 0; i < subEntities.length; i++) {
                var subEntity = subEntities[i];

                Vec3 pos = subPositions[i].xRot(Mth.DEG_TO_RAD * -this.getXRot()).yRot(Mth.DEG_TO_RAD * -this.getYRot()).add(this.position());
                subEntity.setPos(pos);
                //subEntity.setDeltaMovement(newVector);
                //var vec3 = new Vec3(subEntity.getX(), subEntity.getY(), subEntity.getZ());
                subEntity.xo = pos.x;
                subEntity.yo = pos.y;
                subEntity.zo = pos.z;
                subEntity.xOld = pos.x;
                subEntity.yOld = pos.y;
                subEntity.zOld = pos.z;
            }
        }

    }

    protected void destroy() {
        if (!this.level.isClientSide) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 2, .65f);
        }
        kill();
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.subEntities[i].setId(id + i + 1);
    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH_ID);
    }

    public void setHealth(float pHealth) {
        this.entityData.set(DATA_HEALTH_ID, pHealth);
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_HEALTH_ID, 1.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.contains("Health", 99)) {
            this.setHealth(pCompound.getFloat("Health"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putFloat("Health", this.getHealth());

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        //TODO: fill this out with real info
        return new ClientboundAddEntityPacket(this);
    }
}
