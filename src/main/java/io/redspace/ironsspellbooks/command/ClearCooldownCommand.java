package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ClearCooldownCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("clearCooldowns")
                .requires((p) -> p.hasPermission(2))
                .executes((context) -> clearCooldowns(context.getSource(), null))
                .then(Commands.literal("all")
                        .executes((context) -> clearCooldowns(context.getSource(), null)))
                .then(Commands.literal("player")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes((context) -> clearCooldowns(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
        );
    }

    private static int clearCooldowns(CommandSourceStack source, @Nullable Collection<ServerPlayer> targets) {
        if (targets == null || targets.isEmpty()) {
            targets = List.of(Objects.requireNonNull(source.getPlayer(), "Player required"));
        }
        targets.forEach((serverPlayer -> {
            SkillcastingData skillcastingData = SkillcastingData.get(serverPlayer);
            skillcastingData.cooldowns().clear();
            SkillcastingNetwork.syncAllCooldowns(CasterRef.entity(serverPlayer), skillcastingData);
        }));

        if (!targets.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.clearCooldown.success"), true);
        }

        return targets.size();
    }
}