package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.entity.mobs.debug_wizard.DebugWizard;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;

public class CreateDebugWizardCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_debug_wizard.failed"));
    private static final SimpleCommandExceptionType ERROR_FAILED_MAX_LEVEL = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_debug_wizard.failed_max_level"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("createDebugWizard").requires((commandSourceStack) -> {
            return commandSourceStack.hasPermission(2);
        }).then(Commands.argument("spellType", EnumArgument.enumArgument(SpellType.class))
                .then(Commands.argument("spellLevel", IntegerArgumentType.integer(1))
                        .then(Commands.argument("targetsPlayer", BoolArgumentType.bool())
                                .then(Commands.argument("cancelAfterTicks", IntegerArgumentType.integer(0))
                                        .executes((ctx) -> {
                                            return createDebugWizard(
                                                    ctx.getSource(),
                                                    ctx.getArgument("spellType", SpellType.class),
                                                    IntegerArgumentType.getInteger(ctx, "spellLevel"),
                                                    BoolArgumentType.getBool(ctx, "targetsPlayer"),
                                                    IntegerArgumentType.getInteger(ctx, "cancelAfterTicks"));
                                        }))))));
    }

    private static int createDebugWizard(CommandSourceStack source, SpellType spellType, int spellLevel, boolean targetsPlayer, int cancelAfterTicks) throws CommandSyntaxException {
        if (spellLevel > spellType.getMaxLevel()) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell.failed_max_level", spellType, spellType.getMaxLevel())).create();
        }


        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            var debugWizard = new DebugWizard(EntityRegistry.DEBUG_WIZARD.get(), serverPlayer.level, spellType, spellLevel, targetsPlayer, cancelAfterTicks);
            debugWizard.setPos(serverPlayer.position());
            if (serverPlayer.level.addFreshEntity(debugWizard)) {
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }
}
