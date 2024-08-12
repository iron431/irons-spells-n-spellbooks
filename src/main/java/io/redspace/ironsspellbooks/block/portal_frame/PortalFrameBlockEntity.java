package io.redspace.ironsspellbooks.block.portal_frame;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PortalFrameBlockEntity extends BlockEntity {
    boolean isTopHalf;
    UUID uuid;
    @Nullable PortalData portalData;
    boolean clientIsConnected;

    public PortalFrameBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        this(BlockRegistry.PORTAL_FRAME_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    public PortalFrameBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.uuid = UUID.randomUUID();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        tag.putUUID("uuid", this.uuid);
    }

    public void setPortalData(PortalData portalData) {
        this.portalData = portalData;
        this.setChanged();
    }

    public boolean isPortalConnected() {
        return portalData != null;
    }

    public void breakPortalConnection() {
        if (this.portalData != null) {
            PortalManager.INSTANCE.removePortalData(portalData.portalEntityId1);
            PortalManager.INSTANCE.removePortalData(portalData.portalEntityId2);
            var server = this.level == null ? null : this.level.getServer();
            if (server != null) {
                boolean primary = this.getUUID().equals(portalData.portalEntityId1);
                var otherPos = primary ? portalData.globalPos2 : portalData.globalPos1;
                var dimension = server.getLevel(otherPos.dimension());
                var otherBlockPos = BlockPos.containing(otherPos.pos());
                if (dimension != null && dimension.isLoaded(otherBlockPos)) {
                    if (dimension.getBlockEntity(otherBlockPos) instanceof PortalFrameBlockEntity portalFrame) {
                        portalFrame.setPortalData(null);
                    }
                }
            }
            this.portalData = null;
            this.setChanged();
        }
    }

    public void teleport(Entity entity) {
        PortalManager.INSTANCE.processDelayCooldown(uuid, entity.getUUID(), 1);
        IronsSpellbooks.LOGGER.debug("PortalFrame.teleport: {}", this.getBlockPos());
        IronsSpellbooks.LOGGER.debug("PortalFrame.teleport: {}", this.portalData);
        if (PortalManager.INSTANCE.canUsePortal(this.uuid, entity)) {
            //PortalManager.INSTANCE.addPortalCooldown(entity, uuid);
            var portalData = PortalManager.INSTANCE.getPortalData(this.uuid);
            portalData.getConnectedPortalPos(uuid).ifPresent(portalPos -> {
                Vec3 destination = portalPos.pos().add(0, entity.getY() - this.getBlockPos().getY(), 0);
                entity.setYRot(portalPos.rotation());
                this.level.playSound(null, this.getBlockPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1f, 1f);
                if (level.dimension().equals(portalPos.dimension())) {
                    entity.teleportTo(destination.x, destination.y + .1, destination.z);
                    var delta = entity.getDeltaMovement();
                    float hspeed = (float) Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                    float f = portalPos.rotation() * Mth.DEG_TO_RAD;
                    entity.setDeltaMovement(-Mth.sin(f) * hspeed, delta.y, Mth.cos(f) * hspeed);
                } else {
                    //IronsSpellbooks.LOGGER.debug("PortalEntity: teleport entity:{} to dimension: {}", entity, portalPos.dimension());
                    var server = level.getServer();
                    if (server != null) {
                        var dim = server.getLevel(portalPos.dimension());
                        if (dim != null) {
                            entity.changeDimension(new DimensionTransition(dim, destination, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
                        }
                    }
                }
                this.level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1f, 1f);
            });
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(tag, pRegistries);
        if (tag.contains("uuid")) {
            this.uuid = tag.getUUID("uuid");
            var portalData = PortalManager.INSTANCE.getPortalData(uuid);
            if (portalData != null) {
                this.portalData = portalData;
            }
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        var tag = super.getUpdateTag(pRegistries);
        tag.putBoolean("connected",this.isPortalConnected());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        var packet = ClientboundBlockEntityDataPacket.create(this);
        //irons_spellbooks.LOGGER.debug("getUpdatePacket: packet.getTag:{}", packet.getTag());
        return packet;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.clientIsConnected = tag.getBoolean("connected");
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
