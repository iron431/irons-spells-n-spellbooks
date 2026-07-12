package io.redspace.ironsspellbooks.command;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.redspace.ironslib.util.Color;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.network.debug.PlayPlayerAnimationPacket;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IronsDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("ironsDebug").requires((p_138819_) -> {
                    return p_138819_.hasPermission(2);
                }).then(Commands.literal("spellCount").executes((commandContext -> {
                    int i = SpellRegistry.getEnabledSpells().size();
                    commandContext.getSource().sendSuccess(() -> Component.literal(String.valueOf(i)), true);
                    return i;
                })))
                .then(Commands.literal("items").executes((commandContext -> {
                    if (commandContext.getSource().getPlayer() instanceof ServerPlayer player) {
                        player.getInventory().add(new ItemStack(ItemRegistry.DEV_CROWN.get()));
                        player.getInventory().add(new ItemStack(ItemRegistry.NETHERITE_SPELL_BOOK.get()));
                        player.getInventory().add(new ItemStack(ItemRegistry.INSCRIPTION_TABLE_BLOCK_ITEM.get()));
                    }
                    return 1;
                })))
                .then(Commands.literal("pocketDimension").then(Commands.literal("clearId").executes((commandContext -> {
                    if (commandContext.getSource().getPlayer() instanceof ServerPlayer player) {
                        PocketDimensionManager.INSTANCE.remove(player.getUUID());
                    }
                    return 1;
                }))))
                .then(Commands.literal("rarityTest").executes((commandContext -> {
                    SpellRarity.rarityTest();
                    return 1;
                })))
                .then(Commands.literal("animation").then(Commands.argument("animation", StringArgumentType.string()).executes(IronsDebugCommand::playPlayerAnimation)))
                .then(Commands.literal("generateCreateRecipeCompat").executes(CreateRecipeCompatGenerator::run))
                .then(Commands.literal("clear_chronicle_cache").executes(cmd -> {
                    ItemRegistry.THE_CHRONICLE.get().clearCache();
                    return 1;
                }))
                .then(Commands.literal("attribute_test")
                        .then(Commands.literal("give").executes(IronsDebugCommand::giveAttributeTest))
                        .then(Commands.literal("add")
                                .then(Commands.argument("attribute", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ATTRIBUTE.keySet(), builder))
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("modifier", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(AttributeModifier.Operation.values()).map(AttributeModifier.Operation::getSerializedName), builder))
                                                        .executes(IronsDebugCommand::addAttributeModifier))))))
                .then(Commands.literal("summons").then(Commands.literal("set_self_as_owner").then(
                        Commands.argument("target", EntityArgument.entity())
                                .executes(commandContext -> {
                                    SummonManager.setOwner(EntityArgument.getEntity(commandContext, "target"), commandContext.getSource().getEntityOrException());
                                    return 1;
                                })
                )).then(Commands.literal("get_owner").then(
                        Commands.argument("target", EntityArgument.entity())
                                .executes(commandContext -> {
                                    var entity = EntityArgument.getEntity(commandContext, "target");
                                    var owner = SummonManager.getOwner(entity);
                                    if (owner == null) {
                                        commandContext.getSource().sendSystemMessage(
                                                Component.literal(String.format("Entity %s has no owner", entity.getName().getString()))
                                        );
                                    } else {
                                        commandContext.getSource().sendSystemMessage(
                                                Component.literal(String.format("Entity %s has owner %s (%s)", entity.getName().getString(), owner.getName().getString(), owner.getUUID()))
                                        );
                                    }
                                    return 1;
                                })
                )))
                .then(Commands.literal("palettizer").then(Commands.argument("minecraft:textures/entity/player/wide/steve.png", ResourceLocationArgument.id()).then(Commands.argument("CSV-Hex", StringArgumentType.string()).executes(IronsDebugCommand::palettizeCommand)))));
    }

    private static final List<Holder<Attribute>> APPLY_ATTRIBUTES_TO_CONTEXT = List.of(
            AttributeRegistry.SPELL_RADIUS,
            AttributeRegistry.SPELL_RANGE,
            AttributeRegistry.SPELL_RICOCHET,
            AttributeRegistry.SPELL_PIERCING,
            AttributeRegistry.SPELL_PROJECTILE_SPEED,
            AttributeRegistry.SPELL_DAMAGE_OVER_TIME,
            AttributeRegistry.SPELL_HEALING,
            AttributeRegistry.SPELL_EFFECT_DURATION
    );

    private static int giveAttributeTest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getPlayer() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        for (Holder<Attribute> attribute : APPLY_ATTRIBUTES_TO_CONTEXT) {
            player.getInventory().add(createAttributeTestStick(attribute, false));
            player.getInventory().add(createAttributeTestStick(attribute, true));
        }

        source.sendSuccess(() -> Component.literal("Gave attribute test sticks for applyAttributesToContext"), true);
        return 1;
    }

    private static int addAttributeModifier(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getPlayer() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("Must be holding an item in the main hand"));
            return 0;
        }

        ResourceLocation attributeId = ResourceLocationArgument.getId(context, "attribute");
        var attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(attributeId);
        if (attributeHolder.isEmpty()) {
            source.sendFailure(Component.literal("Unknown attribute: " + attributeId));
            return 0;
        }

        double amount = DoubleArgumentType.getDouble(context, "amount");
        String modifierName = StringArgumentType.getString(context, "modifier");
        AttributeModifier.Operation operation = parseOperation(modifierName);
        if (operation == null) {
            source.sendFailure(Component.literal("Unknown modifier operation: " + modifierName + " (expected one of add_value, add_multiplied_base, add_multiplied_total)"));
            return 0;
        }

        Holder<Attribute> attribute = attributeHolder.get();
        ResourceLocation modifierId = IronsSpellbooks.id("debug_" + attributeId.getPath() + "_" + Long.toHexString(System.nanoTime()));
        ItemAttributeModifiers current = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers updated = current.withModifierAdded(
                attribute,
                new AttributeModifier(modifierId, amount, operation),
                EquipmentSlotGroup.MAINHAND
        );
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, updated);

        source.sendSuccess(() -> Component.literal(String.format("Added %s %s %s to held item", attributeId, amount, operation.getSerializedName())), true);
        return 1;
    }

    @Nullable
    private static AttributeModifier.Operation parseOperation(String name) {
        for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
            if (operation.getSerializedName().equalsIgnoreCase(name)) {
                return operation;
            }
        }
        return null;
    }

    private static ItemStack createAttributeTestStick(Holder<Attribute> attribute, boolean includeMultiplyTotal) {
        String attributeName = ResourceLocation.parse(attribute.getRegisteredName()).getPath();
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(
                        attribute,
                        new AttributeModifier(IronsSpellbooks.id("debug_add_" + attributeName), 5, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                );
        if (includeMultiplyTotal) {
            builder.add(
                    attribute,
                    new AttributeModifier(IronsSpellbooks.id("debug_mult_total_" + attributeName), 0.50, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                    EquipmentSlotGroup.MAINHAND
            );
        }

        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(attributeName + (includeMultiplyTotal ? " +5, +50%" : " +5"))
        );
        return stack;
    }

    private static int playPlayerAnimation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        String animation = StringArgumentType.getString(context, "animation");
        if (!animation.contains(":")) {
            animation = "irons_spellbooks:" + animation;
        }
        ResourceLocation animationId = ResourceLocation.parse(animation);

        PacketDistributor.sendToPlayer(player, new PlayPlayerAnimationPacket(player.getUUID(), animationId));
        source.sendSuccess(() -> Component.literal("Playing animation: " + animationId), true);
        return 1;
    }

    private static int palettizeCommand(CommandContext<CommandSourceStack> context) {
        try {

            var resource = ResourceLocationArgument.getId(context, "minecraft:textures/entity/player/wide/steve.png");
            if (!resource.getPath().endsWith(".png")) {
                resource = resource.withSuffix(".png");
            }
            if (!resource.getPath().endsWith(".png")) {
                resource = resource.withSuffix(".png");
            }
            var colorListString = StringArgumentType.getString(context, "CSV-Hex");
            colorListString = colorListString.replace("#", "");
            List<Integer> colors = Arrays.stream(colorListString.split(",")).map(i -> Integer.parseInt(i, 16) | 0xFF000000).toList();
            var image = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(resource).get().open());
            List<Integer> colorkey = new ArrayList<>();
            Arrays.stream(image.getPixelsRGBA()).distinct().filter(c -> !new Color(c).empty()).forEach(colorkey::add);
            colorkey.sort(Comparator.comparing(i -> new Color(i).luminance()));
            if (colors.size() != colorkey.size()) {
                context.getSource().sendFailure(Component.literal(String.format("mismatch warning: image has %s colors, palette has %d", colorkey.size(), colors.size())));
            }
            Map<Integer, Integer> colorToIndex = new HashMap<>();
            for (int i = 0; i < colorkey.size(); i++) {
                colorToIndex.put(colorkey.get(i), i);
            }
            image = image.mappedCopy(c -> {
                if (new Color(c).empty()) {
                    return 0;
                }
                if (!colorToIndex.containsKey(c)) {
                    return c;
                }
                int i = colorToIndex.get(c);
                if (i >= colors.size()) {
                    return c;
                }
                return new Color(colors.get(i)).toRgba();
            });
            String[] split = resource.getPath().split("/");
            String filename = split[split.length - 1];
            File file = export(filename, image);
            if (file == null) {
                context.getSource().sendFailure(Component.literal("failure"));
                return 0;
            } else {
                context.getSource().sendSuccess(() -> Component.literal("success").withStyle(Style.EMPTY.withUnderlined(true).withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())
                )), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
        }
        return 0;
    }

    @Nullable
    public static File export(String name, NativeImage image) {
        try {
            if (!name.endsWith(".png")) {
                name = name + ".png";
            }

            Path path = Path.of("screenshots/irons_spellbooks").resolve(name);
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
            }

            File file = path.toFile();
            image.writeToFile(file);
            return file;
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.debug(e.getMessage());
            return null;
        }
    }
}
