package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CreateScrollCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_scroll.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("createScroll").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).then(Commands.argument("spell", SpellArgument.spellArgument()).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes((commandContext) -> {
            return createScroll(commandContext.getSource(), commandContext.getArgument("spell", String.class), IntegerArgumentType.getInteger(commandContext, "level"));
        }))));
    }

    private static int createScroll(CommandSourceStack source, String spell, int spellLevel) throws CommandSyntaxException {
        if (!spell.contains(":")) {
            spell = IronsSpellbooks.MODID + ":" + spell;
        }

        AbstractSkill abstractSpell = SpellRegistry.getSpell(ResourceLocation.parse(spell));

        if (abstractSpell == null) {
            throw ERROR_FAILED.create();
        }

        if (spellLevel > abstractSpell.getMaxLevel()) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell.failed_max_level", abstractSpell.getDisplayName(null), abstractSpell.getMaxLevel())).create();
        }

        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemStack = new ItemStack(ItemRegistry.SCROLL.get());
            ISkillContainer.set(itemStack, Scroll.createScrollContainer(new SkillData(abstractSpell, spellLevel)));
            if (serverPlayer.getInventory().add(itemStack)) {
                return 1;
            }
        }

        throw ERROR_FAILED.create();
    }
}
