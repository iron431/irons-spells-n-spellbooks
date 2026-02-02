package io.redspace.ironsspellbooks.api.config;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record SpellConfigParameter<T>(ResourceLocation key, Codec<T> datatype, Supplier<T> defaultValue) {
    public SpellConfigParameter(ResourceLocation key, Codec<T> datatype, T defaultValue) {
        this(key, datatype, () -> defaultValue);
    }

    public static final SpellConfigParameter<SchoolType> SCHOOL =
            new SpellConfigParameter<>(IronsSpellbooks.id("school"), SchoolRegistry.REGISTRY.byNameCodec(), SchoolRegistry.EVOCATION);

    public static final SpellConfigParameter<SpellRarity> MIN_RARITY =
            new SpellConfigParameter<>(IronsSpellbooks.id("min_rarity"), SpellRarity.CODEC, SpellRarity.COMMON);

    public static final SpellConfigParameter<Integer> MAX_LEVEL =
            new SpellConfigParameter<>(IronsSpellbooks.id("max_level"), com.mojang.serialization.Codec.INT, 1);

    public static final SpellConfigParameter<Boolean> ENABLED =
            new SpellConfigParameter<>(IronsSpellbooks.id("enabled"), com.mojang.serialization.Codec.BOOL, true);

    public static final SpellConfigParameter<Double> COOLDOWN_IN_SECONDS =
            new SpellConfigParameter<>(IronsSpellbooks.id("cooldown_in_seconds"), com.mojang.serialization.Codec.DOUBLE, 10.0);

    public static final SpellConfigParameter<Boolean> ALLOW_CRAFTING =
            new SpellConfigParameter<>(IronsSpellbooks.id("allow_crafting"), com.mojang.serialization.Codec.BOOL, true);

    public static final SpellConfigParameter<Double> POWER_MULTIPLIER =
            new SpellConfigParameter<>(IronsSpellbooks.id("power_multiplier"), com.mojang.serialization.Codec.DOUBLE, 1.0);

    public static final SpellConfigParameter<Double> MANA_MULTIPLIER =
            new SpellConfigParameter<>(IronsSpellbooks.id("mana_cost_multiplier"), com.mojang.serialization.Codec.DOUBLE, 1.0);
}
