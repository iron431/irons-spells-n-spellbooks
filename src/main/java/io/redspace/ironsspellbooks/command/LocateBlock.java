package io.redspace.ironsspellbooks.command;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Predicate;

public class LocateBlock {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell_book.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(Commands.literal("locateBlock").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.argument("block", BlockPredicateArgument.blockPredicate(pContext)).executes((commandContext) -> {
            return locateBlock(commandContext.getSource(), BlockPredicateArgument.getBlockPredicate(commandContext, "block"));
        })));
    }

    private static int locateBlock(CommandSourceStack source, Predicate<BlockInWorld> predicate) throws CommandSyntaxException {
        var startingPosition = source.getPlayer().blockPosition();
        var level = source.getLevel();
        var xFrom = startingPosition.getX() - 200;
        var xTo = startingPosition.getX() + 200;
        var yFrom = -64;
        var yTo = startingPosition.getY();
        var zFrom = startingPosition.getZ() - 200;
        var zTo = startingPosition.getZ() + 200;

        IronsSpellbooks.LOGGER.debug("Starting locateBlock from: {}, xFrom:{} xTo:{} yFrom:{} yTo:{} zFrom:{} zTo:{}", startingPosition, xFrom, xTo, yFrom, yTo, zFrom, zTo);

        for (int i = yFrom; i < yTo; i++) {
            for (int j = xFrom; j < xTo; j++) {
                for (int k = zFrom; k < zTo; k++) {
                    var blockPos = new BlockPos(j, i, k);
                    if (predicate.test(new BlockInWorld(level, blockPos, true))) {
                        IronsSpellbooks.LOGGER.debug("Located x:{} y:{} z:{}", j, i, k);
                    }
                }
            }
        }

        IronsSpellbooks.LOGGER.debug("Finished locateBlock");
        return 1;
    }
}
