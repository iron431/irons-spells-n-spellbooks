package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class TouchDigSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "touch_dig");

    private static final int distance = 8;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.harvest_level", Component.translatable(getHarvestLevel(getSpellPower(spellLevel, caster)).descriptionId)),
                Component.translatable("ui.irons_spellbooks.distance", distance)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(0.5)
            .build();

    public TouchDigSpell() {
        this.baseManaCost = 15;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 10; // high base so percent buffs are more likely to push the threshhold over
        this.spellPowerPerLevel = 3;
        this.castTime = 0;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.TOUCH_DIG_CAST.get());
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
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    record HarvestData(TagKey<Block> cantHarvest, String descriptionId) {
        static HarvestData NETHERITE = new HarvestData(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "ui.irons_spellbooks.harvest_level.netherite");
        static HarvestData DIAMOND = new HarvestData(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, "ui.irons_spellbooks.harvest_level.diamond");
        static HarvestData IRON = new HarvestData(BlockTags.INCORRECT_FOR_IRON_TOOL, "ui.irons_spellbooks.harvest_level.iron");
    }

    private HarvestData getHarvestLevel(double spellPower) {
        if (spellPower >= 15) {
            return HarvestData.NETHERITE;
        } else if (spellPower >= 13) {
            return HarvestData.DIAMOND;
        } else {
            return HarvestData.IRON;
        }
    }

    private boolean canBreak(Level level, BlockPos blockPos, double spellPower) {
        var blockState = level.getBlockState(blockPos);
        return blockState.getDestroySpeed(level, blockPos) >= 0 && !blockState.is(getHarvestLevel(spellPower).cantHarvest);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED)));
            return false;
        }
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, distance);
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_target_block").withStyle(ChatFormatting.RED)));
            }
            return false;
        }
        if (!canBreak(level, blockHitResult.getBlockPos(), getSpellPower(spellLevel, entity))) {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_harvest_level").withStyle(ChatFormatting.RED)));
            }
            return false;
        }
        return true;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var blockhit = Utils.getTargetBlock(world, entity, ClipContext.Fluid.NONE, distance);
        Vec3 vec = blockhit.getLocation();
        Vec3 particle = entity.getEyePosition().subtract(0, 0.1, 0);
        int count = (int) vec.distanceTo(particle) * 2;
        for (int i = 0; i < count; i++) {
            Vec3 pos = vec.add(particle.subtract(vec).scale((double) i / count));
            MagicManager.spawnParticles(world, ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, false);
        }
        MagicManager.spawnParticles(world, ParticleTypes.CRIT, vec.x, vec.y, vec.z, 25, 0, 0, 0, 0.2, false);

        if (canBreak(world, blockhit.getBlockPos(), getSpellPower(spellLevel, entity))) {
            if (!(entity instanceof ServerPlayer serverPlayer)
                    || !net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(world, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, blockhit.getBlockPos(), world.getBlockState(blockhit.getBlockPos())).isCanceled()) {
                doDestroyBlock(world, blockhit.getBlockPos(), entity);
            }
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private void doDestroyBlock(Level level, BlockPos pos, LivingEntity livingEntity) {
        BlockState blockstate = level.getBlockState(pos);
        if (!blockstate.isAir()) {
            FluidState fluidstate = level.getFluidState(pos);
            if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
                level.levelEvent(2001, pos, Block.getId(blockstate));
            }
            BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(blockstate, level, pos, blockentity, livingEntity, livingEntity.getMainHandItem());
            if (level.setBlock(pos, fluidstate.createLegacyBlock(), 3)) {
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(livingEntity, blockstate));
            }
        }
    }
}
