package io.redspace.ironsspellbooks.worldgen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.List;

public class IceSpiderPatrolSpawner implements CustomSpawner {
    private static final int DELAY_FIXED = 20 * 60 * 8;
    private static final int DELAY_VARIABLE = 20 * 60 * 3;
    private int tickDelay;

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!spawnEnemies || !ServerConfigs.ICE_SPIDER_PATROLS.get()) {
            return 0;
        }
        int playercount = level.players().size();
        if (playercount < 1) {
            return 0;
        }
        RandomSource randomsource = level.random;
        this.tickDelay--;
        if (this.tickDelay > 0) {
            return 0;
        }
        this.tickDelay = DELAY_FIXED / getGroupedPlayerCount(level) + randomsource.nextInt(DELAY_VARIABLE);
        if (!level.isRaining() || randomsource.nextBoolean()) {
            return 0;
        }

        Player player = null;
        for (int i = 0; i < playercount; i++) {
            player = level.players().get(randomsource.nextInt(playercount));
            if (!player.isSpectator() && !player.isCreative()) {
                break;
            }
            player = null;
        }
        if (player == null) {
            return 0;
        }


        if (performIceSpiderHuntSpawn(level, player, 4)) {
            return 1;
        }

        return 0;
    }

    public static boolean performIceSpiderHuntSpawn(ServerLevel level, LivingEntity targetEntity, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            if (i > 0) {
                // source biome check
                Holder<Biome> holder = level.getBiome(targetEntity.blockPosition());
                if (!holder.is(ModTags.ICE_SPIDER_PATROLS)) {
                    // allow at least 1 attempt in case we get lucky with nearby biome. otherwise, short circuit
                    return false;
                }
            }
            var randomsource = level.random;
            int k = (24 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
            int l = (24 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = targetEntity.blockPosition().mutable().move(k, 0, l);
            if (!level.hasChunksAt(
                    blockpos$mutableblockpos.getX() - 10,
                    blockpos$mutableblockpos.getZ() - 10,
                    blockpos$mutableblockpos.getX() + 10,
                    blockpos$mutableblockpos.getZ() + 10
            )) {
                break;
            }
            Holder<Biome> holder = level.getBiome(blockpos$mutableblockpos);
            if (!holder.is(ModTags.ICE_SPIDER_PATROLS)) {
                break;
            }
            blockpos$mutableblockpos.setY(
                    level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos$mutableblockpos).getY()
            );
            if (createSpider(level, blockpos$mutableblockpos, targetEntity)) {
                return true;
            }
        }
        return false;
    }

    private static int getGroupedPlayerCount(ServerLevel serverLevel) {
        List<BlockPos> groupPositions = new ArrayList<>();
        int count = 0;
        int groupRange = 48;
        for (Player player : serverLevel.players()) {
            if (groupPositions.stream().noneMatch(pos -> pos.distSqr(player.blockPosition()) < groupRange * groupRange)) {
                count++;
                groupPositions.add(player.blockPosition());
            }
        }
        return count;
    }

    private static boolean createSpider(ServerLevel level, BlockPos.MutableBlockPos pos, LivingEntity targetEntity) {
        BlockState blockstate = level.getBlockState(pos);
        if (!NaturalSpawner.isValidEmptySpawnBlock(level, pos, blockstate, blockstate.getFluidState(), EntityRegistry.ICE_SPIDER.get())) {
            return false;
        } else if (!checkPatrollingMonsterSpawnRules(EntityRegistry.ICE_SPIDER.get(), level, MobSpawnType.PATROL, pos, level.random)) {
            return false;
        }
        IceSpiderEntity iceSpider = new IceSpiderEntity(level);
        iceSpider.moveTo(pos.immutable(), 0, 0);
        iceSpider.setTarget(targetEntity);
        level.playSound(null, iceSpider.blockPosition(), SoundRegistry.ICE_SPIDER_HOWL.get(), SoundSource.HOSTILE, 4, 1f);
        iceSpider.setEmergeFromGround();
        if (!EventHooks.checkSpawnPosition(iceSpider, level, MobSpawnType.PATROL)) {
            return false;
        }
        level.addFreshEntity(iceSpider);
        iceSpider.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.PATROL, null);
        return true;
    }

    public static boolean checkPatrollingMonsterSpawnRules(
            EntityType<? extends Mob> mob, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= 8 && level.getDifficulty() != Difficulty.PEACEFUL && Monster.checkMobSpawnRules(mob, level, spawnType, pos, random);
    }
}
