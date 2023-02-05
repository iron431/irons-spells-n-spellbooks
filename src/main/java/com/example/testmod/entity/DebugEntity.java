package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class DebugEntity extends Entity {
    private static final int FAILSAFE_EXPIRE_TIME = 20 * 20;
    private int age;
    private LivingEntity owner;
    private float range;

    public DebugEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public DebugEntity(Level level, LivingEntity owner, float range) {
        super(EntityRegistry.DEBUG.get(), level);
        this.owner = owner;
        this.range = range;
    }

    @Override
    public void tick() {
        super.tick();

        if (++age > FAILSAFE_EXPIRE_TIME) {
            //This exists in case there is any bug with removing the cone onCastComplete
            discard();
        }

        if (owner != null) {
            var startPosition = Utils.getPositionFromEntityLookDirection(owner, 1);
            var endPosition = Utils.getPositionFromEntityLookDirection(owner, range);
            var bb = new AABB(startPosition, endPosition);
            this.setBoundingBox(bb);
            TestMod.LOGGER.debug("DebugEntity: {} {} {}", startPosition, endPosition, bb);
            this.setPos(startPosition);
            this.setXRot(owner.getXRot());
            this.setYRot(owner.getYRot());

            this.level.getEntities((Entity) null, bb, entity -> {
                return true;
            }).forEach(entity -> {
                TestMod.LOGGER.debug("DebugEntity: {}", entity);
            });

            //this.setYHeadRot(owner.getYRot());
            //this.yRotO = getYRot();
            //this.xRotO = getXRot();

//            subEntity.xo = vec3.x;
//            subEntity.yo = vec3.y;
//            subEntity.zo = vec3.z;
//            subEntity.xOld = vec3.x;
//            subEntity.yOld = vec3.y;
//            subEntity.zOld = vec3.z;
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return super.getDimensions(pPose);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
