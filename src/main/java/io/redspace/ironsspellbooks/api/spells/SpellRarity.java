package io.redspace.ironsspellbooks.api.spells;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum SpellRarity implements StringRepresentable {
    COMMON(0),
    UNCOMMON(1),
    RARE(2),
    EPIC(3),
    LEGENDARY(4)/*,
    MYTHIC(5),
    ANCIENT(6)*/;

    private final int value;
    public static final Codec<SpellRarity> CODEC = StringRepresentable.fromEnum(SpellRarity::values);

    SpellRarity(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return this.value;
    }

    public MutableComponent getDisplayName() {
        return DISPLAYS[getValue()];
    }

    private static List<Double> rawRarityConfig;
    private static List<Double> rarityConfig = null;

    public static List<Double> getRawRarityConfig() {
        if (rarityConfig == null) {
            rawRarityConfig = SpellRarity.getRawRarityConfigInternal();
        }
        return rawRarityConfig;
    }

    private static List<Double> getRawRarityConfigInternal() {
        var fromConfig = (List<Double>) ServerConfigs.RARITY_CONFIG.get();

        if (fromConfig.size() != 5) {
            var configDefault = (List<Double>) ServerConfigs.RARITY_CONFIG.getDefault();
            IronsSpellbooks.LOGGER.info("INVALID RARITY CONFIG FOUND (Size != 5): {} FALLING BACK TO DEFAULT: {}", fromConfig, configDefault);
            return configDefault;
        }

        if (fromConfig.stream().mapToDouble(a -> a).sum() != 1) {
            var configDefault = (List<Double>) ServerConfigs.RARITY_CONFIG.getDefault();
            IronsSpellbooks.LOGGER.info("INVALID RARITY CONFIG FOUND (Values must add up to 1): {} FALLING BACK TO DEFAULT: {}", fromConfig, configDefault);
            return configDefault;
        }

        return fromConfig;
    }

    public static List<Double> getRarityConfig() {
        if (rarityConfig == null) {
            var counter = new AtomicDouble();
            rarityConfig = new ArrayList<>();
            getRawRarityConfig().forEach(item -> {
                rarityConfig.add(counter.addAndGet(item));
            });
        }

        return rarityConfig;
    }


    /**
     * @return Returns positive if the other is less rare, negative if it is more rare, and zero if they are equal
     */
    public int compareRarity(SpellRarity other) {
        return Integer.compare(this.getValue(), other.getValue());
    }

    public static void rarityTest() {
        var sb = new StringBuilder();
        SpellRegistry.REGISTRY.forEach(s -> {
            sb.append(String.format("\nSpellType:%s\n", s));
            sb.append(String.format("\tMinRarity:%s, MaxRarity:%s\n", s.getMinRarity(), s.getMaxRarity()));
            sb.append(String.format("\tMinLevel:%s, MaxLevel:%s\n", s.getMinLevel(), s.getMaxLevel()));
            sb.append(String.format("\tRawRarityConfig:%s\n", getRawRarityConfig().stream().map(Object::toString).collect(Collectors.joining(","))));
            sb.append(String.format("\tRarityConfig:%s\n", getRarityConfig().stream().map(Object::toString).collect(Collectors.joining(","))));

            for (int i = s.getMinLevel(); i <= s.getMaxLevel(); i++) {
                List<Double> rarityConfig = getRawRarityConfig();
                double d = i / (double) s.getMaxLevel();
                int start = s.getMinRarity();
                int end = s.getMaxRarity();
                List<Double> modifiedRarityBrackets = rarityConfig.subList(start, end + 1);
                double total = modifiedRarityBrackets.stream().mapToDouble(a -> a).sum();
                double current = 0;
                SpellRarity rarity = null;
                for (int j = 0; j < modifiedRarityBrackets.size(); j++) {
                    current += modifiedRarityBrackets.get(j) / total;
                    if (d <= current) {
                        rarity = SpellRarity.values()[j + s.getMinRarity()];
                        break;
                    }
                }
                if (rarity == null) {
                    throw new RuntimeException();
                }

                sb.append(String.format("\t\tLevel %s -> %s\n", i, s.getRarity(i)));
                sb.append(String.format("\t\tTESTL %s -> %s\n", i, rarity));
                sb.append(String.format("\t\tEQUAL:%s\n", rarity == s.getRarity(i)));
            }

            sb.append("\n");

            for (int i = s.getMinRarity(); i <= s.getMaxRarity(); i++) {
                sb.append(String.format("\t\t%s -> Level %s\n", SpellRarity.values()[i], s.getMinLevelForRarity(SpellRarity.values()[i])));
            }
        });

        IronsSpellbooks.LOGGER.debug(sb.toString());
    }

    public ChatFormatting getChatFormatting() {
        return switch (this) {
            case COMMON -> ChatFormatting.GRAY;
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.AQUA;
            case EPIC -> ChatFormatting.LIGHT_PURPLE;
            case LEGENDARY -> ChatFormatting.GOLD;
        };
    }

    private final MutableComponent[] DISPLAYS = {
            Component.translatable("rarity.irons_spellbooks.common").withStyle(ChatFormatting.GRAY),
            Component.translatable("rarity.irons_spellbooks.uncommon").withStyle(ChatFormatting.GREEN),
            Component.translatable("rarity.irons_spellbooks.rare").withStyle(ChatFormatting.AQUA),
            Component.translatable("rarity.irons_spellbooks.epic").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("rarity.irons_spellbooks.legendary").withStyle(ChatFormatting.GOLD),
            Component.translatable("rarity.irons_spellbooks.mythic").withStyle(ChatFormatting.GOLD),
            Component.translatable("rarity.irons_spellbooks.ancient").withStyle(ChatFormatting.GOLD),
    };

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
