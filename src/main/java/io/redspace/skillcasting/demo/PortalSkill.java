package io.redspace.skillcasting.demo;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlock;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Optional;

public class PortalSkill extends AbstractSkill {
    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        HitResult blockHitResult = Utils.raycastForBlock(castContext.level(), castContext.position(), castContext.position().add(castContext.direction().scale(32)), ClipContext.Fluid.NONE);
        castContext.set(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT.get(), blockHitResult);
        return true;
    }

    @Override
    public void onCast(CastContext castContext) {
        if (!(castContext.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        HitResult hitResult = castContext.get(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT.get());
        if (hitResult == null) {
            return;
        }
        PortalData portalCastData;
        if (!castContext.has(SkillcastingComponentTypes.PORTAL_DATA.get())) {
            // fixme: this is kinda like the "has recast for" pipeline of spellbooks. is this implicit "defer to alternative behavior" what we are looking for?
            // setup first portal.
        }

        if (hitResult.getType() == HitResult.Type.BLOCK && serverLevel.getBlockEntity(((BlockHitResult) hitResult).getBlockPos()) instanceof PortalFrameBlockEntity portalFrame && !portalFrame.isPortalConnected()) {
            handleBlockPortal(castContext, serverLevel, portalFrame);
        } else {
            handleEntityPortal(castContext, serverLevel, hitResult);
        }
    }

    private void handleBlockPortal(CastContext castContext, ServerLevel serverLevel, PortalFrameBlockEntity portalFrame) {
        Vec3 portalLocation = portalFrame.getPortalLocation();
        float portalRotation = portalFrame.getBlockState().getValue(PortalFrameBlock.FACING).toYRot();
        if (!castContext.has(SkillcastingComponentTypes.PORTAL_DATA.get())) {
            // fixme: this is kinda like the "has recast for" pipeline of spellbooks. is this implicit "defer to alternative behavior" what we are looking for?
            // setup first portal.
            var portalData = new PortalData();
            portalData.isBlock = true;
            portalData.globalPos1 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);
            portalData.portalEntityId1 = portalFrame.getUUID();
            PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);
            portalFrame.setChanged();
            castContext.set(SkillcastingComponentTypes.PORTAL_DATA.get(), portalData);
        } else {
            // complete connection
            PortalData portalData = castContext.get(SkillcastingComponentTypes.PORTAL_DATA.get());
            if (portalData.globalPos1 != null & portalData.portalEntityId1 != null) {
                portalData.globalPos2 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);
                portalData.portalEntityId2 = portalFrame.getUUID();
                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);
                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId2, portalData);
                portalFrame.setChanged();
            }
        }
    }

    private void handleEntityPortal(CastContext castContext, ServerLevel serverLevel, HitResult hitResult) {
        Vec3 hitResultPos = hitResult.getLocation().subtract(castContext.direction().multiply(.25, 0, .25));
        Vec3 portalLocation = serverLevel.clip(new ClipContext(hitResultPos, hitResultPos.add(0, -2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation().add(0, 0.076, 0);
        float portalRotation = 90 + Utils.getAngle(portalLocation.x, portalLocation.z, castContext.position().x, castContext.position().y) * Mth.RAD_TO_DEG;
        if (!castContext.has(SkillcastingComponentTypes.PORTAL_DATA.get())) {
            PortalData portalData = new PortalData();
            // fixme: should probably be getting the existing recast config over simulating a fresh one
            portalData.setPortalDuration(getRecastConfig(castContext).map(RecastConfig::durationTicks).orElse(100) + 10);
            PortalEntity portalEntity = setupPortalEntity(castContext, portalData, portalLocation, portalRotation);
            portalData.globalPos1 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);
            portalData.portalEntityId1 = portalEntity.getUUID();
            castContext.set(SkillcastingComponentTypes.PORTAL_DATA.get(), portalData);
        } else {
            PortalData portalData = castContext.get(SkillcastingComponentTypes.PORTAL_DATA.get());
            if (portalData.globalPos1 != null & portalData.portalEntityId1 != null) {
                portalData.globalPos2 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);
                portalData.setPortalDuration(getPortalDuration(castContext));
                PortalEntity secondPortalEntity = setupPortalEntity(castContext, portalData, portalLocation, portalRotation);
                secondPortalEntity.setPortalConnected();
                portalData.portalEntityId2 = secondPortalEntity.getUUID();
                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);
                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId2, portalData);

                var firstPortalLevel = serverLevel.getServer().getLevel(portalData.globalPos1.dimension());
                if (firstPortalLevel != null) {
                    var firstPortalEntity = (PortalEntity) firstPortalLevel.getEntity(portalData.portalEntityId1);
                    if (firstPortalEntity != null) {
                        firstPortalEntity.setPortalConnected();
                        firstPortalEntity.setTicksToLive(portalData.ticksToLive);
                    }
                }
            }
        }
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(2, 20 * 10));
    }

    @Override
    public void onRecastFinished(CastContext castContext, RecastResult result) {
        if (result == RecastResult.USED_ALL_RECASTS) {
            // successful portal, no cleanup required
            return;
        }
        PortalData portalData = castContext.get(SkillcastingComponentTypes.PORTAL_DATA.get());
        if (portalData == null || portalData.portalEntityId1 == null || portalData.globalPos1 == null) {
            return;
        }
        if (!(castContext.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var portalLevel = serverLevel.getServer().getLevel(portalData.globalPos1.dimension());
        if (portalLevel != null) {
            if (portalData.isBlock) {
                var block = BlockPos.containing(portalData.globalPos1.pos());
                if (portalLevel.isLoaded(block)) {
                    portalLevel.getBlockEntity(block, BlockRegistry.PORTAL_FRAME_BLOCK_ENTITY.get()).ifPresent(PortalFrameBlockEntity::setChanged);
                }
            } else {
                var portal1 = portalLevel.getEntity(portalData.portalEntityId1);
                if (portal1 != null) {
                    portal1.discard();
                }
            }
        }
        PortalManager.INSTANCE.removePortalData(portalData.portalEntityId1);
    }

    public int getPortalDuration(CastContext castContext) {
        return 20 * 2 * 60;
    }

    private PortalEntity setupPortalEntity(CastContext castContext, PortalData portalData, Vec3 spawnPos, float rotation) {
        var portalEntity = new PortalEntity(castContext.level(), portalData);
        if (castContext.caster().get() instanceof Entity entity) {
            portalEntity.setOwnerUUID(entity.getUUID());
        }
        portalEntity.moveTo(spawnPos);
        portalEntity.setYRot(rotation);
        castContext.level().addFreshEntity(portalEntity);
        return portalEntity;
    }
}
