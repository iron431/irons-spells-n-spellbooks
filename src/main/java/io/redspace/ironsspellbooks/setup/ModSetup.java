package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronInteraction;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.capabilities.magic.MagicEvents;
import io.redspace.ironsspellbooks.compat.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.Optional;


public class ModSetup {

    public static void setup() {
        IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(MagicEvents::onWorldTick);

        //SPELLBOOKS
        //bus.addGenericListener(ItemStack.class, SpellBookDataEvents::onAttachCapabilities);
        //bus.addListener(SpellBookDataEvents::onRegisterCapabilities);

        //SCROLLS
        //bus.addListener(ScrollDataEvents::onRegisterCapabilities);
        //bus.addGenericListener(ItemStack.class, ScrollDataEvents::onAttachCapabilitiesItemStack);

    }

    public static void init(FMLCommonSetupEvent event) {

        CompatHandler.init();

        event.enqueueWork(() -> {
                    for (Map.Entry<Item, AlchemistCauldronInteraction> entry : AlchemistCauldronTile.INTERACTIONS.entrySet()) {
                        var item = entry.getKey();
                        DispenserBlock.registerBehavior(item, new OptionalDispenseItemBehavior() {
                            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
                            final Optional<DispenseItemBehavior> oldBehavior = DispenserBlock.DISPENSER_REGISTRY.containsKey(item) ? Optional.of(DispenserBlock.DISPENSER_REGISTRY.get(item)) : Optional.empty();

                            //takeLiquid copied from the other dispenser interactions
                            private ItemStack takeLiquid(BlockSource p_123447_, ItemStack p_123448_, ItemStack p_123449_) {
                                p_123448_.shrink(1);
                                if (p_123448_.isEmpty()) {
                                    p_123447_.level().gameEvent(null, GameEvent.FLUID_PICKUP, p_123447_.pos());
                                    return p_123449_.copy();
                                } else {
                                    if (p_123447_.blockEntity().insertItem(p_123449_.copy()).getCount() < 0) {
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
                                ServerLevel serverlevel = blockSource.level();
                                BlockPos blockpos = blockSource.pos().relative(blockSource.blockEntity().getBlockState().getValue(DispenserBlock.FACING));
                                BlockState blockstate = serverlevel.getBlockState(blockpos);
                                if (serverlevel.getBlockEntity(blockpos) instanceof AlchemistCauldronTile cauldron) {
                                    var resultStack = cauldron.INTERACTIONS.get(itemStack.getItem()).interact(cauldron, blockstate, serverlevel, blockpos, itemStack);
                                    if (resultStack != null) {
                                        this.setSuccess(true);
                                        cauldron.setChanged();
                                        return this.takeLiquid(blockSource, itemStack, resultStack);
                                    }
                                }
                                return oldBehavior.map(behavior -> behavior.dispense(blockSource, itemStack)).orElse(itemStack);
                            }
                        });
                    }
                });


//            DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new OptionalDispenseItemBehavior() {
//                private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
//                final DispenseItemBehavior oldBehavior = DispenserBlock.DISPENSER_REGISTRY.get(Items.GLASS_BOTTLE);
//
//                //takeLiquid copied from the other dispenser interactions
//                private ItemStack takeLiquid(BlockSource p_123447_, ItemStack p_123448_, ItemStack p_123449_) {
//                    p_123448_.shrink(1);
//                    if (p_123448_.isEmpty()) {
//                        p_123447_.level().gameEvent(null, GameEvent.FLUID_PICKUP, p_123447_.pos());
//                        return p_123449_.copy();
//                    } else {
//                        if (p_123447_.blockEntity().insertItem(p_123449_.copy()).getCount() < 0) {
//                            this.defaultDispenseItemBehavior.dispense(p_123447_, p_123449_.copy());
//                        }
//
//                        return p_123448_;
//                    }
//                }
//
//                /**
//                 * Dispense the specified stack, play the dispense sound, and spawn particles.
//                 */
//                public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
//                    this.setSuccess(false);
//                    ServerLevel serverlevel = blockSource.level();
//                    BlockPos blockpos = blockSource.pos().relative(blockSource.blockEntity().getBlockState().getValue(DispenserBlock.FACING));
//                    BlockState blockstate = serverlevel.getBlockState(blockpos);
//                    if (serverlevel.getBlockEntity(blockpos) instanceof AlchemistCauldronTile cauldron) {
//                        var resultStack = cauldron.INTERACTIONS.get(itemStack.getItem()).interact(cauldron, blockstate, serverlevel, blockpos, itemStack);
//                        if (resultStack != null) {
//                            this.setSuccess(true);
//                            cauldron.setChanged();
//                            return this.takeLiquid(blockSource, itemStack, resultStack);
//                        }
//                    }
//                    return oldBehavior.dispense(blockSource, itemStack);
//                }
//            });
//        });
    }
}