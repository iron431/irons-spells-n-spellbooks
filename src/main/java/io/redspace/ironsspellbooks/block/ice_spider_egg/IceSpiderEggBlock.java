package io.redspace.ironsspellbooks.block.ice_spider_egg;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

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
                IronsSpellbooks.LOGGER.debug("summonSpiderAround rcc: {}", raycastCount);
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

    static int raycastCount = 0;

    private boolean summonSpiderAround(Player player) {
        //todo:profile pathfinding
        //todo: make sure not to implement a sculk-sensor-like obfuscating-with-blocks explioit that prevents spider spawns
        BlockPos center = player.blockPosition();
        Vec3 origin = player.getBoundingBox().getCenter();
        var level = player.level;
        int range = 12;
        IceSpiderEntity spider = new IceSpiderEntity(level);

        Vec3[] probeDirections = {
                new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1),
                new Vec3(0.7, 0, 0.7), new Vec3(-0.7, 0, 0.7), new Vec3(-0.7, 0, -0.7), new Vec3(0.7, 0, -0.7)
        };
        shuffle(probeDirections);
        double stepLength = 5;
        raycastCount = 0;
        for (Vec3 initialProbe : probeDirections) {
            Vec3 initialCast = initialProbe.scale(stepLength);
            BlockHitResult bhr = castRayTowardsEmptySpace(level, origin, origin.add(initialCast));
            Vec3 currentPosition = hoverAboveGround(level, bhr.getLocation());
            int maxItr = (int) (range / stepLength) + 1;
            Vec3 bias = initialCast;
            for (int i = 0; i < maxItr; i++) {
                bhr = pickFarthestRayFromRadialCascade(level, currentPosition, bias, probeDirections, stepLength, 2);
                Vec3 node = bhr.getLocation();
                if (bhr.getType() != HitResult.Type.MISS) {
                    node = node.subtract(node.subtract(currentPosition).normalize()); // back off 1 block from collision
                }
                node = hoverAboveGround(level, node);
                bias = node.subtract(currentPosition); // update bias to try to continue in the same direction
                Utils.particleTrail(level, currentPosition, node, ParticleHelper.UNSTABLE_ENDER);
                currentPosition = node;
                if (checkDistanceAchieved(currentPosition, origin, range, 1d)) {
                    if (tryMoveSpider(spider, currentPosition, bias)) {
                        level.playSound(null, spider.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 8, 1f);
                        spider.setEmergeFromGround();
                        spider.setTarget(player);
                        spider.setYRot(Utils.getAngle(currentPosition.x, currentPosition.z, origin.x, origin.z) * Mth.RAD_TO_DEG);
                        level.addFreshEntity(spider);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean tryMoveSpider(IceSpiderEntity spider, Vec3 pos, Vec3 castedFromDir) {
        var level = spider.level;
        spider.moveTo(pos);
        AABB bb = spider.getBoundingBox().inflate(1).move(0, 0.501, 0);
        if (level.noCollision(bb) && !level.containsAnyLiquid(bb)) {
            return true;
        } else {
            spider.moveTo(pos.subtract(castedFromDir.scale(0.5)));
            bb = spider.getBoundingBox();
            return level.noCollision(bb) && !level.containsAnyLiquid(bb);
        }
    }

    Vec3 hoverAboveGround(Level level, Vec3 vec3) {
        return Utils.moveToRelativeGroundLevel(level, vec3, 1, 12).add(0, 1, 0);
    }

    boolean checkDistanceAchieved(Vec3 a, Vec3 b, double range, double p) {
        return a.distanceToSqr(b) > range * range * p * p;
    }

    BlockHitResult pickFarthestRayFromRadialCascade(Level level, Vec3 origin, Vec3 bias, Vec3[] probeDirections, double stepLength, double randomness) {
        ArrayList<BlockHitResult> hits = new ArrayList<>(probeDirections.length);
        for (Vec3 dir : probeDirections) {
            hits.add(castRayTowardsEmptySpace(level, origin, origin.add(dir.scale(stepLength)).add(bias.scale(0.5)).add(Utils.getRandomVec3(randomness))));
        }
        hits.sort(Comparator.comparingDouble(hit -> hit.getLocation().distanceToSqr(origin)));
        return hits.getLast();
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
            raycastCount++;
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
        casts.sort(Comparator.comparingDouble(hit -> hit.getLocation().distanceToSqr(start)));
        return casts.getLast();
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
