package com.example.testmod.command;

import com.example.testmod.item.Scroll;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.spells.SpellType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;

public class CreateScrollCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.testmod.create_scroll.failed"));

//    public static final SuggestionProvider<CommandSourceStack> SPELL_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation(TestMod.MODID, "spell_suggestions"), (p_212438_, p_212439_) -> {
//        return SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon), p_212439_, EntityType::getKey, (p_212436_) -> {
//            return Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(p_212436_)));
//        });
//    });

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("createScroll").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.argument("spellType", EnumArgument.enumArgument(SpellType.class)).then(Commands.argument("level", IntegerArgumentType.integer(0, 10)).executes((commandContext) -> {
            return createScroll(commandContext.getSource(), commandContext.getArgument("spellType", SpellType.class), IntegerArgumentType.getInteger(commandContext, "level"));
        }))));
    }

    private static int createScroll(CommandSourceStack source, SpellType spellType, int spellLevel) throws CommandSyntaxException {
        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(ItemRegistry.SCROLL.get());
            if (itemstack.getItem() instanceof Scroll scroll) {
                var scrollData = scroll.getScrollData(itemstack);
                scrollData.setData(spellType.getValue(), spellLevel);
                if (serverPlayer.getInventory().add(itemstack)) {
                    return 1;
                }
            }
        }

        throw ERROR_FAILED.create();
    }
}
