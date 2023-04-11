package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenerateSpellDataCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.generate_spell_data.failed"));

    private static final String SPELL_DATA_TEMPLATE = """
            - name: "%s"
              school: "%s"
              icon: "%s"
              level: "%d to %d"
              mana: "%d to %d"
              cooldown: "%ds"
              cast_type: "%s"
              rarity: "%s to %s"
              description: "%s"
              u1: "%s"
              u2: "%s"
              u3: "%s"
              u4: "%s"
              
                    """;

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("generateSpellData").requires((p_138819_) -> {
            return p_138819_.hasPermission(2);
        }).executes((commandContext) -> {
            return generateSpellData(commandContext.getSource());
        }));
    }

    private static int generateSpellData(CommandSourceStack source) throws CommandSyntaxException {
        try {
            var sb = new StringBuilder();

            Arrays.stream(SpellType.values())
                    .filter(st -> (st.isEnabled() && st != SpellType.NONE_SPELL))
                    .forEach(spellType -> {
                        var spellMin = AbstractSpell.getSpell(spellType, spellType.getMinLevel());
                        var spellMax = AbstractSpell.getSpell(spellType, spellType.getMaxLevel());

                        var uniqueInfo = spellMin.getUniqueInfo(null);
                        var u1 = uniqueInfo.size() >= 1 ? uniqueInfo.get(0).getString() : "";
                        var u2 = uniqueInfo.size() >= 2 ? uniqueInfo.get(1).getString() : "";
                        var u3 = uniqueInfo.size() >= 3 ? uniqueInfo.get(2).getString() : "";
                        var u4 = uniqueInfo.size() >= 4 ? uniqueInfo.get(3).getString() : "";

                        sb.append(String.format(SPELL_DATA_TEMPLATE,
                                handleCapitalization(spellType.name()),
                                handleCapitalization(spellType.getSchoolType().name()),
                                String.format("../img/spells/%s.png", spellType.getId()),
                                spellType.getMinLevel(),
                                spellType.getMaxLevel(),
                                spellMin.getManaCost(),
                                spellMax.getManaCost(),
                                spellMin.getSpellCooldown(),
                                handleCapitalization(spellType.getCastType().name()),
                                handleCapitalization(spellMin.getRarity().name()),
                                handleCapitalization(spellMax.getRarity().name()),
                                Component.translatable(String.format("%s.guide", spellType.getComponentId())).getString(),
                                u1,
                                u2,
                                u3,
                                u4)
                        );
                    });

            var file = new BufferedWriter(new FileWriter("spell_data.yml"));
            file.write(sb.toString());
            file.close();

            return 1;

        } catch (Exception e) {
        }

        throw ERROR_FAILED.create();
    }

    public static String handleCapitalization(String input) {
        return Arrays.stream(input.toLowerCase().split("[ |_]"))
                .map(word -> {
                    if (word.equals("spell")) {
                        return "";
                    } else {
                        var first = word.substring(0, 1);
                        var rest = word.substring(1);
                        return first.toUpperCase() + rest;
                    }
                })
                .collect(Collectors.joining(" "))
                .trim();
    }
}
