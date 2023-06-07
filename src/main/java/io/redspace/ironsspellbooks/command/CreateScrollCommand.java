package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;

public class CreateScrollCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_scroll.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("createScroll").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.argument("spellType", EnumArgument.enumArgument(SpellType.class)).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes((commandContext) -> {
            return createScroll(commandContext.getSource(), commandContext.getArgument("spellType", SpellType.class), IntegerArgumentType.getInteger(commandContext, "level"));
        }))));
    }

    private static int createScroll(CommandSourceStack source, SpellType spellType, int spellLevel) throws CommandSyntaxException {
        if (spellLevel > spellType.getMaxLevel()) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell.failed_max_level", spellType, spellType.getMaxLevel())).create();
        }

        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(ItemRegistry.SCROLL.get());
            SpellData.setSpellData(itemstack, spellType, spellLevel);
            if (serverPlayer.getInventory().add(itemstack)) {
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }
}
