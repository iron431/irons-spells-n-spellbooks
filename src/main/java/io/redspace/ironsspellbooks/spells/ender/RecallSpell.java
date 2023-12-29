package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

@AutoSpellConfig
public class RecallSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "recall");

    public RecallSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 100;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(300)
            .build();

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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity instanceof ServerPlayer serverPlayer) {
            ServerLevel respawnLevel = ((ServerLevel) world).getServer().getLevel(serverPlayer.getRespawnDimension());
            respawnLevel = respawnLevel == null ? world.getServer().overworld() : respawnLevel;
            var spawnLocation = findSpawnPosition(respawnLevel, serverPlayer);
            IronsSpellbooks.LOGGER.debug("Recall.onCast findSpawnLocation: {}", spawnLocation);
            if (spawnLocation.isPresent()) {
                Vec3 vec3 = spawnLocation.get();
                IronsSpellbooks.LOGGER.debug("Recall.onCast.a dimension: {} -> {}", serverPlayer.level.dimension(), respawnLevel.dimension());
                if (serverPlayer.level.dimension() != respawnLevel.dimension()) {
                    serverPlayer.changeDimension(respawnLevel, new PortalTeleporter(vec3));
                } else {
                    serverPlayer.teleportTo(vec3.x, vec3.y, vec3.z);
                }
            } else {
                respawnLevel = world.getServer().overworld();
                IronsSpellbooks.LOGGER.debug("Recall.onCast.b dimension: {} -> {}", serverPlayer.level.dimension(), respawnLevel.dimension());
                if (serverPlayer.level.dimension() != respawnLevel.dimension()) {
                    serverPlayer.changeDimension(respawnLevel, new PortalTeleporter(Vec3.ZERO));
                }
                serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
                var pos = respawnLevel.getSharedSpawnPos();
                serverPlayer.teleportTo(pos.getX(), pos.getY(), pos.getZ());
            }
        }
        super.onCast(world, spellLevel, entity, playerMagicData);
    }

    /**
     * Adapted from vanilla {@link Player#findRespawnPositionAndUseSpawnBlock(ServerLevel, BlockPos, float, boolean, boolean)}
     */
    public static Optional<Vec3> findSpawnPosition(ServerLevel level, ServerPlayer player) {
        BlockPos spawnBlockpos = player.getRespawnPosition();
        if (spawnBlockpos == null) {
            return Optional.empty();
        }
        BlockState blockstate = level.getBlockState(spawnBlockpos);
        Block block = blockstate.getBlock();
        if (block instanceof RespawnAnchorBlock && blockstate.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(level)) {
            IronsSpellbooks.LOGGER.debug("RecallSpell.findSpawnPosition.respawnAnchor");
            return RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, spawnBlockpos);
        } else if (block instanceof BedBlock && BedBlock.canSetSpawn(level)) {
            IronsSpellbooks.LOGGER.debug("RecallSpell.findSpawnPosition.bed");
            return BedBlock.findStandUpPosition(EntityType.PLAYER, level, spawnBlockpos, player.getYRot());
        } else {
            return Optional.empty();
//            boolean flag = block.isPossibleToRespawnInThis();
//            boolean flag1 = level.getBlockState(spawnBlockpos.above()).getBlock().isPossibleToRespawnInThis();
//            return flag && flag1 ? Optional.of(new Vec3((double)spawnBlockpos.getX() + 0.5D, (double)spawnBlockpos.getY() + 0.1D, (double)spawnBlockpos.getZ() + 0.5D)) : Optional.empty();
        }
    }

    //TODO: replace with portal's teleporter on merge with recast?
    public static class PortalTeleporter implements ITeleporter {
        private final Vec3 destinationPosition;

        PortalTeleporter(Vec3 destinationPosition) {
            this.destinationPosition = destinationPosition;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
            entity.fallDistance = 0;
            return repositionEntity.apply(false);
        }

        @Override
        public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return new PortalInfo(destinationPosition, Vec3.ZERO, entity.getYRot(), entity.getXRot());
        }

        @Override
        public boolean isVanilla() {
            return false;
        }

        @Override
        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
            return false;
        }
    }
}