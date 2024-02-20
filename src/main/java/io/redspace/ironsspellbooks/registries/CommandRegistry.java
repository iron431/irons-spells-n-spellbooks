package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.command.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;


@Mod.EventBusSubscriber()
public class CommandRegistry {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {

        var commandDispatcher = event.getDispatcher();
        var commandBuildContext = event.getBuildContext();

        CreateScrollCommand.register(commandDispatcher);
        CreateSpellBookCommand.register(commandDispatcher);
        CreateImbuedSwordCommand.register(commandDispatcher, commandBuildContext);
        CreateDebugWizardCommand.register(commandDispatcher);
        CastCommand.register(commandDispatcher);
        ManaCommand.register(commandDispatcher);
        GenerateModList.register(commandDispatcher);
        LearnCommand.register(commandDispatcher);
        ClearCooldownCommand.register(commandDispatcher);
        ClearRecastsCommand.register(commandDispatcher);

        if (!FMLLoader.isProduction()) {
            ClearSpellSelectionCommand.register(commandDispatcher);
            IronsDebugCommand.register(commandDispatcher);
        }
    }
}