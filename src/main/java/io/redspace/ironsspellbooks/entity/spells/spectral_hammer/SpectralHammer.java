package io.redspace.ironsspellbooks.entity.spells.spectral_hammer;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.VisualFallingBlockEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SpectralHammer extends LivingEntity implements GeoEntity {

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
    private Player owner;

    public SpectralHammer(EntityType<? extends SpectralHammer> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public SpectralHammer(Level levelIn, LivingEntity owner, BlockHitResult blockHitResult, int depth, int radius) {
        this(EntityRegistry.SPECTRAL_HAMMER.get(), levelIn);

        if (owner instanceof Player player) {
            this.owner = player;
        }

        this.blockHitResult = blockHitResult;
        this.depth = depth;
        this.radius = radius;

        var xRot = blockHitResult.getDirection().getAxis().isVertical() ? 90 : 0;
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
            didAnimate = true;
        }

        if (ticksAlive == doDamageTick - 2 && !didDamage) {
            var location = this.position();
            level.playSound(null, location.x, location.y, location.z, SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 2f, random.nextIntBetweenInclusive(6, 8) * .1f);
            level.playSound(null, location.x, location.y, location.z, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.NEUTRAL, 1f, random.nextIntBetweenInclusive(6, 8) * .1f);

        }

        if (ticksAlive >= doDamageTick && !didDamage) {
            if (blockHitResult != null && blockHitResult.getType() != HitResult.Type.MISS) {
                var blockPos = blockHitResult.getBlockPos();
                var blockState = level.getBlockState(blockPos);

                if (blockState.is(ModTags.SPECTRAL_HAMMER_MINEABLE)) {
                    var blockCollector = getBlockCollector(blockPos, blockHitResult.getDirection(), radius, depth, new HashSet<>(), new HashSet<>());
                    collectBlocks(blockPos, blockCollector);

                    if (!blockCollector.blocksToRemove.isEmpty()) {
                        //IronsSpellbooks.LOGGER.debug("SpectralHammer.tick: origin:{}", blockCollector.origin);
                        var random = Utils.random;
                        AtomicInteger count = new AtomicInteger();
                        int maxPossibleStacks = (this.radius * 2) * (1 + this.radius * 2) * (this.depth + 1);
                        //TODO: using a simple container of this size may be a memory hog, and adding 1-3 items per block is going spin countless iterations through #addItem
                        // Could instead keep set of itemstacks, and update the count for each block broken, then add those to a simple container and drop them
                        SimpleContainer drops = new SimpleContainer(maxPossibleStacks);
                        blockCollector.blocksToRemove.forEach(pos -> {
                            var distance = blockCollector.origin.distManhattan(pos);
                            var missChance = random.nextFloat() * 20;
                            float pct = (distance * distance) / (100.0f * this.radius);

                            var blockstate = level.getBlockState(pos);
                            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, blockstate, owner);
                            MinecraftForge.EVENT_BUS.post(event);

                            // Handle if the event is canceled
                            if (!event.isCanceled()) {
                                boolean spawnFallingBlock = missChance < pct;
                                if (spawnFallingBlock) {
                                    var blockstateCopy = blockstate.getBlock().defaultBlockState();//withPropertiesOf(blockstate);
                                    var fallingblockentity = new VisualFallingBlockEntity(level, pos.getX(), pos.getY(), pos.getZ(), blockstateCopy, 100, true);
                                    IronsSpellbooks.LOGGER.debug("spectral hammer falling block {} {} {} {} {}", blockstateCopy, pos.getX(), pos.getZ(), pos.getZ(), fallingblockentity);
                                    //fallingblockentity.setDeltaMovement(Utils.getRandomVec3(0.05).subtract(0, 0.05, 0));
                                    level.addFreshEntity(fallingblockentity);
                                }
                                if (count.incrementAndGet() % 5 == 0 && !spawnFallingBlock) {
                                    level.destroyBlock(pos, false);
                                } else {
                                    level.removeBlock(pos, false);
                                }

                                //IronsSpellbooks.LOGGER.debug("SpectralHammer.tick: remove.2 pos:{}, dist:{}, missChance:{}, pct:{}", pos, distance, missChance, pct);
                                dropResources(blockstate, level, pos).forEach(drops::addItem);
                            }
                        });
                        Containers.dropContents(level, this.blockPosition(), drops);
                    }
                }
            }

            didDamage = true;
        }

        super.tick();
    }

    public static List<ItemStack> dropResources(BlockState pState, Level pLevel, BlockPos pos) {
        List<ItemStack> drops = new ArrayList<>();
        if (pLevel instanceof ServerLevel) {
            drops = Block.getDrops(pState, (ServerLevel) pLevel, pos, null);
            pState.spawnAfterBreak((ServerLevel) pLevel, pos, ItemStack.EMPTY, true);
        }
        return drops;
    }

    private void collectBlocks(BlockPos blockPos, BlockCollectorHelper bch) {
        var stack = new Stack<BlockPos>();
        stack.push(blockPos);

        while (!stack.isEmpty()) {
            BlockPos currentPos = stack.pop();

            if (bch.blocksChecked.contains(currentPos) || bch.blocksToRemove.contains(currentPos)) {
                continue;
            }

            if (bch.isValidBlockToCollect(level, currentPos)) {
                bch.blocksToRemove.add(currentPos);

                var tmpPos = currentPos.above();
                if (!bch.blocksChecked.contains(tmpPos) && !bch.blocksToRemove.contains(tmpPos)) {
                    stack.push(tmpPos);
                }
                tmpPos = currentPos.below();
                if (!bch.blocksChecked.contains(tmpPos) && !bch.blocksToRemove.contains(tmpPos)) {
                    stack.push(tmpPos);
                }
                tmpPos = currentPos.north();
                if (!bch.blocksChecked.contains(tmpPos) && !bch.blocksToRemove.contains(tmpPos)) {
                    stack.push(tmpPos);
                }
                tmpPos = currentPos.south();
                if (!bch.blocksChecked.contains(tmpPos) && !bch.blocksToRemove.contains(tmpPos)) {
                    stack.push(tmpPos);
                }
                tmpPos = currentPos.east();
                if (!bch.blocksChecked.contains(tmpPos) && !bch.blocksToRemove.contains(tmpPos)) {
                    stack.push(tmpPos);
                }
                tmpPos = currentPos.west();
                if (!bch.blocksChecked.contains(tmpPos) && !bch.blocksToRemove.contains(tmpPos)) {
                    stack.push(tmpPos);
                }
            } else {
                bch.blocksChecked.add(currentPos);
            }
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
            return level.getBlockState(bp).is(ModTags.SPECTRAL_HAMMER_MINEABLE)
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


    private final RawAnimation animationBuilder = RawAnimation.begin().thenPlay("hammer_swing");
    private final AnimationController animationController = new AnimationController(this, "controller", 0, this::predicate);

    private PlayState predicate(software.bernie.geckolib.core.animation.AnimationState event) {

        if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
            if (playSwingAnimation) {
                event.getController().setAnimation(animationBuilder);
                playSwingAnimation = false;
            }
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(animationController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
}
