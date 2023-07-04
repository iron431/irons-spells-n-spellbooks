package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.command.*;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.command.ModIdArgument;

import static net.minecraftforge.registries.ForgeRegistries.COMMAND_ARGUMENT_TYPES;


@Mod.EventBusSubscriber()
public class CommandRegistry {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {

        CreateScrollCommand.register(event.getDispatcher());
        CreateSpellBookCommand.register(event.getDispatcher());
        //CreateImbuedSwordCommand.register(event.getDispatcher(), event.getBuildContext());
        CreateDebugWizardCommand.register(event.getDispatcher());
        ManaCommand.register(event.getDispatcher());
        GenerateModList.register(event.getDispatcher());
        //GenerateSiteData.register(event.getDispatcher());

    }
}