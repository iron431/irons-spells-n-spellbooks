package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class LearnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("learnSpell")
                .requires((p) -> p.hasPermission(2))
                .then(Commands.literal("forget_all")
                        .executes((context) -> forgetAll(context.getSource())))
                .then(Commands.literal("learn_all")
                        .executes((context) -> learnAll(context.getSource())))
                .then(Commands.literal("learn").then(Commands.argument("spell", SpellArgument.spellArgument()).executes((commandContext) -> {
                    return learn(commandContext.getSource(), commandContext.getArgument("spell", String.class));
                })))

        );
    }

    private static int forgetAll(CommandSourceStack source) {
        MagicData.get(source.getPlayer()).getLearnedSpellData().clear();
        source.getPlayer().syncData(DataAttachmentRegistry.MAGIC_DATA);
        return 1;
    }

    private static int learnAll(CommandSourceStack source) {
        int i = 0;
        for (AbstractSpell spell : SpellRegistry.getEnabledSpells()) {
            if (spell.requiresLearning() && !spell.isLearned(source.getPlayer())) {
                MagicData.get(source.getPlayer()).getLearnedSpellData().learnedSpells.add(spell.getSkillId());
            }
        }
        source.getPlayer().syncData(DataAttachmentRegistry.MAGIC_DATA);
        return i;
    }

    private static int learn(CommandSourceStack source, String spellId) {
        if (!spellId.contains(":")) {
            spellId = IronsSpellbooks.MODID + ":" + spellId;
        }
        AbstractSkill spell = Objects.requireNonNull(SkillRegistry.get(ResourceLocation.parse(spellId)), "unknown spell: " + spellId);
        MagicData.get(source.getPlayer()).getLearnedSpellData().learnedSpells.add(spell.getSkillId());
        source.getPlayer().syncData(DataAttachmentRegistry.MAGIC_DATA);
        return 1;
    }
}
