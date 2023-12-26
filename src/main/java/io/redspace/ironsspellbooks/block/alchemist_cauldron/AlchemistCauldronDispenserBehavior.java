package io.redspace.ironsspellbooks.block.alchemist_cauldron;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemistCauldronDispenserBehavior {
    public static void init() {
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new OptionalDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
            final DispenseItemBehavior oldBehavior = DispenserBlock.DISPENSER_REGISTRY.get(Items.GLASS_BOTTLE);

            //takeLiquid copied from the other dispenser interactions
            private ItemStack takeLiquid(BlockSource p_123447_, ItemStack p_123448_, ItemStack p_123449_) {
                p_123448_.shrink(1);
                if (p_123448_.isEmpty()) {
                    p_123447_.getLevel().gameEvent(null, GameEvent.FLUID_PICKUP, p_123447_.getPos());
                    return p_123449_.copy();
                } else {
                    if (p_123447_.<DispenserBlockEntity>getEntity().addItem(p_123449_.copy()) < 0) {
                        this.defaultDispenseItemBehavior.dispense(p_123447_, p_123449_.copy());
                    }

                    return p_123448_;
                }
            }

            /**
             * Dispense the specified stack, play the dispense sound, and spawn particles.
             */
            public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                this.setSuccess(false);
                ServerLevel serverlevel = blockSource.getLevel();
                BlockPos blockpos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
                BlockState blockstate = serverlevel.getBlockState(blockpos);
                if (AlchemistCauldronBlock.getLevel(blockstate) > 0 && serverlevel.getBlockEntity(blockpos) instanceof AlchemistCauldronTile cauldron) {
                    var resultStack = cauldron.interactions.get(itemStack.getItem()).interact(blockstate, serverlevel, blockpos, AlchemistCauldronBlock.getLevel(blockstate), itemStack);
                    if (resultStack != null) {
                        this.setSuccess(true);
                        cauldron.setChanged();
                        return this.takeLiquid(blockSource, itemStack, resultStack);
                    }
                }
                return oldBehavior.dispense(blockSource, itemStack);
            }
        });
    }
}