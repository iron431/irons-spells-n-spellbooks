package io.redspace.ironsspellbooks.command;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.redspace.ironslib.util.Color;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.SuspendedBlockEntity;
import io.redspace.ironsspellbooks.network.debug.PlayPlayerAnimationPacket;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.command.EnumArgument;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
                }).then(Commands.argument("dataType", EnumArgument.enumArgument(IronsDebugCommandTypes.class)).executes((commandContext) -> {
                    return getDataForType(commandContext.getSource(), commandContext.getArgument("dataType", IronsDebugCommandTypes.class));
                })).then(Commands.literal("spellCount").executes((commandContext -> {
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
                .then(Commands.literal("palettizer")
                        .then(Commands.argument("minecraft:textures/entity/player/wide/steve.png", ResourceLocationArgument.id())
                                .then(Commands.argument("CSV-Hex", StringArgumentType.string())
                                        .executes(IronsDebugCommand::palettizeCommand))))
                .then(Commands.literal("arcaneExplode")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 32))
                                        .executes(IronsDebugCommand::arcaneExplode))))
        );
    }

    private static int arcaneExplode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos center = BlockPosArgument.getLoadedBlockPos(context, "pos");
        int radius = IntegerArgumentType.getInteger(context, "radius");
        ServerLevel level = context.getSource().getLevel();

        int count = 0;
        Vec3 centerVec3 = center.getCenter();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) > radius * radius) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
                continue;
            }

            SuspendedBlockEntity entity = new SuspendedBlockEntity(EntityRegistry.SUSPENDED_BLOCK.get(), level);
            entity.blockState = state;
            entity.setStartPos(pos.immutable());
            entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vec3 motion = entity.position().subtract(centerVec3).scale(0.15).add(Utils.getRandomVec3(1));
            double speed = motion.length();
            motion = motion.normalize();
            speed = Mth.clamp(speed, 1, 5);
            entity.setDeltaMovement(motion.scale(speed));

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                entity.blockData = blockEntity.saveWithoutMetadata(level.registryAccess());
            }

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
            level.addFreshEntity(entity);
            count++;
        }

        int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal("Converted " + finalCount + " blocks to suspended blocks"), true);
        return count;
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

    public static int getDataForType(CommandSourceStack source, IronsDebugCommandTypes ironsDebugCommandTypes) {
        switch (ironsDebugCommandTypes) {
            case RECASTING -> {
                getReacstingData(source);
            }
        }
        return 1;
    }

    public static void getReacstingData(CommandSourceStack source) {
        var serverPlayer = source.getPlayer();
        var magicData = MagicData.getPlayerMagicData(serverPlayer);

        writeResults(source, magicData.getPlayerRecasts().toString());
    }

    private static void writeResults(CommandSourceStack source, String results) {
        try {
            var file = new File("irons_debug.txt");
            var writer = new BufferedWriter(new FileWriter(file));
            writer.write(results);
            writer.close();

            Component component = Component.literal(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> {
                return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            });
            source.sendSuccess(() -> Component.translatable("commands.irons_spellbooks.irons_debug_command.success", component), true);
        } catch (Exception ignored) {
        }
    }

    public enum IronsDebugCommandTypes {
        RECASTING
    }
}
