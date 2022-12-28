package com.example.testmod.setup;

import com.example.testmod.capabilities.magic.data.MagicEvents;
import com.example.testmod.capabilities.scroll.data.ScrollDataEvents;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataEvents;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        //MANA
        bus.addGenericListener(Entity.class, MagicEvents::onAttachCapabilitiesPlayer);
        //bus.addListener(ManaEvents::onPlayerCloned);
        bus.addListener(MagicEvents::onRegisterCapabilities);
        bus.addListener(MagicEvents::onWorldTick);

        //SPELLBOOKS
        //bus.addGenericListener(ItemStack.class, SpellBookDataEvents::onAttachCapabilities);
        bus.addListener(SpellBookDataEvents::onRegisterCapabilities);

        //SCROLLS
        bus.addListener(ScrollDataEvents::onRegisterCapabilities);

    }

    public static void init(FMLCommonSetupEvent event) {
        Messages.register();
    }

}