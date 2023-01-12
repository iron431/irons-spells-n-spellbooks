package com.example.testmod.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.Collection;

public class CreateScrollArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_SPELL = new DynamicCommandExceptionType((p_93342_) -> {
        return Component.translatable("commands.testmod.create_scroll.not_found", p_93342_);
    });

    public static net.minecraft.commands.arguments.EntitySummonArgument id() {
        return new net.minecraft.commands.arguments.EntitySummonArgument();
    }

    public static ResourceLocation getSummonableEntity(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        return verifyCanSummon(pContext.getArgument(pName, ResourceLocation.class));
    }

    private static ResourceLocation verifyCanSummon(ResourceLocation pId) throws CommandSyntaxException {
        Registry.ENTITY_TYPE.getOptional(pId).filter(EntityType::canSummon).orElseThrow(() -> {
            return ERROR_UNKNOWN_SPELL.create(pId);
        });
        return pId;
    }

    public ResourceLocation parse(StringReader pReader) throws CommandSyntaxException {
        return verifyCanSummon(ResourceLocation.read(pReader));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}