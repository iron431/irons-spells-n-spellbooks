package io.redspace.ironsspellbooks.entity.spectral_hammer;

import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SpectralHammer extends LivingEntity implements IAnimatable {

    private final int ticksToLive = 30;
    private final int doDamageTick = 13;
    private final int doAnimateTick = 20;

    private int depth = 0;
    private int radius = 0;

    private boolean didDamage = false;
    private boolean didAnimate = false;
    private int ticksAlive = 0;
    private boolean playSwingAnimation = true;
    private BlockHitResult blockHitResult;
    private float damageAmount;
    Set<BlockPos> missedBlocks = new HashSet<>();

    public SpectralHammer(EntityType<? extends SpectralHammer> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public SpectralHammer(Level levelIn, LivingEntity owner, BlockHitResult blockHitResult, int depth, int radius) {
        this(EntityRegistry.SPECTRAL_HAMMER.get(), levelIn);

        this.blockHitResult = blockHitResult;
        this.depth = depth;
        this.radius = radius;

        var xRot = owner.getXRot();
        var yRot = owner.getYRot();
        var yHeadRot = owner.getYHeadRot();

        this.setYRot(yRot);
        this.setXRot(xRot);
        this.setYBodyRot(yRot);
        this.setYHeadRot(yHeadRot);

//        IronsSpellbooks.LOGGER.debug("SpectralHammer: owner - xRot:{}, yRot:{}, yHeadRot:{}", xRot, yRot, yHeadRot);
//        IronsSpellbooks.LOGGER.debug("SpectralHammer: this - xRot:{}, yRot:{}, look:{}", this.getXRot(), this.getYRot(), this.getLookAngle());
//        IronsSpellbooks.LOGGER.debug("SpectralHammer: blockHitResult.dir:{}, damageAmount:{}", blockHitResult.getDirection(), damageAmount);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public void tick() {
        if (++ticksAlive >= ticksToLive) {
            discard();
        }

        if (ticksAlive >= doAnimateTick && !didAnimate) {
            missedBlocks.forEach(pos -> {
                FallingBlockEntity.fall(level, pos, level.getBlockState(pos));
            });
            didAnimate = true;
        }

        if (ticksAlive == doDamageTick - 2 && !didDamage) {
            var location = this.position();
            level.playSound(null, location.x, location.y, location.z, SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 2f, 1f);

        }

        if (ticksAlive >= doDamageTick && !didDamage) {
            if (blockHitResult != null && blockHitResult.getType() != HitResult.Type.MISS) {
                var blockPos = blockHitResult.getBlockPos();
                var blockState = level.getBlockState(blockPos);

                if (blockState.is(BlockTags.STONE_ORE_REPLACEABLES)) {
                    var blockCollector = getBlockCollector(blockPos, blockHitResult.getDirection(), radius, depth, new HashSet<>(), new HashSet<>());
                    collectBlocks(blockPos, blockCollector);

                    if (!blockCollector.blocksToRemove.isEmpty()) {
                        //IronsSpellbooks.LOGGER.debug("SpectralHammer.tick: origin:{}", blockCollector.origin);
                        var random = level.getRandom();
                        AtomicInteger count = new AtomicInteger();
                        blockCollector.blocksToRemove.forEach(pos -> {
                            var distance = blockCollector.origin.distManhattan(pos);
                            var missChance = random.nextFloat() * 3;
                            float pct = (distance * distance) / 100.0f;

                            if (missChance < pct) {
                                //IronsSpellbooks.LOGGER.debug("SpectralHammer.tick: missed pos:{}, dist:{}, missChance:{}, pct:{}", pos, distance, missChance, pct);
                                missedBlocks.add(pos);
                            } else {
                                if (count.incrementAndGet() % 5 == 0) {
                                    //IronsSpellbooks.LOGGER.debug("SpectralHammer.tick: remove.1 pos:{}, dist:{}, missChance:{}, pct:{}", pos, distance, missChance, pct);
                                    level.destroyBlock(pos, true);
                                } else {
                                    //IronsSpellbooks.LOGGER.debug("SpectralHammer.tick: remove.2 pos:{}, dist:{}, missChance:{}, pct:{}", pos, distance, missChance, pct);
                                    var bState = level.getBlockState(pos);
                                    Block.dropResources(bState, level, pos);
                                    level.removeBlock(pos, false);
                                }
                            }
                        });
                    }
                }
            }

            didDamage = true;
        }

        super.tick();
    }

    private void collectBlocks(BlockPos blockPos, BlockCollectorHelper bch) {
        //IronsSpellbooks.LOGGER.debug("SpectralHammer.collectBlocks: blockPos:{} checked:{} toRemove:{}", blockPos, bch.blocksChecked.size(), bch.blocksToRemove.size());

        if (bch.blocksChecked.contains(blockPos) || bch.blocksToRemove.contains(blockPos)) {
            return;
        }

        if (bch.isValidBlockToCollect(level, blockPos)) {
            //IronsSpellbooks.LOGGER.debug("SpectralHammer.collectBlocks: blockPos{} is valid", blockPos);
            bch.blocksToRemove.add(blockPos);
            collectBlocks(blockPos.above(), bch);
            collectBlocks(blockPos.below(), bch);
            collectBlocks(blockPos.north(), bch);
            collectBlocks(blockPos.south(), bch);
            collectBlocks(blockPos.east(), bch);
            collectBlocks(blockPos.west(), bch);
        } else {
            //IronsSpellbooks.LOGGER.debug("SpectralHammer.collectBlocks: blockPos{} is not valid", blockPos);
            bch.blocksChecked.add(blockPos);
        }
    }

    private BlockCollectorHelper getBlockCollector(BlockPos origin, Direction direction, int radius, int depth, Set<BlockPos> blocksToRemove, Set<BlockPos> blocksChecked) {
        int minX = origin.getX() - radius;
        int maxX = origin.getX() + radius;
        int minY = origin.getY() - radius;
        int maxY = origin.getY() + radius;
        int minZ = origin.getZ() - radius;
        int maxZ = origin.getZ() + radius;

        switch (direction) {
            case WEST -> {
                minX = origin.getX();
                maxX = origin.getX() + depth;
            }
            case EAST -> {
                minX = origin.getX() - depth;
                maxX = origin.getX();
            }
            case SOUTH -> {
                minZ = origin.getZ() - depth;
                maxZ = origin.getZ();
            }
            case NORTH -> {
                minZ = origin.getZ();
                maxZ = origin.getZ() + depth;
            }
            case UP -> {
                minY = origin.getY() - depth;
                maxY = origin.getY();
            }
            case DOWN -> {
                minY = origin.getY();
                maxY = origin.getY() + depth;
            }
        }

        return new BlockCollectorHelper(origin, direction, radius, depth, minX, maxX, minY, maxY, minZ, maxZ, blocksToRemove, blocksChecked);
    }

    private record BlockCollectorHelper(
            BlockPos origin,
            Direction originVector,
            int radius,
            int depth,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ,
            Set<BlockPos> blocksToRemove,
            Set<BlockPos> blocksChecked) {

        public boolean isValidBlockToCollect(Level level, BlockPos bp) {
            return level.getBlockState(bp).is(BlockTags.STONE_ORE_REPLACEABLES)
                    && bp.getX() >= minX
                    && bp.getX() <= maxX
                    && bp.getY() >= minY
                    && bp.getY() <= maxY
                    && bp.getZ() >= minZ
                    && bp.getZ() <= maxZ;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height * 0.6F;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @SuppressWarnings("removal")
    private final AnimationFactory factory = new AnimationFactory(this);

    @SuppressWarnings("removal")
    private final AnimationBuilder animationBuilder = new AnimationBuilder().addAnimation("hammer_swing", false);
    private final AnimationController animationController = new AnimationController(this, "controller", 0, this::predicate);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationController);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        if (event.getController().getAnimationState() == AnimationState.Stopped) {
            if (playSwingAnimation) {
                event.getController().setAnimation(animationBuilder);
                playSwingAnimation = false;
            }
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
