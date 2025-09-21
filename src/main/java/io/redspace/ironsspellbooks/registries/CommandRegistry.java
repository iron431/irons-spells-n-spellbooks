package io.redspace.ironsspellbooks.registries;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.redspace.ironsspellbooks.command.*;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLLoader;


@EventBusSubscriber()
public class CommandRegistry {
    @SuppressWarnings("unchecked")
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
        IronsSpellbooksCommand.register(commandDispatcher);

        if (!FMLLoader.isProduction()) {
            ClearSpellSelectionCommand.register(commandDispatcher);
            IronsDebugCommand.register(commandDispatcher);
            GenerateSiteData.register(commandDispatcher);
            commandDispatcher.register((LiteralArgumentBuilder<CommandSourceStack>) ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("it")).executes(source->((CommandSourceStack)source.getSource()).getPlayer().openMenu(new SimpleMenuProvider(
                    (i, inventory, player) ->
                            new InscriptionTableMenu(i, inventory, ContainerLevelAccess.NULL), Component.translatable("block.irons_spellbooks.inscription_table")
            )).orElse(0)));
        }
    }
}