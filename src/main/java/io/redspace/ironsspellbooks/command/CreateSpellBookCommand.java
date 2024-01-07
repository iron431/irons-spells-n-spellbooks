package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CreateSpellBookCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell_book.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(
                Commands.literal("createSpellBook").requires((p_138819_) -> p_138819_.hasPermission(2))
                        .then(Commands.argument("slots", IntegerArgumentType.integer(1, 20))
                                .executes((commandContext) -> crateSpellBook(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "slots")))
                                .then(Commands.literal("randomize")
                                        .executes((commandContext) -> crateRandomSpellBook(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "slots"))))));
    }

    private static int crateSpellBook(CommandSourceStack source, int slots) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(ItemRegistry.WIMPY_SPELL_BOOK.get());
            var spellContainer = ISpellContainer.create(slots, true, true);
            spellContainer.save(itemstack);

            if (serverPlayer.getInventory().add(itemstack)) {
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }

    private static int crateRandomSpellBook(CommandSourceStack source, int slots) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(ItemRegistry.WIMPY_SPELL_BOOK.get());
            var spellContainer = ISpellContainer.create(slots, true, true);
            for (int i = 0; i < slots; i++) {
                AbstractSpell spell;
                do {
                    spell = new SpellFilter().getRandomSpell(source.getLevel().random);
                } while (!spellContainer.addSpell(spell, source.getLevel().random.nextIntBetweenInclusive(1, spell.getMaxLevel()), false, null));
            }
            spellContainer.save(itemstack);
            if (serverPlayer.getInventory().add(itemstack)) {
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }
}
