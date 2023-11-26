package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Collection;

public class LearnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("learnSpell")
                .requires((p) -> p.hasPermission(2))
                .then(Commands.literal("forget")
                        .executes((context) -> forget(context.getSource())))
                .then(Commands.literal("learn").then(Commands.argument("spell", SpellArgument.spellArgument()).executes((commandContext) -> {
                    return learn(commandContext.getSource(), commandContext.getArgument("spell", String.class));
                })))

        );
    }

    private static int forget(CommandSourceStack source) {
        MagicData.getPlayerMagicData(source.getPlayer()).getSyncedData().forgetAllSpells();
        return 1;
    }

    private static int learn(CommandSourceStack source, String spellId) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        MagicData.getPlayerMagicData(source.getPlayer()).getSyncedData().learnSpell(spell);
        return 1;
    }
}
