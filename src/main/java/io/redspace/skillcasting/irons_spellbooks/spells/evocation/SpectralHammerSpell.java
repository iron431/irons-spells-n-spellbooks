package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammer;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

public class SpectralHammerSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 1f).intValue();
        int depth = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.dimensions", 1 + radius * 2, 1 + radius * 2, depth + 1),
                Component.translatable("ui.irons_spellbooks.distance", castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 16f).intValue())
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(2)
            .build();

    public SpectralHammerSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 15;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float spellPower = getSpellPower(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 16f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, Math.max(spellPower * 0.5f, 1f));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, (int) spellPower);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        if (caster instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED)));
            return false;
        }

        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 16f);
        Vec3 start = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 end = start.add(castContext.direction().scale(range));
        CollisionContext collisionContext = caster == null ? CollisionContext.empty() : CollisionContext.of(caster);
        BlockHitResult blockHitResult = castContext.level().clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, collisionContext));

        boolean success = blockHitResult.getType() == HitResult.Type.BLOCK
                && castContext.level().getBlockState(blockHitResult.getBlockPos()).is(ModTags.SPECTRAL_HAMMER_MINEABLE);
        if (!success && caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_spectral_hammer").withStyle(ChatFormatting.RED)));
        }
        if (success) {
            castContext.set(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT, blockHitResult);
        }
        return success;
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        HitResult hitResult = castContext.getOrNull(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT);
        if (!(hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Entity caster = castContext.asEntityCaster();
        float yRot = caster != null ? caster.getYRot() : castContext.getYRot() * Mth.RAD_TO_DEG;
        float yHeadRot = caster instanceof LivingEntity living ? living.getYHeadRot() : yRot;

        Direction face = blockHitResult.getDirection();
        int radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 1f).intValue();
        int depth = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);

        SpectralHammer spectralHammer = new SpectralHammer(level, caster, blockHitResult, depth, radius, yRot, yHeadRot);
        Vec3 position = Vec3.atCenterOf(blockHitResult.getBlockPos());

        if (!face.getAxis().isVertical()) {
            Vec3 forward = castContext.direction().multiply(1, 0, 1);
            if (forward.lengthSqr() > 0) {
                position = position.subtract(0, 2, 0).subtract(forward.normalize().scale(1.5));
            }
        } else if (face == Direction.DOWN) {
            position = position.subtract(0, 3, 0);
        }

        spectralHammer.setPos(position.x, position.y, position.z);
        level.addFreshEntity(spectralHammer);
    }
}
