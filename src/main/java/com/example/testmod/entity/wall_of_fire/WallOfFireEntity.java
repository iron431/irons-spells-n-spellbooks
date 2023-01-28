package com.example.testmod.entity.wall_of_fire;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.AbstractShieldEntity;
import com.example.testmod.entity.ShieldPart;
import com.example.testmod.network.ServerboundCancelCast;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.CastSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WallOfFireEntity extends AbstractShieldEntity {
    //protected static final EntityDataAccessor<CompoundTag> DATA_ANCHOR_POINTS = SynchedEntityData.defineId(WallOfFireEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> DATA_CLIENT_CREATE_SHIELD_FLAG = SynchedEntityData.defineId(WallOfFireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> DATA_ANCHOR_POINTS = SynchedEntityData.defineId(WallOfFireEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private List<Vec3> anchorPoints = new ArrayList<>();
    protected ShieldPart[] subEntities;
    protected List<Vec3> partPositions = new ArrayList<>();

    private boolean tempCreateWall;
    private int maxTotalDistance = 10;
    private float accumulatedDistance;
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

//    boolean flagClientCreateShield;
//    boolean flagServerCreateShield;

    public WallOfFireEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        subEntities = new ShieldPart[0];
    }

    @Override
    public void takeDamage(DamageSource source, float amount, @org.jetbrains.annotations.Nullable Vec3 location) {

    }

    public WallOfFireEntity(Level level, Entity owner) {
        this(EntityRegistry.WALL_OF_FIRE_ENTITY.get(), level);
        setOwner(owner);
    }

    public void addAnchor(Vec3 anchor) {
        anchor = setOnGround(anchor);
        if (anchorPoints.size() == 0) {
            anchorPoints.add(anchor);

        } else {
            int i = anchorPoints.size();
            float distance = (float) anchorPoints.get(i - 1).distanceTo(anchor);
            float maxDistance = this.maxTotalDistance - this.accumulatedDistance;
            if (distance <= maxDistance) {
                //point fits, continue
                accumulatedDistance += distance;
                anchorPoints.add(anchor);
                //TestMod.LOGGER.debug("WallOfFire: this anchor fits (length {})", distance);

            } else {
                //too long, clip and cancel spell
                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
                anchor = setOnGround(anchor);
                anchorPoints.add(anchor);
                createShield();

            }
            CompoundTag tag = this.entityData.get(DATA_ANCHOR_POINTS);
            ListTag anchors = tag.getList("Anchors", 9);
            CompoundTag newAnchor = new CompoundTag();
            newAnchor.putDouble("x", anchor.x);
            newAnchor.putDouble("y", anchor.y);
            newAnchor.putDouble("z", anchor.z);
            anchors.add(newAnchor);
            tag.put("Anchors", anchors);
            this.entityData.set(DATA_ANCHOR_POINTS, tag);
            //TestMod.LOGGER.debug("WallOfFire.maxDistance: {}", this.maxTotalDistance);
            //TestMod.LOGGER.debug("WallOfFire.currentDistance: {}", this.accumulatedDistance);
        }
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_ANCHOR_POINTS.equals(pKey)) {

            anchorPoints = readAnchorPointsFromNBT(entityData.get(DATA_ANCHOR_POINTS));
            TestMod.LOGGER.debug("WallOfFire.doing expensive onSyncedDataUpdated stuff");
        }

        super.onSyncedDataUpdated(pKey);
    }

    private List<Vec3> readAnchorPointsFromNBT(CompoundTag tag) {
        List<Vec3> out = new ArrayList<>();
        ListTag anchors = tag.getList("Anchors", 9);
        int length = anchors.size();
        for (int i = 0; i < length; i++) {
            CompoundTag anchor = anchors.getCompound(i);
            out.add(new Vec3(anchor.getDouble("x"), anchor.getDouble("y"), anchor.getDouble("z")));
        }
        return out;
    }

    private Vec3 setOnGround(Vec3 in) {
        double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) in.x, (int) in.z);
        if (Math.abs(y - in.y) > 3) {
            //too great of a gap
            y = in.y;
        }
        return new Vec3(in.x, y, in.z);

    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            if (this.entityData.get(DATA_CLIENT_CREATE_SHIELD_FLAG) && anchorPoints.size() > 0) {
                TestMod.LOGGER.debug("WallOfFireEntity.tick.clientCreateShield");
                createShield();
                this.entityData.set(DATA_CLIENT_CREATE_SHIELD_FLAG, false);

            }
        }
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
        TestMod.LOGGER.debug("WallOfFire.getParts (array length: {}),", subEntities.length);
        if (!level.isClientSide) {
            for (int i = 0; i < anchorPoints.size(); i++) {
                var vec = anchorPoints.get(i);
                MagicManager.spawnParticles(level, ParticleTypes.SMOKE, vec.x, vec.y + .5, vec.z, 30, 0, 1, 0, .01, true);

            }
            if (tempCreateWall) {
                for (int i = 0; i < anchorPoints.size() - 1; i++) {
                    Vec3 start = anchorPoints.get(i);
                    Vec3 end = anchorPoints.get(i + 1);
                    Vec3 lookVec = end.subtract(start).scale(.1f);
                    for (int x = 0; x < 10; x++) {
                        MagicManager.spawnParticles(level, ParticleTypes.DRAGON_BREATH, start.x + lookVec.x * x, start.y + lookVec.y * x, start.z + lookVec.z * x, 1, 0, 0, 0, 0, true);

                    }
                }
            }
        }

        //TestMod.LOGGER.debug("WallOfFire.tick: sub entities size: {}", subEntities.size());
    }

    @Override
    public void createShield() {
        tempCreateWall = true;

        partPositions.clear();
        clearSubEntities();

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

        Entity owner = getOwner();
        if (owner instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            playerMagicData.forgetCastingEntity();
            if (playerMagicData.isCasting())
                ServerboundCancelCast.cancelCast(serverPlayer, playerMagicData.getCastSource() != CastSource.Scroll);
        }
        if (!level.isClientSide)
            this.entityData.set(DATA_CLIENT_CREATE_SHIELD_FLAG, true);

    }

    private void clearSubEntities() {
        for (ShieldPart subEntity : subEntities) {
            subEntity.discard();
        }
        subEntities = new ShieldPart[0];
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
        //TODO: serialize anchor points
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_CLIENT_CREATE_SHIELD_FLAG, false);
        entityData.define(DATA_ANCHOR_POINTS, new CompoundTag());
    }
}
