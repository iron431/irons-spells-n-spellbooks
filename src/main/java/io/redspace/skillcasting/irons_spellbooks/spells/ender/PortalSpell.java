package io.redspace.skillcasting.irons_spellbooks.spells.ender;



import io.redspace.ironsspellbooks.api.config.DefaultConfig;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;

import io.redspace.ironsspellbooks.api.util.Utils;

import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlock;

import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;

import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;

import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;

import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;

import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;

import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;

import io.redspace.ironsspellbooks.registries.BlockRegistry;

import io.redspace.skillcasting.api.cast.CastContext;

import io.redspace.skillcasting.api.recast.RecastConfig;

import io.redspace.skillcasting.api.recast.RecastResult;

import io.redspace.skillcasting.api.skill.CastType;

import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;

import net.minecraft.ChatFormatting;

import net.minecraft.core.BlockPos;

import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.util.Mth;

import net.minecraft.world.entity.Entity;

import net.minecraft.world.level.ClipContext;

import net.minecraft.world.level.Level;

import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.phys.HitResult;

import net.minecraft.world.phys.Vec3;



import java.util.List;

import java.util.Optional;



public class PortalSpell extends AbstractSpellSkill {



    public static final int PORTAL_RECAST_COUNT = 2;

    private static final int RECAST_DURATION_TICKS = 20 * 120;

    private static final float CAST_DISTANCE = 48f;



    private final DefaultConfig defaultConfig = new DefaultConfig()

            .setMinRarity(SpellRarity.UNCOMMON)

            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)

            .setMaxLevel(3)

            .setCooldownSeconds(180)

            .build();



    public PortalSpell() {

        this.baseSpellPower = 5 * 60;

        this.spellPowerPerLevel = 2 * 60;

        this.baseManaCost = 200;

        this.manaCostPerLevel = 10;

        this.castTime = 0;

    }



    @Override

    public DefaultConfig getDefaultConfig() {

        return defaultConfig;

    }



    @Override

    public CastType getCastType() {

        return CastType.INSTANT;

    }



    @Override

    public List<MutableComponent> getUniqueInfo(CastContext castContext) {

        return List.of(

                Component.translatable("ui.irons_spellbooks.cast_range", Utils.stringTruncation(CAST_DISTANCE, 1)),

                Component.translatable("ui.irons_spellbooks.portal_duration",

                        Utils.timeFromTicks((int) (getSpellPower(castContext) * 20), 2))

        );

    }



    @Override

    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {

        return Optional.of(new RecastConfig(PORTAL_RECAST_COUNT, RECAST_DURATION_TICKS));

    }



    @Override

    public boolean checkPreCastConditions(CastContext castContext) {

        if (castContext.level().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {

            if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {

                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(

                        Component.translatable("ui.irons_spellbooks.cast_error_dimension").withStyle(ChatFormatting.RED)));

            }

            return false;

        }

        HitResult hitResult = Utils.raycastForBlock(castContext.level(), castContext.position(),

                castContext.position().add(castContext.direction().scale(CAST_DISTANCE)), ClipContext.Fluid.NONE);

        castContext.set(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT, hitResult);

        PortalData portalData = castContext.getOrNull(SpellcastingComponentTypes.PORTAL_DATA);

        if (portalData != null && portalData.isBlock) {

            if (hitResult.getType() == HitResult.Type.MISS

                    || !(castContext.level().getBlockEntity(((BlockHitResult) hitResult).getBlockPos()) instanceof PortalFrameBlockEntity portalFrame)

                    || portalFrame.isPortalConnected()) {

                if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {

                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(

                            Component.translatable("ui.irons_spellbooks.portal_target_failure").withStyle(ChatFormatting.RED)));

                }

                return false;

            }

        }

        return true;

    }



    @Override

    public void onCast(ServerLevel level, CastContext castContext) {

        if (!(level instanceof ServerLevel serverLevel)) {

            return;

        }

        HitResult hitResult = castContext.getOrNull(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT);

        if (hitResult == null) {

            return;

        }

        PortalData existingData = castContext.getOrNull(SpellcastingComponentTypes.PORTAL_DATA);

        boolean canHitBlock = existingData == null || existingData.isBlock;

        if (canHitBlock

                && hitResult.getType() == HitResult.Type.BLOCK

                && serverLevel.getBlockEntity(((BlockHitResult) hitResult).getBlockPos()) instanceof PortalFrameBlockEntity portalFrame

                && !portalFrame.isPortalConnected()) {

            handleBlockPortal(castContext, serverLevel, portalFrame);

        } else {

            handleEntityPortal(castContext, serverLevel, hitResult);

        }

    }



    private void handleBlockPortal(CastContext castContext, ServerLevel serverLevel, PortalFrameBlockEntity portalFrame) {

        Vec3 portalLocation = portalFrame.getPortalLocation();

        float portalRotation = portalFrame.getBlockState().getValue(PortalFrameBlock.FACING).toYRot();

        if (!castContext.has(SpellcastingComponentTypes.PORTAL_DATA)) {

            var portalData = new PortalData();

            portalData.isBlock = true;

            portalData.globalPos1 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);

            portalData.portalEntityId1 = portalFrame.getUUID();

            PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);

            portalFrame.setChanged();

            castContext.set(SpellcastingComponentTypes.PORTAL_DATA, portalData);

        } else {

            PortalData portalData = castContext.getOrNull(SpellcastingComponentTypes.PORTAL_DATA);

            if (portalData != null && portalData.globalPos1 != null && portalData.portalEntityId1 != null) {

                portalData.globalPos2 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);

                portalData.portalEntityId2 = portalFrame.getUUID();

                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);

                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId2, portalData);

                portalFrame.setChanged();

            }

        }

    }



    private void handleEntityPortal(CastContext castContext, ServerLevel serverLevel, HitResult hitResult) {

        Entity caster = castContext.asEntityCaster();

        if (caster == null) {

            return;

        }

        Vec3 hitResultPos = hitResult.getLocation().subtract(castContext.direction().multiply(0.25, 0, 0.25));

        Vec3 portalLocation = serverLevel.clip(new ClipContext(hitResultPos, hitResultPos.add(0, -caster.getBbHeight() - 1, 0),

                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation().add(0, 0.076, 0);

        float portalRotation = 90 + Utils.getAngle(portalLocation.x, portalLocation.z, caster.getX(), caster.getZ()) * Mth.RAD_TO_DEG;



        if (!castContext.has(SpellcastingComponentTypes.PORTAL_DATA)) {

            PortalData portalData = new PortalData();

            portalData.setPortalDuration(RECAST_DURATION_TICKS + 10);

            PortalEntity portalEntity = setupPortalEntity(castContext, portalData, portalLocation, portalRotation);

            portalData.globalPos1 = PortalPos.of(serverLevel.dimension(), portalLocation, portalRotation);

            portalData.portalEntityId1 = portalEntity.getUUID();

            castContext.set(SpellcastingComponentTypes.PORTAL_DATA, portalData);

        } else {

            PortalData portalData = castContext.getOrNull(SpellcastingComponentTypes.PORTAL_DATA);

            if (portalData != null && portalData.globalPos1 != null && portalData.portalEntityId1 != null) {

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

    public void onRecastFinished(CastContext castContext, RecastResult result) {

        if (result == RecastResult.USED_ALL_RECASTS) {

            return;

        }

        PortalData portalData = castContext.getOrNull(SpellcastingComponentTypes.PORTAL_DATA);

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

            PortalManager.INSTANCE.removePortalData(portalData.portalEntityId1);

        }

    }



    private int getPortalDuration(CastContext castContext) {

        return (int) (getSpellPower(castContext) * 20);

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


