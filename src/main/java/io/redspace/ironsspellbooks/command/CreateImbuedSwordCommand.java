package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;

import java.util.stream.Collectors;

public class CreateImbuedSwordCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_imbued_sword.failed"));

    private static final SuggestionProvider<CommandSourceStack> SWORD_SUGGESTIONS = (p_180253_, p_180254_) -> {
        var resources = ForgeRegistries.ITEMS.getEntries().stream()
                .filter((k) -> k instanceof SwordItem)
                .map(x -> Registries.ITEM.location())
                .collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(resources, p_180254_);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext context) {

        pDispatcher.register(Commands.literal("createImbuedSword").requires((commandSourceStack) -> {
            return commandSourceStack.hasPermission(2);
        }).then(Commands.argument("item", ItemArgument.item(context)).suggests(SWORD_SUGGESTIONS)
                .then(Commands.argument("spellType", EnumArgument.enumArgument(SpellType.class))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1)).executes((ctx) -> {
                            return createImbuedSword(ctx.getSource(), ctx.getArgument("item", ItemInput.class), ctx.getArgument("spellType", SpellType.class), IntegerArgumentType.getInteger(ctx, "level"));
                        })))));
    }

    private static int createImbuedSword(CommandSourceStack source, ItemInput itemInput, SpellType spellType, int spellLevel) throws CommandSyntaxException {
        if (spellLevel > spellType.getMaxLevel()) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell.failed_max_level", spellType, spellType.getMaxLevel())).create();
        }

        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(itemInput.getItem());
            if (itemstack.getItem() instanceof SwordItem) {
                SpellData.setSpellData(itemstack, spellType, spellLevel);
                if (serverPlayer.getInventory().add(itemstack)) {
                    return 1;
                }
            }
        }

        throw ERROR_FAILED.create();
    }
}
