package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.command.CreateDebugWizardCommand;
import com.example.testmod.command.CreateImbuedSwordCommand;
import com.example.testmod.command.CreateScrollCommand;
import com.example.testmod.command.CreateSpellBookCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber()
public class CommandRegistry {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        TestMod.LOGGER.debug("onCommandsRegister");
        CreateScrollCommand.register(event.getDispatcher());
        CreateSpellBookCommand.register(event.getDispatcher());
        CreateImbuedSwordCommand.register(event.getDispatcher(), event.getBuildContext());
        CreateDebugWizardCommand.register(event.getDispatcher());
    }
}