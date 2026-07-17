package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class CastCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("cast")
                .requires((p) -> p.hasPermission(2))
                .then(Commands.argument("casters", EntityArgument.entities())
                        .then(Commands.argument("spell", SpellArgument.spellArgument())
                                        .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class)))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class), IntegerArgumentType.getInteger(context, "level"))))
                                //.then(Commands.argument("function value", FunctionArgument.functions())
                                //        .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class), FunctionArgument.getFunctions(context, "function value"))))
                        ))
        );
    }

//    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, Collection<CommandFunction> functions) {
//        int i = 0;
//
//        for (CommandFunction commandfunction : functions) {
//            i += source.getServer().getFunctions().execute(commandfunction, source.withSuppressedOutput().withMaximumPermission(2));
//        }
//        return castSpell(source, targets, spellId, i);
//    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId) {
        return castSpell(source, targets, spellId, 1);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }

        var spell = SpellRegistry.getSpell(ResourceLocation.parse(spellId));

        for (Entity target : targets) {
            SkillcastingManager.initiateCast(CasterRef.entity(target), SkillcastingManager.buildCastContext(CasterRef.entity(target), spell.holder(), spellLevel, CastSource.EMPTY));
        }
        return 1;
    }
}