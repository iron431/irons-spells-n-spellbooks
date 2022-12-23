package com.example.testmod.setup;

import com.example.testmod.capabilities.mana.data.ManaEvents;
import com.example.testmod.capabilities.spellbook.data.SpellBookDataEvents;
import com.example.testmod.setup.Messages;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        //MANA
        bus.addGenericListener(Entity.class, ManaEvents::onAttachCapabilitiesPlayer);
        bus.addListener(ManaEvents::onPlayerCloned);
        bus.addListener(ManaEvents::onRegisterCapabilities);
        bus.addListener(ManaEvents::onWorldTick);

        //SPELLBOOKS
        bus.addGenericListener(ItemStack.class, SpellBookDataEvents::onAttachCapabilities);
        bus.addListener(SpellBookDataEvents::onRegisterCapabilities);
    }

    public static void init(FMLCommonSetupEvent event) {
        Messages.register();
    }

}