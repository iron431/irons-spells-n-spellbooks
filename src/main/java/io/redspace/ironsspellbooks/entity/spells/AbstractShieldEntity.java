package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractShieldEntity extends Entity implements AntiMagicSusceptible {
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(AbstractShieldEntity.class, EntityDataSerializers.FLOAT);

    public boolean hurtThisTick;

    public AbstractShieldEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
//        width = 3;
//        height = 3;
//        subEntities = new ShieldPart[width * height];
//        subPositions = new Vec3[width * height];
//        this.setHealth(100);
//        //this.setXRot(45);
//        //this.setYRot(45);
//        LIFETIME = 20 * 20;
//        createShield();

    }

    public AbstractShieldEntity(Level level, float health) {
        this(EntityRegistry.SHIELD_ENTITY.get(), level);
        this.setHealth(health);
    }

    protected abstract void createShield();

//    public void setRotation(float x, float y) {
//        this.setXRot(x);
//        this.xRotO = x;
//        this.setYRot(y);
//        this.yRotO = y;
//    }

    public abstract void takeDamage(DamageSource source, float amount, @Nullable Vec3 location);

    @Override
    public void tick() {
        hurtThisTick = false;
        for (PartEntity<?> subEntity : getParts()) {
            Vec3 pos = subEntity.position();
            subEntity.setPos(pos);
            subEntity.xo = pos.x;
            subEntity.yo = pos.y;
            subEntity.zo = pos.z;
            subEntity.xOld = pos.x;
            subEntity.yOld = pos.y;
            subEntity.zOld = pos.z;
        }
    }

    protected void destroy() {
        kill();
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public abstract PartEntity<?>[] getParts();

    @Override
    public void setId(int id) {
        super.setId(id);
        var subEntities = getParts();
        for (int i = 0; i < subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            subEntities[i].setId(id + i + 1);
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
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        //TODO: fill this out with real info
        return new ClientboundAddEntityPacket(this);
    }

    public List<VoxelShape> getVoxels() {
        List<VoxelShape> voxels = new ArrayList<>();
        for (PartEntity<?> shieldPart : getParts())
            voxels.add(Shapes.create(shieldPart.getBoundingBox()));
        return voxels;
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }
}
