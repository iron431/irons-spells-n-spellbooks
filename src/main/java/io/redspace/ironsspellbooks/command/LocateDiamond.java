package io.redspace.ironsspellbooks.command;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

public class LocateDiamond {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell_book.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("locateDiamond").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).executes((commandContext) -> {
            return locateBlock(commandContext.getSource());
        }));
    }

    private static int locateBlock(CommandSourceStack source) throws CommandSyntaxException {
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
                    var blockstate = level.getChunk(blockPos).getBlockState(blockPos);

                    if (blockstate.is(Blocks.DIAMOND_ORE)) {
                        IronsSpellbooks.LOGGER.debug("Diamond Located x:{} y:{} z:{}", j, i, k);
                    }
//                    else if(!blockstate.is(Blocks.AIR)){
//                        IronsSpellbooks.LOGGER.debug("{} Located x:{} y:{} z:{}",blockstate.getBlock(), j, i, k);
//                    }
                }
            }
        }
        IronsSpellbooks.LOGGER.debug("Finished locateBlock");
        return 1;
    }
}
