package io.redspace.ironsspellbooks.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.io.File;

public class IronsSpellbooksCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ironsSpellbooks")
                .requires((p) -> p.hasPermission(3));

        registerSummonCommandChain(command);
        registerUpgradeChain(command);
        registerInscriptionTableCommand(command);
        registerCameraShakeCommand(command);
        registerConfigCommands(command);

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

    public static void registerCameraShakeCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("camera_shake")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0))
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                        .executes(IronsSpellbooksCommand::createCameraShake)))));
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

    private static int createCameraShake(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Vec3 pos = Vec3Argument.getVec3(source, "pos");
        double radius = DoubleArgumentType.getDouble(source, "radius");
        int ticks = IntegerArgumentType.getInteger(source, "ticks");
        CameraShakeManager.addCameraShake(new CameraShakeData(source.getSource().getLevel(), ticks, pos, (float) radius));
        return ticks;
    }

    public static void registerConfigCommands(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.then(Commands.literal("convert_legacy_config")
                .executes(LegacyConfigConverter::runCommand));
        command.then(Commands.literal("config")
                .then(Commands.literal("regenerate_example").executes(IronsSpellbooksCommand::regenerateExampleSpellConfigFile))
                .then(Commands.literal("generate_file")
                        .then(Commands.argument("spell", SpellArgument.spellArgument())
                                .then(Commands.literal("full").executes(c -> generateSpellConfigFile(c, true, false)).then(Commands.literal("override").executes(c -> generateSpellConfigFile(c, true, true))))
                                .then(Commands.literal("skeleton").executes(c -> generateSpellConfigFile(c, false, false)).then(Commands.literal("override").executes(c -> generateSpellConfigFile(c, false, true))))))
                .then(Commands.literal("list").executes(c -> {
                    SpellConfigManager.ALL_TYPES.forEach(param -> c.getSource()
                            .sendSystemMessage(Component.literal(param.key().toString())));
                    return 1;
                })));
    }

    private static int regenerateExampleSpellConfigFile(CommandContext<CommandSourceStack> context) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Pair<Boolean, File> result = SpellConfigManager.createExampleConfig(gson, SpellConfigManager.getSpellConfigDir().toPath().resolve("irons_spellbooks").resolve("example.txt").toFile());
        if (result.getFirst()) {
            context.getSource().sendSuccess(
                    () -> Component.translatable("commands.irons_spellbooks.generic.create_file",
                            Component.literal(result.getSecond().getName())
                                    .withStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, result.getSecond().getPath()))))
                    , true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("command.failed"));
            return 0;
        }
    }

    private static int generateSpellConfigFile(CommandContext<CommandSourceStack> context, boolean full, boolean override) {
        String spellid = context.getArgument("spell", String.class);
        if (!spellid.contains(":")) {
            spellid = "irons_spellbooks:" + spellid;
        }
        var source = context.getSource();
        AbstractSpell spell = SpellRegistry.getSpell(spellid);
        if (spell == SpellRegistry.none()) {
            source.sendFailure(Component.translatable("commands.irons_spellbooks.generic.unknown_spell", spellid));
            return 0;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Pair<Boolean, File> result = SpellConfigManager.generateSpellConfigFile(gson, spell, full, override);
        if (result.getFirst()) {
            source.sendSuccess(
                    () -> Component.translatable("commands.irons_spellbooks.generic.create_file",
                            Component.literal(result.getSecond().getName())
                                    .withStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, result.getSecond().getPath()))))
                    , true);
            return 1;
        } else if (result.getSecond() != null) {
            source.sendFailure(Component.translatable("commands.irons_spellbooks.config.cant_override", Component.literal(result.getSecond().getName()).withStyle(ChatFormatting.UNDERLINE)));
            return 0;
        } else {
            source.sendFailure(Component.translatable("command.failed"));
            return 0;
        }
    }
}
