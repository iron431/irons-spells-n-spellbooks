package io.redspace.ironsspellbooks.entity.spells.target_area;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.UUID;

public class TargetedAreaEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(TargetedAreaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(TargetedAreaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FADING = SynchedEntityData.defineId(TargetedAreaEntity.class, EntityDataSerializers.BOOLEAN);


    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    boolean hasOwner;
    boolean shouldFade;

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

    public static TargetedAreaEntity createTargetAreaEntity(Level level, Vec3 center, float radius, int color, Entity owner) {
        TargetedAreaEntity targetedAreaEntity = new TargetedAreaEntity(level, radius, color);
        targetedAreaEntity.setPos(center);
        targetedAreaEntity.setOwner(owner);
        level.addFreshEntity(targetedAreaEntity);
        return targetedAreaEntity;
    }

    @Override
    public void tick() {
        this.firstTick = false;
        var owner = getOwner();
        if (owner != null) {
            setPos(owner.position());
            this.xOld = owner.xOld;
            this.yOld = owner.yOld;
            this.zOld = owner.zOld;
            this.xo = owner.xo;
            this.yo = owner.yo;
            this.zo = owner.zo;
        }
        if (shouldFade && this.tickCount >= duration - 10) {
            this.entityData.set(DATA_FADING, true);
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
        this.getEntityData().define(DATA_FADING, false);
    }

    public boolean isFading() {
        return this.entityData.get(DATA_FADING);
    }

    public void setRadius(float pRadius) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Mth.clamp(pRadius, 0.0F, 32.0F));
        }
    }

    public void setShouldFade(boolean shouldFade) {
        this.shouldFade = shouldFade;
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
        return Utils.deconstructRGB(this.getEntityData().get(DATA_COLOR));
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
        tag.putBoolean("ShouldFade", this.shouldFade);
        if (duration > 0)
            tag.putInt("Duration", duration);
        if (ownerUUID != null)
            tag.putUUID("Owner", ownerUUID);
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setRadius(tag.getFloat("Radius"));
        this.setColor(tag.getInt("Color"));
        this.tickCount = (tag.getInt("Age"));
        this.shouldFade = (tag.getBoolean("ShouldFade"));
        if (tag.contains("Duration")) {
            this.duration = tag.getInt("Duration");
        }
        if (tag.contains("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
            hasOwner = true;
        }
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
    }

    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        Entity entity = this.level.getEntity(pPacket.getData());
        if (entity != null) {
            this.setOwner(entity);
        }

    }
}
