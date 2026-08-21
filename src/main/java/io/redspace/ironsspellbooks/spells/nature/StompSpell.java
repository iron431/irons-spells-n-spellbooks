package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.StompAoe;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class StompSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(16)
            .build();

    public StompSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 10;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.distance",
                        castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f).intValue())
        );
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.EARTHQUAKE_CAST).toOpt();
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float powerMultiplier = castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        // todo: range and radius okay for this context? i think so
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 4 + castContext.getSkillLevel() * powerMultiplier);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, powerMultiplier);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Vec3 spawn = Utils.moveToRelativeGroundLevel(level,
                castContext.position(PositionAnchor.CASTING_POSITION), 2);
        BlockPos bpos = BlockPos.containing(spawn);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(bpos)).setPos(bpos),
                spawn.x, spawn.y, spawn.z, 40, 0.0D, 0.0D, 0.0D, 0.20 + 0.05F * castContext.getSkillLevel());

        float yRot = castContext.getYRot() * -Mth.RAD_TO_DEG;
        int range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f).intValue();
        StompAoe stomp = new StompAoe(level, range, yRot);
        SkillcastingUtils.attachToContext(stomp, castContext);
        stomp.moveTo(spawn);
        stomp.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        stomp.setRadius(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 1f));
        stomp.setOwner(castContext.asEntityCaster());
        level.addFreshEntity(stomp);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.STOMP;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public boolean shouldAIStopCasting(CastContext castContext, Mob mob, LivingEntity target) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
