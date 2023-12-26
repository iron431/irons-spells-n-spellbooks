package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronDispenserBehavior;
import io.redspace.ironsspellbooks.capabilities.magic.MagicEvents;
import io.redspace.ironsspellbooks.compat.CompatHandler;
import io.redspace.ironsspellbooks.player.CommonPlayerEvents;
import io.redspace.ironsspellbooks.registries.BiomeRegistry;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

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
        bus.addListener(CommonPlayerEvents::onPlayerRightClickItem);
        bus.addListener(CommonPlayerEvents::onUseItemStop);

        //SPELLBOOKS
        //bus.addGenericListener(ItemStack.class, SpellBookDataEvents::onAttachCapabilities);
        //bus.addListener(SpellBookDataEvents::onRegisterCapabilities);

        //SCROLLS
        //bus.addListener(ScrollDataEvents::onRegisterCapabilities);
        //bus.addGenericListener(ItemStack.class, ScrollDataEvents::onAttachCapabilitiesItemStack);

    }

    public static void init(FMLCommonSetupEvent event) {


        event.enqueueWork(() -> {
            Messages.register();
            CompatHandler.init();
            AlchemistCauldronDispenserBehavior.init();
        });
    }
}