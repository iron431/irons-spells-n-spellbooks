package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.entity.mobs.goals.HomeOwner;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

import java.util.Optional;

public class RecallSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(300)
            .build();

    public RecallSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 80;
        this.baseManaCost = 100;
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
        return PlayableSound.standard(SoundRegistry.RECALL_PREPARE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.of(SoundEvents.ENDERMAN_TELEPORT, 2f, 1f).toOpt();
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
        if (serverPlayer.getCombatTracker().inCombat) {
            serverPlayer.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_combat")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof ServerPlayer serverPlayer) {
            var destination = NeoForge.EVENT_BUS.post(new PlayerRespawnPositionEvent(serverPlayer,
                    serverPlayer.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING), false))
                    .getDimensionTransition();
            serverPlayer.changeDimension(destination);
        } else if (castContext.asEntityCaster() instanceof HomeOwner homeOwner && homeOwner.getHome() != null) {
            var pos = homeOwner.getHome();
            castContext.asEntityCaster().teleportTo(pos.getX(), pos.getY() + 0.15, pos.getZ());
        }
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of((casterRef, data, activeCast) -> {
            if (casterRef.get() instanceof LivingEntity living) {
                ambientParticles(living);
            }
        });
    }

    public static void ambientParticles(LivingEntity entity) {
        float f = entity.tickCount * 0.125f;
        Vec3 trail1 = new Vec3(Mth.cos(f), Mth.sin(f * 2), Mth.sin(f)).normalize();
        Vec3 trail2 = new Vec3(Mth.sin(f), Mth.cos(f * 2), Mth.cos(f)).normalize();
        Vec3 trail3 = trail1.multiply(trail2).normalize().scale(1f + (Mth.sin(f) + Mth.cos(f)) * 0.5f);
        Vec3 pos = entity.getBoundingBox().getCenter();
        entity.level().addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail1.x, pos.y + trail1.y, pos.z + trail1.z, 0, 0, 0);
        entity.level().addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail2.x, pos.y + trail2.y, pos.z + trail2.z, 0, 0, 0);
        entity.level().addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + trail3.x, pos.y + trail3.y, pos.z + trail3.z, 0, 0, 0);
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.stop();
    }
}
