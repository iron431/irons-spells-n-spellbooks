package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AutoSpellConfig
public class PortalSpell extends AbstractSpell {
    public static final int PORTAL_RECAST_COUNT = 2;
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "portal");

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
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new PortalData();
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return PORTAL_RECAST_COUNT;
    }


    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        var recast = playerMagicData.getPlayerRecasts().getRecastInstance(this.getSpellId());
        if (recast != null && recast.getCastData() instanceof PortalData portalData && portalData.isBlock) {
            var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, getCastDistance(spellLevel, entity));
            if (blockHitResult.getType() == HitResult.Type.MISS || !(level.getBlockEntity(blockHitResult.getBlockPos()) instanceof PortalFrameBlockEntity portalFrame) || portalFrame.isPortalConnected()) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.portal_target_failure").withStyle(ChatFormatting.RED)));
                }
                return false;
            }
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("PortalSpell.onCast isClient:{}, entity:{}, pmd:{}", level.isClientSide, entity, playerMagicData);
        }

        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, getCastDistance(spellLevel, entity));
            if (blockHitResult.getType() != HitResult.Type.MISS && level.getBlockEntity(blockHitResult.getBlockPos()) instanceof PortalFrameBlockEntity portalFrame && !portalFrame.isPortalConnected()) {
                handleBlockPortal(level, spellLevel, entity, castSource, playerMagicData, player, serverLevel, blockHitResult, portalFrame);
            } else {
                handleEntityPortal(level, spellLevel, entity, castSource, playerMagicData, player, serverLevel, blockHitResult);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void handleBlockPortal(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, Player player, ServerLevel serverLevel, BlockHitResult blockHitResult, PortalFrameBlockEntity portalFrame) {
        Vec3 portalLocation = blockHitResult.getBlockPos().getBottomCenter();
        float portalRotation = blockHitResult.getDirection().toYRot();
        if (playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
            var portalData = (PortalData) playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId()).getCastData();

            if (portalData.globalPos1 != null & portalData.portalEntityId1 != null) {
                portalData.globalPos2 = PortalPos.of(player.level.dimension(), portalLocation, portalRotation);
//                portalData.setPortalDuration(getPortalDuration(spellLevel, player));
//                PortalEntity secondPortalEntity = setupPortalEntity(serverLevel, portalData, player, portalLocation, portalRotation);
//                secondPortalEntity.setPortalConnected();
                portalData.portalEntityId2 = portalFrame.getUUID();
                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);
                PortalManager.INSTANCE.addPortalData(portalData.portalEntityId2, portalData);
                portalFrame.setPortalData(portalData);
            }
        } else {
            var portalData = new PortalData();
            portalData.isBlock = true;
            portalData.globalPos1 = PortalPos.of(player.level.dimension(), portalLocation, portalRotation);
            portalData.portalEntityId1 = portalFrame.getUUID();
            //FIXME: if you relog while casting, this will break the connection (its only being set locally, not in portal manager)
            portalFrame.setPortalData(portalData);
            playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 2, getRecastDuration(spellLevel, player), castSource, portalData), playerMagicData);
        }
    }

    private void handleEntityPortal(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, Player player, ServerLevel serverLevel, BlockHitResult blockHitResult) {
        Vec3 hitResultPos = blockHitResult.getLocation().subtract(entity.getForward().normalize().multiply(.25, 0, .25));
        Vec3 portalLocation = level.clip(new ClipContext(hitResultPos, hitResultPos.add(0, -entity.getBbHeight() - 1, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getLocation().add(0, 0.076, 0);
        float portalRotation = 90 + Utils.getAngle(portalLocation.x, portalLocation.z, entity.getX(), entity.getZ()) * Mth.RAD_TO_DEG;
        if (playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
            var portalData = (PortalData) playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId()).getCastData();

            if (portalData.globalPos1 != null & portalData.portalEntityId1 != null) {
                portalData.globalPos2 = PortalPos.of(player.level.dimension(), portalLocation, portalRotation);
                portalData.setPortalDuration(getPortalDuration(spellLevel, player));
                PortalEntity secondPortalEntity = setupPortalEntity(serverLevel, portalData, player, portalLocation, portalRotation);
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
        } else {
            var portalData = new PortalData();
            portalData.setPortalDuration(getRecastDuration(spellLevel, player) + 10);
            PortalEntity portalEntity = setupPortalEntity(level, portalData, player, portalLocation, portalRotation);
            portalData.globalPos1 = PortalPos.of(player.level.dimension(), portalLocation, portalRotation);
            portalData.portalEntityId1 = portalEntity.getUUID();
            playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 2, getRecastDuration(spellLevel, player), castSource, portalData), playerMagicData);
        }
    }

    private PortalEntity setupPortalEntity(Level level, PortalData portalData, Player owner, Vec3 spawnPos, float rotation) {
        var portalEntity = new PortalEntity(level, portalData);
        portalEntity.setOwnerUUID(owner.getUUID());
        portalEntity.moveTo(spawnPos);
        portalEntity.setYRot(rotation);
        level.addFreshEntity(portalEntity);
        return portalEntity;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (recastResult != RecastResult.USED_ALL_RECASTS) {
            if (castDataSerializable instanceof PortalData portalData && portalData.portalEntityId1 != null) {
                if (portalData.globalPos1 != null) {
                    var server = serverPlayer.getServer();
                    if (server != null) {
                        var level = server.getLevel(portalData.globalPos1.dimension());
                        if (level != null) {
                            var portal1 = level.getEntity(portalData.portalEntityId1);
                            if (portal1 != null) {
                                portal1.discard();
                            } else {
                                PortalManager.INSTANCE.removePortalData(portalData.portalEntityId1);
                            }
                        }
                    }
                }
            }
        }
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    public int getRecastDuration(int spellLevel, LivingEntity caster) {
        return 20 * 120;
    }

    public int getPortalDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20);
    }

    private float getCastDistance(int spellLevel, LivingEntity sourceEntity) {
        return 48;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.cast_range", Utils.stringTruncation(getCastDistance(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.portal_duration", Utils.timeFromTicks(getPortalDuration(spellLevel, caster), 2))
        );
    }
}
