package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class IronsSpellbooksCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ironsSpellbooks")
                .requires((p) -> p.hasPermission(3));

        registerSummonCommandChain(command);

        dispatcher.register(command);
    }

    public static void registerSummonCommandChain(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("summons")
                .then(Commands.argument("target", EntityArgument.entities())
                        .then(Commands.literal("setOwner")
                                .then(Commands.argument("owner", EntityArgument.entity())
                                        .executes(IronsSpellbooksCommand::summonSetOwner)))));

    }

    private static int summonSetOwner(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        var owner = EntityArgument.getEntity(source, "owner");
        var targets = EntityArgument.getEntities(source, "target");
        for (var entity : targets) {
            SummonManager.setOwner(entity, owner);
        }
        source.getSource().sendSuccess(() -> Component.literal(String.format("Set %s as owner for %s entities", owner.getName().getString(), targets.size())), true);
        return targets.size();
    }
}
