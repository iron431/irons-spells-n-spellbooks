package io.redspace.ironsspellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlock;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.capabilities.magic.SerializedTargetData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PocketDimensionSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "pocket_dimension");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public PocketDimensionSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 40;
        this.baseManaCost = 300;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        // do not allow cast time scaling
        return castTime;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (level.dimension().equals(PocketDimensionManager.POCKET_DIMENSION) && !PocketDimensionManager.INSTANCE.hasId(serverPlayer)) {
            return false;
        }
        if (ServerConfigs.POCKET_INACCESSIBLE_DIMENSIONS.contains(level.dimension())) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_dimension").withStyle(ChatFormatting.RED)));
            return false;
        }
        if (entity.getCombatTracker().inCombat) {
            serverPlayer.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_combat").withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.POCKET_DIMENSION_TRAVEL.get());
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ELDRITCH_PREPARE.get());
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SerializedTargetData();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
        if (entity instanceof ServerPlayer serverPlayer) {
            Vec3 particlePos = serverPlayer.getBoundingBox().getCenter();
            MagicManager.spawnParticles(level, ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 100, 0.1, 0.2, 0.1, 0.1, false);
            // Pull player out of pocket dimension
            if (level.dimension().equals(PocketDimensionManager.POCKET_DIMENSION) && PocketDimensionManager.INSTANCE.hasId(serverPlayer)) {
                // Fetch return position and put them there
                PortalPos returnTo = PocketDimensionManager.INSTANCE.returnPosForPlayer(serverPlayer);
                if (returnTo != null)
                {
                    Scroll.attemptRemoveScrollAfterCast(serverPlayer); // Manually call this because this serverplayer will be removed from the level after the spellcast
                    PocketDimensionManager.INSTANCE.updateReturn(serverPlayer);
                    serverPlayer.stopRiding();
                    serverPlayer.changeDimension(new DimensionTransition(serverPlayer.getServer().getLevel(returnTo.dimension()), returnTo.pos(), Vec3.ZERO, returnTo.rotation(), serverPlayer.getXRot(), DimensionTransition.DO_NOTHING));
                }
            }
            // Place player in pocket dimension
            else {
                PortalData portalData = new PortalData();
                portalData.setPortalDuration(20 * 60);
                portalData.firstPortal(serverPlayer.getUUID(), PortalPos.of(serverPlayer.level.dimension(), serverPlayer.position(), serverPlayer.getYRot()));
                // Generate a pocket and get the portal
                PocketDimensionManager.INSTANCE.generatePocketIfUnassigned(serverPlayer);
                BlockPos portalPos = PocketDimensionManager.INSTANCE.findPortalForStructure(serverPlayer.serverLevel(), PocketDimensionManager.INSTANCE.structurePosForPlayer(serverPlayer));
                ServerLevel pocketLevel = serverPlayer.getServer().getLevel(PocketDimensionManager.POCKET_DIMENSION);
                var portalChunk = pocketLevel.getChunk(portalPos);
                var portal = portalChunk.getBlockEntity(portalPos);
                // If no portal is found, make a new one
                if (!(portal instanceof PortalFrameBlockEntity portalFrameBlockEntity)) {
                    pocketLevel.setBlockAndUpdate(portalPos, BlockRegistry.POCKET_PORTAL_FRAME.get().defaultBlockState().setValue(PortalFrameBlock.HALF, DoubleBlockHalf.LOWER));
                    pocketLevel.setBlockAndUpdate(portalPos.above(), BlockRegistry.POCKET_PORTAL_FRAME.get().defaultBlockState().setValue(PortalFrameBlock.HALF, DoubleBlockHalf.UPPER));
                    portal = portalChunk.getBlockEntity(portalPos);
                }
                // Target portal if the new one is found
                if (portal instanceof PortalFrameBlockEntity portalFrameBlockEntity) {
                    var uuid = portalFrameBlockEntity.getUUID();
                    portalData.secondPortal(uuid, PortalPos.of(PocketDimensionManager.POCKET_DIMENSION, portalPos.getBottomCenter(), 180));
                    PortalManager.INSTANCE.addPortalData(uuid, portalData);
                    portalFrameBlockEntity.setChanged();
                    PortalManager.INSTANCE.addDirectPortalCooldown(serverPlayer, uuid); // Manually add cooldown as if the player used the portal to help prevent immediately teleporting back
                }
                // Regardless of if a portal is present, update their return destination and send player to their pocket dimension
                Scroll.attemptRemoveScrollAfterCast(serverPlayer); // Manually call this because this serverplayer will be removed from the level after the spellcast
                PocketDimensionManager.INSTANCE.updateReturn(serverPlayer);
                serverPlayer.stopRiding();
                serverPlayer.changeDimension(new DimensionTransition(pocketLevel, portalPos.getBottomCenter(), Vec3.ZERO, 180, serverPlayer.getXRot(), DimensionTransition.DO_NOTHING));
            }
        }
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }
}
