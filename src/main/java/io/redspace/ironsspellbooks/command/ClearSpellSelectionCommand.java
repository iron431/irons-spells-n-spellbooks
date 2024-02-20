package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ClearSpellSelectionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("clearSpellSelection")
                .requires((p) -> p.hasPermission(2))
                .executes((context) -> clearCooldowns(context.getSource()))
        );
    }

    private static int clearCooldowns(CommandSourceStack source) {
        MagicData.getPlayerMagicData(source.getPlayer()).getSyncedData().setSpellSelection(new SpellSelection());
        source.sendSuccess(() -> Component.literal(String.format("Spell selection cleared for %s", source.getPlayer().toString())), true);
        return 1;
    }
}
