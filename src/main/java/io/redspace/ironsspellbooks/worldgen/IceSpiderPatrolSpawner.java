package io.redspace.ironsspellbooks.worldgen;

import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class IceSpiderPatrolSpawner implements CustomSpawner {
    private static final int DELAY_FIXED = 20 * 60 * 4;
    private static final int DELAY_VARIABLE = 20 * 60 * 1;
    private int tickDelay;

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!spawnEnemies) {
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
        this.tickDelay = (DELAY_FIXED + randomsource.nextInt(DELAY_VARIABLE)) / playercount;
        if (!level.isRaining() || randomsource.nextInt(3) != 0) {
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

        int k = (24 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
        int l = (24 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = player.blockPosition().mutable().move(k, 0, l);
        if (!level.hasChunksAt(
                blockpos$mutableblockpos.getX() - 10,
                blockpos$mutableblockpos.getZ() - 10,
                blockpos$mutableblockpos.getX() + 10,
                blockpos$mutableblockpos.getZ() + 10
        )) {
            return 0;
        }
        Holder<Biome> holder = level.getBiome(blockpos$mutableblockpos);
        if (!holder.is(ModTags.ICE_SPIDER_PATROLS)) {
            return 0;
        }
        blockpos$mutableblockpos.setY(
                level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos$mutableblockpos).getY()
        );
        IceSpiderEntity iceSpider = new IceSpiderEntity(level);
        iceSpider.moveTo(blockpos$mutableblockpos.immutable(), 0, 0);
        iceSpider.setTarget(player);
        level.playSound(null, iceSpider.blockPosition(), SoundRegistry.ICE_SPIDER_HOWL.get(), SoundSource.HOSTILE, 4, 1f);
        iceSpider.setEmergeFromGround();
        level.addFreshEntity(iceSpider);
        return 1;
    }
}
