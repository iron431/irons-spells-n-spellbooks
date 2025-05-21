package io.redspace.ironsspellbooks.block.ice_spider_egg;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IceSpiderEggBlock extends Block {
    public static final MapCodec<IceSpiderEggBlock> CODEC = simpleCodec(IceSpiderEggBlock::new);

    public static final BooleanProperty EGG_FROSTED = BooleanProperty.create("frosted");

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void playerDestroy(Level level, @NotNull Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (state.is(BlockRegistry.ICE_SPIDER_EGG)) {
            boolean isFrosted = state.getValue(EGG_FROSTED);
            if (isFrosted && summonSpiderAround(player)) {
                //todo: better handling for bad spider spawn?
                level.setBlock(pos, state.setValue(EGG_FROSTED, false), 2);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
                level.levelEvent(2001, pos, Block.getId(state));
            } else {
                level.destroyBlock(pos, false);
            }
        }
    }

    private boolean summonSpiderAround(Player player) {
        //todo:profile pathfinding
        BlockPos center = player.blockPosition();
        var level = player.level;
        int range = 24;
        var pathFinder = new PathFinder(new WalkNodeEvaluator(), range);
        var target = Set.of(center);
        IceSpiderEntity spider = new IceSpiderEntity(level);
        for (int i = 0; i < 8; i++) {
            Vec3 offset = new Vec3(level.random.nextDouble() - 0.5, 0, level.random.nextDouble() - 0.5).normalize().scale(range);

            BlockPos potentialSpawn = BlockPos.containing(Utils.moveToRelativeGroundLevel(level,
                    center.getCenter().add(offset.x, 0, offset.z),
                    12
            ));

            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(level, potentialSpawn.offset(-range, -range, -range), potentialSpawn.offset(range, range, range));
            Path path = pathFinder.findPath(pathnavigationregion, spider, target, range, 0, 1f);
            if (path != null) {
                spider.moveTo(potentialSpawn.getBottomCenter());
                spider.setTarget(player);
                level.addFreshEntity(spider);
                //todo:play distant spider howl sound
                return true;
            }
        }
        return false;
    }

    public IceSpiderEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EGG_FROSTED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(EGG_FROSTED);
    }
}
