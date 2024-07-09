package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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
                                .then(Commands.argument("function value", FunctionArgument.functions())
                                        .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", String.class), FunctionArgument.getFunctions(context, "function value"))))
                        ))
        );
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, Collection<CommandFunction> functions) {
        int i = 0;

        for (CommandFunction commandfunction : functions) {
            i += source.getServer().getFunctions().execute(commandfunction, source.withSuppressedOutput().withMaximumPermission(2));
        }
        return castSpell(source, targets, spellId, i);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId) {
        return castSpell(source, targets, spellId, 1);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, String spellId, int spellLevel) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }

        var spell = SpellRegistry.getSpell(spellId);

        for (Entity target : targets) {
            if (target instanceof ServerPlayer serverPlayer) {
                spell.attemptInitiateCast(ItemStack.EMPTY, spellLevel, source.getLevel(), serverPlayer, CastSource.COMMAND, false, "command");
            } else if (target instanceof IMagicEntity castingMob) {
                castingMob.initiateCastSpell(spell, spellLevel);
            } else if (target instanceof LivingEntity livingEntity) {
                var magicData = MagicData.getPlayerMagicData(livingEntity);

                if (!spell.checkPreCastConditions(source.getLevel(), spellLevel, livingEntity, magicData)) {
                    return 0;
                }

                //if (spell.getCastType() == CastType.INSTANT) {
                spell.onCast(source.getLevel(), spellLevel, livingEntity, CastSource.COMMAND, magicData);
                spell.onServerCastComplete(source.getLevel(), spellLevel, livingEntity, magicData, false);
                //} else {
                //    int effectiveCastTime = spell.getEffectiveCastTime(spellLevel, livingEntity);
                //    magicData.initiateCast(spell, spellLevel, effectiveCastTime, CastSource.MOB, "command");
                //    spell.onServerPreCast(source.getLevel(), spellLevel, livingEntity, magicData);
                //}
            }
        }
        return 1;
    }
}