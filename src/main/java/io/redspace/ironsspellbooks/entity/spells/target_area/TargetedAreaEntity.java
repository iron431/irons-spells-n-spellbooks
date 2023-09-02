package io.redspace.ironsspellbooks.entity.spells.target_area;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.OwnerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.UUID;

public class TargetedAreaEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(TargetedAreaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(TargetedAreaEntity.class, EntityDataSerializers.INT);


    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    boolean hasOwner;

    private int duration;

    public void setOwner(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
            hasOwner = true;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (cachedOwner != null && cachedOwner.isAlive()) {
            return cachedOwner;
        } else if (ownerUUID != null && level instanceof ServerLevel serverLevel) {
            cachedOwner = serverLevel.getEntity(ownerUUID);
            if (serverLevel.getEntity(ownerUUID) instanceof LivingEntity livingEntity)
                cachedOwner = livingEntity;
            return cachedOwner;
        } else {
            return null;
        }
    }

    public TargetedAreaEntity(EntityType<TargetedAreaEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setRadius(3f);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static TargetedAreaEntity createTargetAreaEntity(Level level, Vec3 center, float radius, int color) {
        TargetedAreaEntity targetedAreaEntity = new TargetedAreaEntity(level, radius, color);
        targetedAreaEntity.setPos(center);
        level.addFreshEntity(targetedAreaEntity);
        return targetedAreaEntity;
    }

    @Override
    public void tick() {
        var owner = getOwner();
        xOld = getX();
        yOld = getY();
        zOld = getZ();
        if (owner != null) {
            setPos(owner.position());
            this.xOld = owner.xOld;
            this.yOld = owner.yOld;
            this.zOld = owner.zOld;
        }
        if (!level.isClientSide
                && (duration > 0 && tickCount > duration
                || duration == 0 && tickCount > 20 * 20
                || (hasOwner && (owner == null || owner.isRemoved())))
        ) {
            discard();
        }
    }

    public TargetedAreaEntity(Level level, float radius, int color) {
        this(EntityRegistry.TARGET_AREA_ENTITY.get(), level);
        this.setRadius(radius);
        this.setColor(color);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.8F);
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_RADIUS, 2F);
        this.getEntityData().define(DATA_COLOR, 0xFFFFFF);
    }

    public void setRadius(float pRadius) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Mth.clamp(pRadius, 0.0F, 32.0F));
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    public void setColor(int color) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_COLOR, color);
        }
    }

    public Vector3f getColor() {
        int color = this.getEntityData().get(DATA_COLOR);
        //Clever color mapping, taken from potionutils get color
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new Vector3f(red / 255.0f, green / 255.0f, blue / 255.0f);

    }

    public int getColorRaw() {
        return this.getEntityData().get(DATA_COLOR);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_RADIUS.equals(pKey)) {
            this.refreshDimensions();
            if (getRadius() < .1f)
                this.discard();
        }
        super.onSyncedDataUpdated(pKey);
    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", this.getRadius());
        tag.putInt("Color", this.getColorRaw());
        tag.putInt("Age", this.tickCount);
        if (duration > 0)
            tag.putInt("Duration", duration);
        if (ownerUUID != null)
            tag.putUUID("Owner", ownerUUID);
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setRadius(tag.getFloat("Radius"));
        this.setColor(tag.getInt("Color"));
        this.tickCount = (tag.getInt("Age"));
        if (tag.contains("Duration")) {
            this.duration = tag.getInt("Duration");
        }
        if (tag.contains("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
            hasOwner = true;
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
