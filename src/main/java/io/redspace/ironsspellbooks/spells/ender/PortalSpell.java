package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class PortalSpell extends AbstractSpell {
    public static final int PORTAL_RECAST_COUNT = 2;
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "portal");

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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("PortalSpell.onCast isClient:{}, entity:{}, pmd:{}", level.isClientSide, entity, playerMagicData);
        }

        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
            var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, getCastDistance(spellLevel, entity)).getLocation().subtract(entity.getForward().normalize().multiply(.25, 0, .25));
            Vec3 portalLocation = level.clip(new ClipContext(blockHitResult, blockHitResult.add(0, -entity.getBbHeight() - 1, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getLocation().add(0, 0.076, 0);
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

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
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
