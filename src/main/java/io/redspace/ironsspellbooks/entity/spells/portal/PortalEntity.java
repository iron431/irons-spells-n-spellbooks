package io.redspace.ironsspellbooks.entity.spells.portal;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PortalEntity extends Entity implements AntiMagicSusceptible {
    private final static EntityDataAccessor<Optional<UUID>> CONNECTED_PORTAL_GUID = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private PortalEntity connectedPortal;
    private int durationTicks;

    private final HashMap<UUID, Long> cooldownLookup = new HashMap<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final int collisionCheckTicks = 5;

    @SuppressWarnings("FieldCanBeLocal")
    private final int cooldownTicks = 60;

    public PortalEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.PORTAL.get(), level);
    }

    public PortalEntity(Level level, LivingEntity owner, PortalEntity connectedPortal) {
        this(EntityRegistry.PORTAL.get(), level);
        this.connectedPortal = connectedPortal;
        connectedPortal.setConnectedPortal(this);
    }

    public PortalEntity(EntityType<PortalEntity> portalEntityEntityType, Level level) {
        super(portalEntityEntityType, level);

        //TODO: remove this log
        if (!level.isClientSide) {
            IronsSpellbooks.LOGGER.debug("PortalEntity created: dimension:{}, id:{}", level.dimension(), getUUID());
        }
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public void setConnectedPortal(PortalEntity portalEntity) {
        if (connectedPortal != null) {
            IronsSpellbooks.LOGGER.warn("PortalEntity: Connected portal already exists. Cannot set another one");
            return;
        }
        this.connectedPortal = portalEntity;
    }

    public void checkForEntityCollision() {
        if (this.level.isClientSide) return;

        level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.1, 1.1, 1.1)).forEach(livingEntity -> {
            //TODO: remove extraneous logging
            IronsSpellbooks.LOGGER.debug("PortalEntity: entity near portal:{}", livingEntity);
            if (canUsePortal(livingEntity)) {
                IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{}", livingEntity);
                addPortalCooldown(livingEntity);

                if (level.dimension().equals(connectedPortal.level.dimension())) {
                    var blockPos = connectedPortal.blockPosition();
                    livingEntity.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                } else {
                    IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{} to dimension: {}", livingEntity, connectedPortal.level.dimension());
                    livingEntity.changeDimension((ServerLevel) connectedPortal.level, new PortalTeleporter(connectedPortal.blockPosition()));
                }
            }
        });
    }

    public void addPortalCooldown(LivingEntity livingEntity) {
        if (level.isClientSide) {
            return;
        }

        cooldownLookup.put(livingEntity.getUUID(), level.getGameTime() + cooldownTicks);
    }

    public boolean canUsePortal(LivingEntity livingEntity) {
        return connectedPortal != null && !this.isEntityOnCooldown(livingEntity) && !connectedPortal.isEntityOnCooldown(livingEntity);
    }

    public boolean isEntityOnCooldown(LivingEntity livingEntity) {
        if (level.isClientSide) {
            return false;
        }

        var cooldownExpiration = cooldownLookup.get(livingEntity.getUUID());

        if (cooldownExpiration != null) {
            if (cooldownExpiration > level.getGameTime()) {
                return true;
            } else {
                cooldownLookup.remove(livingEntity.getUUID());
            }
        }

        return false;
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

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        IronsSpellbooks.LOGGER.debug("PortalEntity.onAntiMagic isClientSide:{}, connectedPortal:{}", level.isClientSide, connectedPortal);
        if (connectedPortal != null) {
            connectedPortal.discard();
        }
        discard();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CONNECTED_PORTAL_GUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("connectedPortal")) {
            if (level instanceof ServerLevel serverLevel) {
                var portalToConnect = serverLevel.getEntity(compoundTag.getUUID("connectedPortal"));
                if (portalToConnect instanceof PortalEntity otherPortalEntity) {
                    this.connectedPortal = otherPortalEntity;
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        if (connectedPortal != null) {
            compoundTag.putUUID("connectedPortal", connectedPortal.uuid);
        }
    }

    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        //IronsSpellbooks.LOGGER.debug("PortalEntity.tick isClientSide:{}, connectedPortal:{}", level.isClientSide, connectedPortal);
        if (level.isClientSide) {
            spawnParticles();
        } else if (level.getGameTime() % collisionCheckTicks == 0) {
            durationTicks--;
            checkForEntityCollision();
            if (durationTicks <= 0) {
                discard();
            }
        }
    }
}


