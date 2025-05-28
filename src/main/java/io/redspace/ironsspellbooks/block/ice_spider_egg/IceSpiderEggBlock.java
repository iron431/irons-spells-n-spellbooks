package io.redspace.ironsspellbooks.block.ice_spider_egg;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
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
                level.setBlock(pos, state.setValue(EGG_FROSTED, /*false*/true), 2);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
                level.levelEvent(2001, pos, Block.getId(state));
            } else {
                level.destroyBlock(pos, false);
            }
        }
    }

    private <T> void shuffle(T[] ary) {
        Random rand = new Random();

        for (int i = ary.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            T temp = ary[i];
            ary[i] = ary[j];
            ary[j] = temp;
        }
    }

    private boolean summonSpiderAround(Player player) {
        //todo:profile pathfinding
        //todo: make sure not to implement a sculk-sensor-like obfuscating-with-blocks explioit that prevents spider spawns
        BlockPos center = player.blockPosition();
        Vec3 origin = player.getBoundingBox().getCenter();
        var level = player.level;
        int range = 48;
        var pathFinder = new PathFinder(new WalkNodeEvaluator(), range);
        var target = Set.of(center);
        IceSpiderEntity spider = new IceSpiderEntity(level);
        int maxPaths = 16;
        int maxItr = 128;
        int p = 0;
        float f = 1f;

        Vec3[] probeDirections = {
                new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1)/*,
                new Vec3(0.7, 0, 0.7), new Vec3(-0.7, 0, 0.7), new Vec3(-0.7, 0, -0.7), new Vec3(0.7, 0, -0.7)*/
        };
        double stepDistance = 6;
        double randomRange = 1;
        int itrCount = 0;
        Vec3 currentFarthest = origin;
        for (int i = 0; i < 4; i++) {
            ArrayList<Vec3> reaches = new ArrayList<>();
            shuffle(probeDirections);
            randomRange += 0.5;
            for (Vec3 direction : probeDirections) {
                Vec3 initialCast = direction.scale(stepDistance).add(Utils.getRandomVec3(randomRange));
                var bhr = castRayTowardsEmptySpace(level, origin, origin.add(initialCast));
                Vec3 result = bhr.getLocation();
                Vec3 bias = initialCast;
                for (int j = 0; j < 6; j++) {
                    ArrayList<Vec3> results = new ArrayList<>(probeDirections.length);
                    for (Vec3 probe : probeDirections) {
                        Vec3 cast = probe.scale(stepDistance).add(bias.scale(1)).add(Utils.getRandomVec3(randomRange));
                        bhr = castRayTowardsEmptySpace(level, result, result.add(cast));
                        Vec3 hit = bhr.getLocation();
                        if (bhr.getType() != HitResult.Type.MISS) {
                            hit = hit.subtract(hit.subtract(result).normalize().scale(2)); // back off edge 2 block
                        }
                        hit = Utils.moveToRelativeGroundLevel(level, hit, 1, 5);
                        results.add(hit.add(0, 1, 0));
                        Utils.particleTrail(level, result, hit, ParticleHelper.UNSTABLE_ENDER);
                        itrCount++;
                    }
//                    Vec3 resultCopy = result;
                    results.sort(Comparator.comparingDouble(vec3 -> vec3.distanceToSqr(origin)));
                    Vec3 farthestProbe = results.getLast();
                    bias = farthestProbe.subtract(result);
                    Utils.particleTrail(level, result, farthestProbe, ParticleHelper.ACID);
                    result = farthestProbe;

                }
                reaches.add(result);
            }

            reaches.sort(Comparator.comparingDouble(vec3 -> vec3.distanceToSqr(origin)));
            Vec3 farthestReach = reaches.getLast();
            if (farthestReach.distanceToSqr(origin) > range * range * .95 * 95) {
                Utils.particleTrail(level, farthestReach, origin, ParticleHelper.SNOWFLAKE);
                return true;
            } else {
                currentFarthest = farthestReach;
                reaches.clear();
            }
            range -= 8;
        }

//        for (int i = 0; i < maxItr && p < maxPaths; i++) {
//            Vec3 offset = new Vec3(level.random.nextDouble() - 0.5, 0, level.random.nextDouble() - 0.5).normalize().scale(range * f);
//
//            BlockPos potentialSpawn = BlockPos.containing(Utils.moveToRelativeGroundLevel(level,
//                    center.getCenter().add(offset.x, 0, offset.z),
//                    12
//            ));
//            spider.moveTo(potentialSpawn.getBottomCenter()); // fudge position for various uses
//            var collisionBox = spider.getBoundingBox();
//            if (level.isWaterAt(potentialSpawn) || !level.noCollision(spider, collisionBox.deflate(1.0E-7))) {
//                continue;
//            } else {
//                p++;
//                f *= .95f;
//            }
//            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(level, potentialSpawn.offset(-range, -range, -range), potentialSpawn.offset(range, range, range));
//            Path path = pathFinder.findPath(pathnavigationregion, spider, target, range, 0, 1f);
//            if (path != null && path.getEndNode() != null && path.getEndNode().asBlockPos().distManhattan(center) < 9) {
//                Vec3 finalSpawn = level.findFreePosition(spider, Shapes.create(collisionBox.inflate(1, 0, 1)), potentialSpawn.getBottomCenter(), 0, 0, 0).orElse(potentialSpawn.getBottomCenter());
//                spider.moveTo(finalSpawn);
//                spider.setTarget(player);
//                level.addFreshEntity(spider);
//                //todo:play distant spider howl sound
//                spider.playSound(SoundEvents.GENERIC_EXPLODE.value(), 4, 1);
//                ((IceSpiderNavigation) spider.getNavigation()).setPath(path);
//                for (Node node : path.nodes) {
//                    if (player.level instanceof ServerLevel serverLevel) {
//                        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()), node.x, node.y, node.z, 1, 0, 0, 0, 0);
//                    }
//                }
//                return true;
//            }
//        }
        return false;
    }

    BlockHitResult castRayTowardsEmptySpace(Level level, Vec3 start, Vec3 target) {
        boolean debugParticles = false;
        double distanceThresholdSqr = target.distanceToSqr(start) * (.75 * .75); // if this distance is achieved, the raycast was successful
        double offsetLength = start.distanceTo(target) * .25;
        Vec3[] offsets = {Vec3.ZERO, new Vec3(1, 0, 1), new Vec3(0, 1.5, 0), new Vec3(-1, 0, -1)};
        ArrayList<BlockHitResult> casts = new ArrayList<>(5);
        for (Vec3 offset : offsets) {
            Vec3 dir = target.subtract(start);
            Vec3 adjustedRay = dir;
            if (offset != Vec3.ZERO) {
                adjustedRay = dir.add(offset.scale(offsetLength));
                if (debugParticles) {
                    Utils.particleTrail(level, start.add(adjustedRay), target, ParticleHelper.UNSTABLE_ENDER);
                }
            }
            Vec3 destination = start.add(adjustedRay);
            BlockHitResult cast = level.clip(new ClipContext(start, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty()));
            if (cast.getLocation().distanceToSqr(start) >= distanceThresholdSqr) {
                if (debugParticles) {
                    Utils.particleTrail(level, start, cast.getLocation(), ParticleHelper.ACID_BUBBLE);
                }
                return cast;
            } else {
                if (debugParticles) {
                    Utils.particleTrail(level, start, cast.getLocation(), ParticleHelper.FIRE_EMITTER);
                }
                casts.add(cast);
            }
        }
        casts.sort(Comparator.comparingDouble(hit -> -hit.getLocation().distanceToSqr(start)));
        return casts.getFirst();
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
