package io.redspace.skillcasting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.resolver.DirectionResolver;
import io.redspace.skillcasting.data.resolver.FixedPositionResolver;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class CastCommand {
    private static final Dynamic3CommandExceptionType ERROR_NO_BLOCK_ENTITY = new Dynamic3CommandExceptionType((x, y, z) -> Component.translatable("commands.skillcasting.cast.no_block_entity", x, y, z));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SKILL = new DynamicCommandExceptionType(id -> Component.translatable("commands.skillcasting.cast.unknown_skill", id));
    private static final SuggestionProvider<CommandSourceStack> SKILLS = (context, builder) -> SharedSuggestionProvider.suggestResource(SkillcastingRegistries.SKILL_REGISTRY.keySet(), builder);

    @FunctionalInterface
    private interface Cast {
        int run(CommandContext<CommandSourceStack> context, Holder<AbstractSkill> skill, int level, boolean absolute) throws CommandSyntaxException;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cast")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("entity")
                        .then(Commands.argument("casters", EntityArgument.entities())
                                .then(withSpell(CastCommand::castFromEntities))))
                .then(Commands.literal("block")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(withSpell(CastCommand::castFromBlock))))
                // position and facing come from the command source, so /execute positioned/rotated places it
                .then(Commands.literal("virtual")
                        .then(Commands.argument("skill", ResourceLocationArgument.id()).suggests(SKILLS)
                                .executes(context -> castVirtual(context, skill(context), 1, null))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(context -> castVirtual(context, skill(context), IntegerArgumentType.getInteger(context, "level"), null))
                                        .then(Commands.argument("owner", EntityArgument.entity())
                                                .executes(context -> castVirtual(context, skill(context), IntegerArgumentType.getInteger(context, "level"), EntityArgument.getEntity(context, "owner"))))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> withSpell(Cast cast) {
        // rotation is rasterized by the command chain; we can't tell whether it was set by the chain, relative, etc, so we rely on the "absolute" flag for whether to listen
        return Commands.argument("skill", ResourceLocationArgument.id()).suggests(SKILLS)
                .executes(context -> cast.run(context, skill(context), 1, false))
                .then(Commands.literal("absolute")
                        .executes(context -> cast.run(context, skill(context), 1, true)))
                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                        .executes(context -> cast.run(context, skill(context), IntegerArgumentType.getInteger(context, "level"), false))
                        .then(Commands.literal("absolute")
                                .executes(context -> cast.run(context, skill(context), IntegerArgumentType.getInteger(context, "level"), true))));
    }

    private static Holder<AbstractSkill> skill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "skill");
        return SkillcastingRegistries.SKILL_REGISTRY.getHolder(id).orElseThrow(() -> ERROR_UNKNOWN_SKILL.create(id));
    }

    private static int castFromEntities(CommandContext<CommandSourceStack> context, Holder<AbstractSkill> skill, int level, boolean absolute) throws CommandSyntaxException {
        int started = 0;
        for (Entity entity : EntityArgument.getEntities(context, "casters")) {
            if (cast(context.getSource(), CasterRef.entity(entity), skill, level, absolute)) {
                started++;
            }
        }
        return started;
    }

    private static int castFromBlock(CommandContext<CommandSourceStack> context, Holder<AbstractSkill> skill, int level, boolean absolute) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        BlockEntity blockEntity = context.getSource().getLevel().getBlockEntity(pos);
        if (blockEntity == null) {
            throw ERROR_NO_BLOCK_ENTITY.create(pos.getX(), pos.getY(), pos.getZ());
        }
        return cast(context.getSource(), CasterRef.block(blockEntity), skill, level, absolute) ? 1 : 0;
    }

    private static int castVirtual(CommandContext<CommandSourceStack> context, Holder<AbstractSkill> skill, int level, @Nullable Entity owner) {
        CommandSourceStack source = context.getSource();
        Vec3 direction = Vec3.directionFromRotation(source.getRotation());
        return SkillcastingUtils.castAt(source.getLevel(), source.getPosition(), direction, skill, level, CastSource.EMPTY, owner).isPresent() ? 1 : 0;
    }

    private static boolean cast(CommandSourceStack source, CasterRef caster, Holder<AbstractSkill> skill, int level, boolean absolute) {
        CastContext castContext = SkillcastingManager.buildCastContext(caster, skill, level, CastSource.EMPTY);
        if (absolute) {
            castContext.set(SkillcastingComponentTypes.POSITION_RESOLVER, new FixedPositionResolver(source.getPosition()));
            castContext.set(SkillcastingComponentTypes.DIRECTION_RESOLVER, DirectionResolver.fixedFromRotation(source.getRotation()));
        }
        return SkillcastingManager.initiateCast(caster, castContext);
    }
}
