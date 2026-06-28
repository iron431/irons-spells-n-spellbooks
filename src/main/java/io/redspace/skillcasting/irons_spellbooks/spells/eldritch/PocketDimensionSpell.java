package io.redspace.skillcasting.irons_spellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class PocketDimensionSpell extends AbstractSpellSkill {

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
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.ELDRITCH_PREPARE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.POCKET_DIMENSION_TRAVEL).toOpt();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_TIME, castTime);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        Level level = castContext.level();
        if (level.dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("ui.irons_spellbooks.cast_error_dimension").withStyle(ChatFormatting.RED)));
            return false;
        }
        if (serverPlayer.getCombatTracker().inCombat) {
            serverPlayer.displayClientMessage(
                    Component.translatable("ui.irons_spellbooks.cast_error_combat").withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        PortalData portalData = new PortalData();
        portalData.setPortalDuration(20 * 60);
        portalData.firstPortal(serverPlayer.getUUID(), PortalPos.of(serverPlayer.level().dimension(), serverPlayer.position(), serverPlayer.getYRot()));

        PocketDimensionManager.INSTANCE.maybeGeneratePocketRoom(serverPlayer);
        BlockPos portalPos = PocketDimensionManager.INSTANCE.findPortalForStructure(
                serverPlayer.serverLevel(), PocketDimensionManager.INSTANCE.structurePosForPlayer(serverPlayer));
        ServerLevel pocketLevel = serverPlayer.getServer().getLevel(PocketDimensionManager.POCKET_DIMENSION);
        if (pocketLevel == null) {
            return;
        }
        var portal = pocketLevel.getBlockEntity(portalPos);
        if (portal instanceof PortalFrameBlockEntity portalFrameBlockEntity) {
            Vec3 particlePos = serverPlayer.getBoundingBox().getCenter();
            MagicManager.spawnParticles(level, ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 100, 0.1, 0.2, 0.1, 0.1, false);

            var uuid = portalFrameBlockEntity.getUUID();
            portalData.secondPortal(uuid, PortalPos.of(PocketDimensionManager.POCKET_DIMENSION, portalPos.getBottomCenter(), 180));
            PortalManager.INSTANCE.addPortalData(uuid, portalData);
            portalFrameBlockEntity.setChanged();
            PortalManager.INSTANCE.addDirectPortalCooldown(serverPlayer, uuid);
            Scroll.attemptRemoveScrollAfterCast(serverPlayer);
            serverPlayer.stopRiding();
            serverPlayer.changeDimension(new DimensionTransition(
                    pocketLevel,
                    portalData.globalPos2.pos(),
                    Vec3.ZERO,
                    portalData.globalPos2.rotation(),
                    serverPlayer.getXRot(),
                    DimensionTransition.DO_NOTHING));
            castContext.find(SkillcastingComponentTypes.ON_CAST_SOUND)
                    .ifPresent(sound -> sound.play(pocketLevel, portalData.globalPos2.pos(), SoundSource.PLAYERS));
        }
    }
}
