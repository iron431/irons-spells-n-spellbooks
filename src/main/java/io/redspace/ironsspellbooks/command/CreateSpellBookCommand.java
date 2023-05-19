package io.redspace.ironsspellbooks.command;

import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.world.item.ItemStack;

public class CreateSpellBookCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell_book.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("createSpellBook").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.argument("slots", IntegerArgumentType.integer(1, 15)).executes((commandContext) -> {
            return crateSpellBook(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "slots"));
        })));
    }

    private static int crateSpellBook(CommandSourceStack source, int slots) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(ItemRegistry.WIMPY_SPELL_BOOK.get());
            var spellBookData = new SpellBookData(slots);
            SpellBookData.setSpellBookData(itemstack, spellBookData);
            if (serverPlayer.getInventory().add(itemstack)) {
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }
}
