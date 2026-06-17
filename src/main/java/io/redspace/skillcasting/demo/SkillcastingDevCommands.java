package io.redspace.skillcasting.demo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.network.DebugHudTogglePacket;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Minimal {@code /skillcasting} debug command for manually exercising the vertical slice in a dev
 * environment: initiate/cancel skills from the registry and inspect state.
 */
public final class SkillcastingDevCommands {
    private static final SuggestionProvider<CommandSourceStack> SKILL_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(SkillcastingRegistries.SKILLS.keySet().stream(), builder);

    private SkillcastingDevCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var skillArg = Commands.argument("skill", ResourceLocationArgument.id()).suggests(SKILL_SUGGESTIONS);

        dispatcher.register(Commands.literal("skillcasting")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("cast")
                        .executes(ctx -> cast(ctx.getSource(), SkillRegistry.DEMO_PROJECTILE.get().getSkillId(), 1))
                        .then(skillArg
                                .executes(ctx -> cast(
                                        ctx.getSource(),
                                        ResourceLocationArgument.getId(ctx, "skill"),
                                        1))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(ctx -> cast(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "skill"),
                                                IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("cancel").executes(ctx -> cancel(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("hud").executes(ctx -> toggleHud(ctx.getSource())))
                .then(Commands.literal("refresh").executes(ctx -> refresh(ctx.getSource())))
                .then(Commands.literal("bind_demo")
                        .executes(ctx -> bindSkill(ctx.getSource(), SkillRegistry.DEMO_PROJECTILE.get().getSkillId(), 1))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(ctx -> bindSkill(
                                        ctx.getSource(),
                                        SkillRegistry.DEMO_PROJECTILE.get().getSkillId(),
                                        IntegerArgumentType.getInteger(ctx, "level")))))
                .then(Commands.literal("bind")
                        .then(skillArg
                                .executes(ctx -> bindSkill(
                                        ctx.getSource(),
                                        ResourceLocationArgument.getId(ctx, "skill"),
                                        1))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(ctx -> bindSkill(
                                                ctx.getSource(),
                                                ResourceLocationArgument.getId(ctx, "skill"),
                                                IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("cast_all")
                        .executes(ctx -> castAll(ctx.getSource(), 1))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(ctx -> castAll(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "level"))))));
    }

    private static int cast(CommandSourceStack source, ResourceLocation skillId, int level) throws CommandSyntaxException {
        Holder<AbstractSkill> holder = SkillRegistry.holder(skillId);
        if (holder == null) {
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        Vec2 rotation = source.getRotation();
        CasterRef caster = CasterRef.entity(player);
        boolean started = SkillcastingManager.attemptInitiateCast(caster, holder, level, null);
        if (started) {
            ActiveCast activeCast = SkillcastingData.get(player).getActiveCast();
            if (activeCast != null) {
                var context = activeCast.context();
                if (Math.abs(rotation.x - player.getXRot()) > 0.01 || Math.abs(rotation.y - player.getYRot()) > 0.01) {
                    context.set(
                            SkillcastingComponentTypes.DIRECTION_RESOLVER,
                            DirectionResolver.fixedFromRotation(rotation));
                }
                if (player.position().subtract(source.getPosition()).lengthSqr() >= 0.0001) {
                    context.set(
                            SkillcastingComponentTypes.POSITION_RESOLVER,
                            new PositionResolver.Fixed(source.getPosition()));
                }
            }
        }

        source.sendSuccess(
                () -> Component.literal("Cast " + skillId + " " + (started ? "started" : "rejected") + " at level " + level),
                false);
        return started ? 1 : 0;
    }

    private static int cancel(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SkillcastingManager.cancelCast(CasterRef.entity(player), CastEndReason.INTERRUPTED);
        source.sendSuccess(() -> Component.literal("Cancelled active cast (if any)"), false);
        return 1;
    }

    private static int toggleHud(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PacketDistributor.sendToPlayer(player, new DebugHudTogglePacket());
        return 1;
    }

    private static int refresh(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SkillcastingData data = SkillcastingData.get(player);
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
        source.sendSuccess(
                () -> Component.literal("Selection refreshed: " + data.selectionManager().getSkillCount() + " skills"),
                false);
        return 1;
    }

    private static int castAll(CommandSourceStack source, int level) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("Hold an item in main hand"));
            return 0;
        }

        var skills = SkillcastingRegistries.SKILLS.keySet().stream()
                .filter(id -> id.getNamespace().equals(Skillcasting.NAMESPACE))
                .sorted()
                .map(SkillcastingRegistries.SKILLS::get)
                .toList();
        if (skills.isEmpty()) {
            source.sendFailure(Component.literal("No skills registered"));
            return 0;
        }

        var container = new SkillContainer(skills.size(), true, false).mutableCopy();
        for (int i = 0; i < skills.size(); i++) {
            container.addSpellAtIndex(skills.get(i), level, i, false);
        }
        ISkillContainer.set(stack, container.toImmutable());
        var data = SkillcastingData.get(player);
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);

        int bound = skills.size();
        source.sendSuccess(
                () -> Component.literal("Bound " + bound + " skills (level " + level + ") to held item"),
                false);
        return bound;
    }

    private static int bindSkill(CommandSourceStack source, ResourceLocation skillId, int level)
            throws CommandSyntaxException {
        if (SkillRegistry.get(skillId) == null) {
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        var stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("Hold an item in main hand"));
            return 0;
        }
        var skill = SkillRegistry.get(skillId);
        if (skill == null) {
            source.sendFailure(Component.literal("Unknown skill: " + skillId));
            return 0;
        }
        var container = new SkillContainer(1, true, false).mutableCopy();
        container.addSpell(skill, level, false);
        ISkillContainer.set(stack, container.toImmutable());
        var data = SkillcastingData.get(player);
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
        source.sendSuccess(
                () -> Component.literal("Bound " + skillId + " (level " + level + ") to held item"),
                false);
        return 1;
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SkillcastingData data = SkillcastingData.get(player);
        ActiveCast active = data.getActiveCast();
        var activeSkill = active == null ? null : active.context().skill();
        int cooldown = activeSkill == null ? 0 : data.cooldowns().remainingTicks(activeSkill);
        var manager = data.selectionManager();
        var activeId = activeSkill == null ? null : activeSkill.value().getSkillId();
        source.sendSuccess(
                () -> Component.literal(
                        "Active: "
                                + (activeId == null ? "none" : activeId)
                                + "; cooldown: "
                                + cooldown
                                + " ticks; skills: "
                                + manager.getSkillCount()
                                + "; selected: "
                                + manager.getSelectionIndex()),
                false);
        return 1;
    }
}
