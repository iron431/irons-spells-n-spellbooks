package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.command.CreateDebugWizardCommand;
import io.redspace.ironsspellbooks.command.CreateImbuedSwordCommand;
import io.redspace.ironsspellbooks.command.CreateScrollCommand;
import io.redspace.ironsspellbooks.command.CreateSpellBookCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber()
public class CommandRegistry {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        IronsSpellbooks.LOGGER.debug("onCommandsRegister");
        CreateScrollCommand.register(event.getDispatcher());
        CreateSpellBookCommand.register(event.getDispatcher());
        CreateImbuedSwordCommand.register(event.getDispatcher(), event.getBuildContext());
        CreateDebugWizardCommand.register(event.getDispatcher());
    }
}