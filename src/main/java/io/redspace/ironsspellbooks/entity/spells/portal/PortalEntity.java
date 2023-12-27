package io.redspace.ironsspellbooks.entity.spells.portal;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
    private static final int collisionCheckTicks = 5;

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

        level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.1, 1.1, 1.1)).forEach(livingEntity -> {
            //TODO: remove extraneous logging
            IronsSpellbooks.LOGGER.debug("PortalEntity: entity near portal:{}", livingEntity);

            PortalManager.INSTANCE.processDelayCooldown(uuid, livingEntity.getUUID(), collisionCheckTicks);

            if (PortalManager.INSTANCE.canUsePortal(this, livingEntity)) {
                IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{}", livingEntity);

                PortalManager.INSTANCE.addPortalCooldown(livingEntity, uuid);

                var portalData = PortalManager.INSTANCE.getPortalData(this);
                portalData.getConnectedPortalPos(uuid).ifPresent(globalPos -> {
                    if (level.dimension().equals(globalPos.dimension())) {
                        livingEntity.teleportTo(globalPos.pos().getX(), globalPos.pos().getY(), globalPos.pos().getZ());
                    } else {
                        IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{} to dimension: {}", livingEntity, globalPos.dimension());
                        var server = level.getServer();
                        if (server != null) {
                            var dim = server.getLevel(globalPos.dimension());
                            if (dim != null) {
                                livingEntity.changeDimension(dim, new PortalTeleporter(globalPos.pos()));
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void tick() {
        //IronsSpellbooks.LOGGER.debug("PortalEntity.tick isClientSide:{}, connectedPortal:{}", level.isClientSide, connectedPortal);
        if (level.isClientSide) {
            spawnParticles();
        } else if (level.getGameTime() % collisionCheckTicks == 0) {
            PortalManager.INSTANCE.processCooldownTick(uuid, -collisionCheckTicks);
            checkForEntitiesToTeleport();
        }

        ticksToLive--;
        if (ticksToLive <= 0) {
            discard();
        }
    }

    public void spawnParticles() {
        int particles = 100;
        float radius = 1f;
        float step = 6.28f / particles;
        var color = new Vector3f(.5f, .05f, .6f);
        for (int i = 0; i < particles; i++) {
            float x = Mth.cos(i * step) * radius;
            float y = Mth.sin(i * step) * radius * 2;
            Vec3 offset = new Vec3(x, y, 0).yRot(this.getYRot() * Mth.DEG_TO_RAD);
            level.addParticle(new DustParticleOptions(color, .5f), getX() + offset.x, getY() + offset.y, getZ() + offset.z, 1d, 0d, 0);
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


