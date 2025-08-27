package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.ActualMagicData;
import io.redspace.skillcastingapi.registry.SkillRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;

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
        ActualMagicData.get(source.getPlayer()).getLearnedSpellData().forgetAllSpells();
        //todo: sync
        return 1;
    }

    private static int learn(CommandSourceStack source, String spellId) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }
        ActualMagicData.get(source.getPlayer()).getLearnedSpellData().learnSpell(SkillRegistry.REGISTRY.get(ResourceLocation.parse(spellId)));
        //todo: sync
        return 1;
    }
}
