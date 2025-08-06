package io.redspace.ironsspellbooks.config;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.Holder;

import java.util.Optional;

public record SpellConfigParameters(
        Optional<Boolean> enabled,
        Optional<Boolean> canBeCrafted,
        Optional<Boolean> canBeLooted,
        Optional<Integer> maxLevel,
        Optional<SpellRarity> minRarity,
        Optional<Double> powerMultiplier,
        Optional<Double> manaCostMultiplier,
        Optional<Double> cooldownSeconds,
        Optional<Holder<SchoolType>> school
) {
    public static final SpellConfigParameters EMPTY = new SpellConfigParameters(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );
}
