package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.goals.HomeOwner;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RecallSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "recall");

    public RecallSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 80;
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
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        // do not allow cast time scaling
        return castTime;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (entity.getCombatTracker().inCombat) {
            serverPlayer.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_combat").withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
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
        return Optional.of(SoundRegistry.RECALL_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENDERMAN_TELEPORT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        playSound(getCastFinishSound(), entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            var destination = NeoForge.EVENT_BUS.post(new PlayerRespawnPositionEvent(serverPlayer, serverPlayer.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING), false)).getDimensionTransition();
            serverPlayer.changeDimension(destination);
        } else if (entity instanceof HomeOwner homeOwner && homeOwner.getHome() != null) {
            //no dimension check because lazy
            var pos = homeOwner.getHome();
            entity.teleportTo(pos.getX(), pos.getY() + .15, pos.getZ());
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public static void ambientParticles(LivingEntity entity, SyncedSpellData spellData) {
        float f = entity.tickCount * .125f;
        Vec3 trail1 = new Vec3(Mth.cos(f), Mth.sin(f * 2), Mth.sin(f)).normalize()/*.scale(1.5f + Mth.sin(f) * .5f)*/;
        Vec3 trail2 = new Vec3(Mth.sin(f), Mth.cos(f * 2), Mth.cos(f)).normalize()/*.scale(1.5f + Mth.cos(f) * .5f)*/;
        Vec3 trail3 = trail1.multiply(trail2).normalize().scale(1f + (Mth.sin(f) + Mth.cos(f)) * .5f);
        Vec3 pos = entity.getBoundingBox().getCenter();
        entity.level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail1.x, pos.y + trail1.y, pos.z + trail1.z, 0, 0, 0);
        entity.level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail2.x, pos.y + trail2.y, pos.z + trail2.z, 0, 0, 0);
        entity.level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail3.x, pos.y + trail3.y, pos.z + trail3.z, 0, 0, 0);
    }

    @Override
    public void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent(soundEvent -> entity.playSound(soundEvent, 2.0f, 1f));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }
}