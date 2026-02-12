package io.redspace.ironsspellbooks.command;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.block.statue.StatueBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureManager;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

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
                .then(Commands.literal("claimSummon").then(
                        Commands.argument("target", EntityArgument.entity())
                                .executes(commandContext -> {
                                    SummonManager.setOwner(EntityArgument.getEntity(commandContext, "target"), commandContext.getSource().getEntityOrException());
                                    return 1;
                                })))
                .then(Commands.literal("generateCreateRecipeCompat").executes(CreateRecipeCompatGenerator::run))
                .then(Commands.literal("statue")
                        .then(Commands.literal("generate")
                                .then(Commands.argument("textures/entity/player/wide/steve.png", ResourceLocationArgument.id())
                                        .executes(context -> {
                                            try {
                                                var resource = ResourceLocationArgument.getId(context, "textures/entity/player/wide/steve.png");
                                                if (!resource.getPath().endsWith(".png")) {
                                                    resource = resource.withSuffix(".png");
                                                }
                                                var image = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(resource).get().open());
                                                var split = resource.getPath().split("/");
                                                File file = StatueTextureManager.export(split[split.length - 1], StatueTextureManager.transformTexture(image));
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
                                                context.getSource().sendFailure(Component.literal("failure: " + e.getMessage()));
                                                return 0;
                                            }
                                        }))
                        ).then(Commands.literal("set_player").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("username", StringArgumentType.string()).executes(context -> {
                                    String username = StringArgumentType.getString(context, "username");
                                    AtomicBoolean success = new AtomicBoolean(false);
                                    AtomicReference<GameProfile> profile = new AtomicReference<>();
                                    BiConsumer<Boolean, GameProfile> callback = (b, p) -> {
                                        success.set(b);
                                        profile.set(p);
                                    };
                                    context.getSource().getServer().getProfileRepository().findProfilesByNames(new String[]{username}, new ProfileLookupCallback() {
                                        @Override
                                        public void onProfileLookupSucceeded(GameProfile profile) {
                                            callback.accept(true, profile);
                                        }

                                        @Override
                                        public void onProfileLookupFailed(String profileName, Exception exception) {
                                            callback.accept(false, null);
                                        }
                                    });
                                    if (success.get()) {
                                        var blockpos = BlockPosArgument.getBlockPos(context, "pos");
                                        if (context.getSource().getLevel().getBlockEntity(blockpos) instanceof StatueBlockEntity statue) {
                                            statue.setPlayerUuid(profile.get().getId());
                                            var state = context.getSource().getLevel().getBlockState(blockpos);
                                            context.getSource().getLevel().sendBlockUpdated(blockpos, state, state, Block.UPDATE_CLIENTS);
                                            return 1;
                                        }
                                    }
                                    return 0;
                                }
                        )))))
                .then(Commands.literal("clear_chronicle_cache").executes(cmd -> {
                    ItemRegistry.THE_CHRONICLE.get().clearCache();
                    return 1;
                })));
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
