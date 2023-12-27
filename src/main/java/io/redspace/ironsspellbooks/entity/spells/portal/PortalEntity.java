package io.redspace.ironsspellbooks.entity.spells.portal;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * /kill @e[type=irons_spellbooks:portal]
 */

public class PortalEntity extends Entity implements AntiMagicSusceptible {
    static {
        DATA_ID_OWNER_UUID = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    }

    private static final EntityDataAccessor<Optional<UUID>> DATA_ID_OWNER_UUID;
    private static final int collisionCheckTicks = 1;

    private long ticksToLive = 50000;

    public PortalEntity(Level level, PortalData portalData) {
        this(EntityRegistry.PORTAL.get(), level);
        PortalManager.INSTANCE.addPortalData(uuid, portalData);
        this.ticksToLive = (portalData.expiresOnGameTick - level.getGameTime());
    }

    public PortalEntity(EntityType<PortalEntity> portalEntityEntityType, Level level) {
        super(portalEntityEntityType, level);
    }

//    public UUID getOwnerUUID() {
//        return ownerUUID;
//    }
//
//    public LivingEntity getOwner() {
//        if (owner == null && ownerUUID != null) {
//            owner = level.getPlayerByUUID(ownerUUID);
//        }
//
//        return owner;
//    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        PortalManager.INSTANCE.handleAntiMagic(this, magicData);
    }

    public void checkForEntitiesToTeleport() {
        if (this.level.isClientSide) return;

        level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), (entity -> !entity.getType().is(ModTags.CANT_USE_PORTAL))).forEach(entity -> {
            //TODO: remove extraneous logging
            IronsSpellbooks.LOGGER.debug("PortalEntity: entity near portal:{}", entity);

            PortalManager.INSTANCE.processDelayCooldown(uuid, entity.getUUID(), collisionCheckTicks);

            if (PortalManager.INSTANCE.canUsePortal(this, entity)) {
                IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{}", entity);

                PortalManager.INSTANCE.addPortalCooldown(entity, uuid);

                var portalData = PortalManager.INSTANCE.getPortalData(this);
                portalData.getConnectedPortalPos(uuid).ifPresent(portalPos -> {
                    float deltaRot = (entity.getYRot() - this.getYRot()) + (portalPos.rotation() - this.getYRot());
                    Vec3 offset = new Vec3(this.getX() - entity.getX(), this.getY() - entity.getY(), this.getZ() - entity.getZ()).yRot(deltaRot * Mth.DEG_TO_RAD);
                    Vec3 destination = portalPos.pos().subtract(offset);
                    entity.setYRot(entity.getYRot() + deltaRot);
                    entity.setDeltaMovement(entity.getDeltaMovement().yRot(deltaRot));
                    if (level.dimension().equals(portalPos.dimension())) {
                        entity.teleportTo(destination.x, destination.y, destination.z);
                        entity.setOldPosAndRot();
                    } else {
                        IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{} to dimension: {}", entity, portalPos.dimension());
                        var server = level.getServer();
                        if (server != null) {
                            var dim = server.getLevel(portalPos.dimension());
                            if (dim != null) {
                                entity.changeDimension(dim, new PortalTeleporter(destination));
                            }
                        }
                    }
                });
            }
        });
    }

    private Vec3 getDestinationPosition(PortalPos globalPos, Entity entity) {
        Vec3 offset = new Vec3(this.getX() - entity.getX(), this.getY() - entity.getY(), this.getZ() - entity.getZ());
        return new Vec3(globalPos.pos().x - offset.x, globalPos.pos().y - offset.y, globalPos.pos().z - offset.z);
    }

    @Override
    public void tick() {
        //IronsSpellbooks.LOGGER.debug("PortalEntity.tick isClientSide:{}, connectedPortal:{}", level.isClientSide, connectedPortal);
        if (level.isClientSide) {
            spawnParticles(.5f, new Vector3f(.8f, .15f, .65f));
            spawnParticles(.36f, new Vector3f(.5f, .05f, .6f));
            spawnParticles(.2f, new Vector3f(1f, .2f, .7f));
        } else if (level.getGameTime() % collisionCheckTicks == 0) {
            PortalManager.INSTANCE.processCooldownTick(uuid, -collisionCheckTicks);
            checkForEntitiesToTeleport();
        }

        ticksToLive--;
        if (ticksToLive <= 0) {
            discard();
        }
    }

    public void spawnParticles(float radius, Vector3f color) {
        int particles = 50;
        float step = 6.28f / particles;
        Vec3 center = this.getBoundingBox().getCenter();
        for (int i = 0; i < particles; i++) {
            float x = Mth.cos(i * step) * radius;
            float y = Mth.sin(i * step) * radius * 2;
            Vec3 offset = new Vec3(x, y, 0).yRot(-this.getYRot() * Mth.DEG_TO_RAD);
            level.addParticle(new DustParticleOptions(color, .5f), true, center.x + offset.x, center.y + offset.y, center.z + offset.z, 1d, 0d, 0);
        }
    }

    public UUID getOwnerUUID() {
        return this.entityData
                .get(DATA_ID_OWNER_UUID)
                .orElseGet(() -> this.entityData.get(DATA_ID_OWNER_UUID).orElse(null));
    }

    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_ID_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_OWNER_UUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("ticksToLive")) {
            ticksToLive = compoundTag.getLong("ticksToLive");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putLong("ticksToLive", ticksToLive);
    }

//    @Override
//    protected void readAdditionalSaveData(CompoundTag compoundTag) {
//        if (compoundTag.contains("ownerUUID")) {
//            ownerUUID = compoundTag.getUUID("ownerUUID");
//        }
//    }
//
//    @Override
//    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
//        if (ownerUUID != null) {
//            compoundTag.putUUID("ownerUUID", ownerUUID);
//        }
//    }


}


