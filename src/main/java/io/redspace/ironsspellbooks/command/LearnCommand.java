package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class LearnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("learnSpell")
                .requires((p) -> p.hasPermission(2))
                .then(Commands.literal("forget_all")
                        .executes((context) -> forgetAll(context.getSource())))
                .then(Commands.literal("learn_all")
                        .executes((context) -> learnAll(context.getSource())))
                .then(Commands.literal("learn").then(Commands.argument("spell", SpellArgument.spellArgument()).executes((commandContext) -> {
                    return learn(commandContext.getSource(), commandContext.getArgument("spell", String.class));
                })))

        );
    }

    private static int forgetAll(CommandSourceStack source) {
        MagicData.getPlayerMagicData(source.getPlayer()).getSyncedData().forgetAllSpells();
        return 1;
    }

    private static int learnAll(CommandSourceStack source) {
        int i = 0;
        for (AbstractSpell spell : SpellRegistry.getEnabledSpells()) {
            if (spell.requiresLearning() && !spell.isLearned(source.getPlayer())) {
                MagicData.getPlayerMagicData(source.getPlayer()).getSyncedData().learnSpell(spell, false);
            }
        }
        MagicData.getPlayerMagicData(source.getPlayer()).getSyncedData().doSync();
        return i;
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
