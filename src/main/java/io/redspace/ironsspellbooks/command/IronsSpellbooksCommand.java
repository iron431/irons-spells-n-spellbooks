package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class IronsSpellbooksCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ironsSpellbooks")
                .requires((p) -> p.hasPermission(3));

        registerSummonCommandChain(command);
        registerUpgradeChain(command);
        registerInscriptionTableCommand(command);
        registerConfigConverter(command);

        dispatcher.register(command);
    }

    public static void registerSummonCommandChain(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("summons")
                .then(Commands.argument("target", EntityArgument.entities())
                        .then(Commands.literal("setOwner")
                                .then(Commands.argument("owner", EntityArgument.entity())
                                        .executes(IronsSpellbooksCommand::summonSetOwner)))));
    }

    public static void registerUpgradeChain(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("upgrade")
                .then(Commands.argument("type", ResourceKeyArgument.key(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY))
                        .executes(IronsSpellbooksCommand::upgradeHeldItem)
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(IronsSpellbooksCommand::upgradeHeldItem))
                ));
    }

    public static void registerInscriptionTableCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("it")
                .executes(source -> source.getSource().getPlayer().openMenu(new SimpleMenuProvider(
                        (i, inventory, player) ->
                                new InscriptionTableMenu(i, inventory, ContainerLevelAccess.NULL), Component.translatable("block.irons_spellbooks.inscription_table")
                )).orElse(0)));
    }

    private static int upgradeHeldItem(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        int amount = 1;
        try {
            amount = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "amount");
        } catch (Exception ignored) {
        }
        ItemStack stack = commandSourceStackCommandContext.getSource().getPlayer().getMainHandItem();
        if (stack.isEmpty()) {
            throw new RuntimeException("empty item");
        }
        ResourceKey resourcekey = commandSourceStackCommandContext.getArgument("type", ResourceKey.class);
        String slot = UpgradeUtils.getRelevantEquipmentSlot(stack);

        for (int i = 0; i < amount; i++) {
            UpgradeData.set(stack,
                    UpgradeData.getUpgradeData(stack).addUpgrade(stack, (Holder<UpgradeOrbType>) UpgradeOrbTypeRegistry.upgradeTypeRegistry(commandSourceStackCommandContext.getSource().registryAccess())
                            .getHolder(resourcekey).get(), slot)
            );
        }
        return amount;
    }

    private static int summonSetOwner(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        var owner = EntityArgument.getEntity(source, "owner");
        var targets = EntityArgument.getEntities(source, "target");
        for (var entity : targets) {
            SummonManager.setOwner(entity, owner);
        }
        source.getSource().sendSuccess(() -> Component.literal(String.format("Set %s as owner for %s entities", owner.getName().getString(), targets.size())), true);
        return targets.size();
    }

    public static void registerConfigConverter(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("convert_legacy_config")
                .executes(LegacyConfigConverter::runCommand));
    }
}
