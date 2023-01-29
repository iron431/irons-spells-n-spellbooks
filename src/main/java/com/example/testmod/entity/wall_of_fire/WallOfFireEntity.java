package com.example.testmod.entity.wall_of_fire;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractShieldEntity;
import com.example.testmod.entity.ShieldPart;
import com.example.testmod.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WallOfFireEntity extends AbstractShieldEntity implements IEntityAdditionalSpawnData {
    protected ShieldPart[] subEntities;
    protected List<Vec3> partPositions = new ArrayList<>();
    protected List<Vec3> anchorPoints = new ArrayList<>();

    private boolean tempCreateWall;
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    public WallOfFireEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        TestMod.LOGGER.debug("WallOfFire.attempting to create sub entities");
        subEntities = new ShieldPart[0];
        if(level.isClientSide)
            createShield();
    }

    @Override
    public void takeDamage(DamageSource source, float amount, @org.jetbrains.annotations.Nullable Vec3 location) {

    }

    public WallOfFireEntity(Level level, Entity owner, List<Vec3> anchors) {
        this(EntityRegistry.WALL_OF_FIRE_ENTITY.get(), level);
        this.anchorPoints = anchors;
        createShield();

        setOwner(owner);
    }

//    public void addAnchor(Vec3 anchor) {
//        anchor = setOnGround(anchor);
//        if (anchorPoints.size() == 0) {
//            anchorPoints.add(anchor);
//
//        } else {
//            int i = anchorPoints.size();
//            float distance = (float) anchorPoints.get(i - 1).distanceTo(anchor);
//            float maxDistance = this.maxTotalDistance - this.accumulatedDistance;
//            if (distance <= maxDistance) {
//                //point fits, continue
//                accumulatedDistance += distance;
//                anchorPoints.add(anchor);
//                //TestMod.LOGGER.debug("WallOfFire: this anchor fits (length {})", distance);
//
//            } else {
//                //too long, clip and cancel spell
//                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
//                anchor = setOnGround(anchor);
//                anchorPoints.add(anchor);
//                createShield();
//
//            }
//
//            //TestMod.LOGGER.debug("WallOfFire.maxDistance: {}", this.maxTotalDistance);
//            //TestMod.LOGGER.debug("WallOfFire.currentDistance: {}", this.accumulatedDistance);
//        }
//    }

//    private Vec3 setOnGround(Vec3 in) {
//        double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) in.x, (int) in.z);
//        if (Math.abs(y - in.y) > 3) {
//            //too great of a gap
//            y = in.y;
//        }
//        return new Vec3(in.x, y, in.z);
//
//    }

    @Override
    public void tick() {
        for (int i = 0, subEntitiesLength = subEntities.length; i < subEntitiesLength; i++) {
            PartEntity<?> subEntity = subEntities[i];
            Vec3 pos = partPositions.get(i);
            subEntity.setPos(pos);
            subEntity.xo = pos.x;
            subEntity.yo = pos.y;
            subEntity.zo = pos.z;
            subEntity.xOld = pos.x;
            subEntity.yOld = pos.y;
            subEntity.zOld = pos.z;
        }
        //TestMod.LOGGER.debug("WallOfFire.getParts (array length: {}),", subEntities.length);
//        if (!level.isClientSide) {
//            for (int i = 0; i < anchorPoints.size(); i++) {
//                var vec = anchorPoints.get(i);
//                MagicManager.spawnParticles(level, ParticleTypes.SMOKE, vec.x, vec.y + .5, vec.z, 30, 0, 1, 0, .01, true);
//
//            }
//            if (tempCreateWall) {
//                for (int i = 0; i < anchorPoints.size() - 1; i++) {
//                    Vec3 start = anchorPoints.get(i);
//                    Vec3 end = anchorPoints.get(i + 1);
//                    Vec3 lookVec = end.subtract(start).scale(.1f);
//                    for (int x = 0; x < 10; x++) {
//                        MagicManager.spawnParticles(level, ParticleTypes.DRAGON_BREATH, start.x + lookVec.x * x, start.y + lookVec.y * x, start.z + lookVec.z * x, 1, 0, 0, 0, 0, true);
//
//                    }
//                }
//            }
//        }

        //TestMod.LOGGER.debug("WallOfFire.tick: sub entities size: {}", subEntities.size());
    }

    @Override
    public void createShield() {
        TestMod.LOGGER.debug("Attempting to create shield, achor points length: {}", anchorPoints.size());
        float height = 3;
        float step = .5f;
        List<ShieldPart> entitiesList = new ArrayList<>();
        TestMod.LOGGER.debug("WallOfFire:Creating shield");
        for (int i = 0; i < anchorPoints.size() - 1; i++) {
            Vec3 start = anchorPoints.get(i);
            Vec3 end = anchorPoints.get(i + 1);
            Vec3 dirVec = end.subtract(start).normalize().scale(step);
            int steps = (int) (start.distanceTo(end) / step);
            for (int x = 0; x < steps; x++) {
                //MagicManager.spawnParticles(level, ParticleTypes.DRAGON_BREATH, start.x + dirVec.x * x, start.y + dirVec.y * x, start.z + dirVec.z * x, 1, 0, 0, 0, 0, true);
                ShieldPart part = new ShieldPart(this, "part" + i * steps + x, step, height);
                Vec3 pos = new Vec3(start.x + dirVec.x * x, start.y + dirVec.y * x, start.z + dirVec.z * x);
                partPositions.add(pos);
                TestMod.LOGGER.debug("WallOfFire:Creating shield: new sub entity {}", pos);
                entitiesList.add(part);
            }

        }
        //subEntities = new ShieldPart[entitiesList.size()];
        subEntities = entitiesList.toArray(subEntities);
        TestMod.LOGGER.debug("WallOfFire.createShield (array length: {}, real length: {}),", subEntities.length, entitiesList.size());

//        Entity owner = getOwner();
//        if (owner instanceof ServerPlayer serverPlayer) {
//            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
//            playerMagicData.forgetCastingEntity();
//            if (playerMagicData.isCasting())
//                ServerboundCancelCast.cancelCast(serverPlayer, playerMagicData.getCastSource() != CastSource.Scroll);
//        }

    }


    public void setOwner(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.ownerUUID = pOwner.getUUID();
            this.cachedOwner = pOwner;
        }

    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel) this.level).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public PartEntity<?>[] getParts() {
        //var x = new PartEntity[subEntities.size()];
        //this.subEntities.toArray(x);
        //TestMod.LOGGER.debug("WallOfFire.getParts (array length: {}),", subEntities.length);
        return subEntities;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
        super.readAdditionalSaveData(pCompound);

    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        TestMod.LOGGER.debug("WallOfFire.writeSpawnData");
        buffer.writeInt(anchorPoints.size());
        for (Vec3 vec : anchorPoints) {
            buffer.writeFloat((float) vec.x);
            buffer.writeFloat((float) vec.y);
            buffer.writeFloat((float) vec.z);
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        TestMod.LOGGER.debug("WallOfFire.readSpawnData");

        anchorPoints = new ArrayList<>();
        int length = additionalData.readInt();
        for (int i = 0; i < length; i++) {
            anchorPoints.add(new Vec3(additionalData.readFloat(), additionalData.readFloat(), additionalData.readFloat()));
        }
        //createShield();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        //TODO: fill this out with real info
        return new ClientboundAddEntityPacket(this);
    }
}
