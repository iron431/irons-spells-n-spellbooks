package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.server.command.EnumArgument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class IronsDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("ironsDebug").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.argument("dataType", EnumArgument.enumArgument(IronsDebugCommandTypes.class)).executes((commandContext) -> {
            return getDataForType(commandContext.getSource(), commandContext.getArgument("dataType", IronsDebugCommandTypes.class));
        })).then(Commands.literal("spellCount").executes((commandContext -> {
            int i = SpellRegistry.getEnabledSpells().size();
            commandContext.getSource().sendSuccess(()->Component.literal(String.valueOf(i)), true);
            return i;
        }))));
    }

    public static int getDataForType(CommandSourceStack source, IronsDebugCommandTypes ironsDebugCommandTypes) {
        switch (ironsDebugCommandTypes) {
            case RECASTING -> {
                getReacstingData(source);
            }
        }
        return 1;
    }

    public static void getReacstingData(CommandSourceStack source) {
        var serverPlayer = source.getPlayer();
        var magicData = MagicData.getPlayerMagicData(serverPlayer);

        writeResults(source, magicData.getPlayerRecasts().toString());
    }

    private static void writeResults(CommandSourceStack source, String results) {
        try {
            var file = new File("irons_debug.txt");
            var writer = new BufferedWriter(new FileWriter(file));
            writer.write(results);
            writer.close();

            Component component = Component.literal(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> {
                return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            });
            source.sendSuccess(()->Component.translatable("commands.irons_spellbooks.irons_debug_command.success", component), true);
        } catch (Exception ignored) {
        }
    }

    public enum IronsDebugCommandTypes {
        RECASTING
    }
}
