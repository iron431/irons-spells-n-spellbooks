package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronBlock;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronInteraction;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.capabilities.magic.MagicEvents;
import io.redspace.ironsspellbooks.compat.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;
import java.util.Optional;

public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        //PLAYER
        //bus.addListener(ClientPlayerEvents::onPlayerTick); Firing for all players
        //bus.addListener(KeyMappings::onRegisterKeybinds);
//        bus.addListener(ClientPlayerEvents::onLivingEquipmentChangeEvent);
        //bus.addListener(ClientPlayerEvents::onPlayerRenderPre);
//        bus.addListener(ClientPlayerEvents::onLivingEntityUseItemEventStart);
//        bus.addListener(ClientPlayerEvents::onLivingEntityUseItemEventTick);
//        bus.addListener(ClientPlayerEvents::onLivingEntityUseItemEventFinish);

        //MANA
        bus.addGenericListener(Entity.class, MagicEvents::onAttachCapabilitiesPlayer);
        //bus.addListener(ManaEvents::onPlayerCloned);
        bus.addListener(MagicEvents::onRegisterCapabilities);
        bus.addListener(MagicEvents::onWorldTick);

        //SPELLBOOKS
        //bus.addGenericListener(ItemStack.class, SpellBookDataEvents::onAttachCapabilities);
        //bus.addListener(SpellBookDataEvents::onRegisterCapabilities);

        //SCROLLS
        //bus.addListener(ScrollDataEvents::onRegisterCapabilities);
        //bus.addGenericListener(ItemStack.class, ScrollDataEvents::onAttachCapabilitiesItemStack);

    }

    public static void init(FMLCommonSetupEvent event) {
        Messages.register();

        CompatHandler.init();

        event.enqueueWork(() -> {
            for (Map.Entry< Item, AlchemistCauldronInteraction > entry : AlchemistCauldronTile.INTERACTIONS.entrySet()) {
            var item = entry.getKey();
            DispenserBlock.registerBehavior(item, new OptionalDispenseItemBehavior() {
                private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
                final Optional<DispenseItemBehavior> oldBehavior = DispenserBlock.DISPENSER_REGISTRY.containsKey(item) ? Optional.of(DispenserBlock.DISPENSER_REGISTRY.get(item)) : Optional.empty();

                //takeLiquid copied from the other dispenser interactions
                private ItemStack takeLiquid(BlockSource p_123447_, ItemStack p_123448_, ItemStack p_123449_) {
                    p_123448_.shrink(1);
                    if (p_123448_.isEmpty()) {
                        p_123447_.getLevel().gameEvent((Entity)null, GameEvent.FLUID_PICKUP, p_123447_.getPos());
                        return p_123449_.copy();
                    } else {
                        if (((DispenserBlockEntity)p_123447_.getEntity()).addItem(p_123449_.copy()) < 0) {
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
    }
}