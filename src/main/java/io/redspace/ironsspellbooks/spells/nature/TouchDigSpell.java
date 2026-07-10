package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class TouchDigSpell extends AbstractSpell {

    private static final float DIG_DISTANCE = 8f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(0.5f)
            .build();

    public TouchDigSpell() {
        this.baseManaCost = 15;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 0;
    }

    private record HarvestData(TagKey<Block> cantHarvest, String descriptionId) {
        private static final HarvestData NETHERITE = new HarvestData(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "ui.irons_spellbooks.harvest_level.netherite");
        private static final HarvestData DIAMOND = new HarvestData(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, "ui.irons_spellbooks.harvest_level.diamond");
        private static final HarvestData IRON = new HarvestData(BlockTags.INCORRECT_FOR_IRON_TOOL, "ui.irons_spellbooks.harvest_level.iron");
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.harvest_level",
                        Component.translatable(getHarvestLevel(castContext).descriptionId)),
                Component.translatable("ui.irons_spellbooks.distance", castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f))
        );
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.TOUCH_DIG_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, DIG_DISTANCE);
    }

    private HarvestData getHarvestLevel(CastContext castContext) {
        var spellPower = getSpellPower(castContext);
        if (spellPower >= 15) {
            return HarvestData.NETHERITE;
        } else if (spellPower >= 13) {
            return HarvestData.DIAMOND;
        } else {
            return HarvestData.IRON;
        }
    }

    private boolean canBreak(Level level, BlockPos blockPos, CastContext castContext) {
        BlockState blockState = level.getBlockState(blockPos);
        return blockState.getDestroySpeed(level, blockPos) >= 0
                && !blockState.is(getHarvestLevel(castContext).cantHarvest);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        if (caster instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED)));
            return false;
        }

        BlockHitResult blockHitResult = raycastBlock(castContext);
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            if (caster instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("ui.irons_spellbooks.cast_error_target_block").withStyle(ChatFormatting.RED)));
            }
            return false;
        }
        if (!canBreak(castContext.level(), blockHitResult.getBlockPos(), castContext)) {
            if (caster instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("ui.irons_spellbooks.cast_error_harvest_level").withStyle(ChatFormatting.RED)));
            }
            return false;
        }
        castContext.set(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT, blockHitResult);
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (!(castContext.getOrNull(SkillcastingComponentTypes.HIT_RESULT_TRANSIENT) instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) {
            return;
        }

        Vec3 vec = blockHit.getLocation();
        Vec3 particle = castContext.position(PositionAnchor.CASTING_POSITION).subtract(0, 0.1, 0);
        int count = (int) vec.distanceTo(particle) * 2;
        for (int i = 0; i < count; i++) {
            Vec3 pos = vec.add(particle.subtract(vec).scale((double) i / count));
            MagicManager.spawnParticles(level, ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
        }
        MagicManager.spawnParticles(level, ParticleTypes.CRIT, vec.x, vec.y, vec.z, 25, 0, 0, 0, 0.2, false);

        if (canBreak(level, blockHit.getBlockPos(), castContext)) {
            var entity = castContext.asEntityCaster();

            if (!(entity instanceof ServerPlayer serverPlayer)
                    || !CommonHooks.fireBlockBreak(
                    level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer,
                    blockHit.getBlockPos(), level.getBlockState(blockHit.getBlockPos())).isCanceled()) {
                doDestroyBlock(level, blockHit.getBlockPos(), entity);
            }
        }
    }

    private static BlockHitResult raycastBlock(CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, DIG_DISTANCE);
        Vec3 start = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 end = start.add(castContext.direction().scale(range));
        CollisionContext collisionContext = caster == null ? CollisionContext.empty() : CollisionContext.of(caster);
        return castContext.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, collisionContext));
    }

    private static void doDestroyBlock(Level level, BlockPos pos, @Nullable Entity entity) {
        BlockState blockstate = level.getBlockState(pos);
        if (!blockstate.isAir()) {
            FluidState fluidstate = level.getFluidState(pos);
            if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
                level.levelEvent(2001, pos, Block.getId(blockstate));
            }
            ItemStack toolStack = entity instanceof LivingEntity livingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY;
            BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(blockstate, level, pos, blockentity, entity, toolStack);
            if (level.setBlock(pos, fluidstate.createLegacyBlock(), 3)) {
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(entity, blockstate));
            }
        }
    }
}
