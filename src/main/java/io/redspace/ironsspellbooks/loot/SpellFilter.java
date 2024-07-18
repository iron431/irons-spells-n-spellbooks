package io.redspace.ironsspellbooks.loot;


import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpellFilter {
    SchoolType schoolType = null;
    List<AbstractSpell> spells = new ArrayList<>();
    static final Cache<List<AbstractSpell>> DEFAULT_SPELLS = new Cache<>(() -> SpellRegistry.REGISTRY.stream().filter(AbstractSpell::allowLooting).toList());
    static final Cache<Map<SchoolType, List<AbstractSpell>>> SPELLS_FOR_SCHOOL = new Cache<>(() -> SchoolRegistry.REGISTRY.stream().collect(Collectors.toMap((school -> school), (school -> SpellRegistry.getSpellsForSchool(school).stream().filter(AbstractSpell::allowLooting).toList()))));

    public SpellFilter(SchoolType schoolType) {
        this.schoolType = schoolType;
    }

    public SpellFilter(List<AbstractSpell> spells) {
        this.spells = spells;
    }

    public SpellFilter() {
    }

    private static final Codec<SpellFilter> SCHOOL_CODEC = ResourceLocation.CODEC.fieldOf("school").xmap(resourceLocation -> new SpellFilter(SchoolRegistry.getSchool(resourceLocation)), spellFilter -> spellFilter.schoolType.getId()).codec();
    private static final Codec<SpellFilter> SPELLS_CODEC = Codec.list(ResourceLocation.CODEC).fieldOf("spells").xmap(resourceLocation -> new SpellFilter(resourceLocation.stream().filter(r -> SpellRegistry.getSpell(r) != null).map(SpellRegistry::getSpell).toList()), spellFilter -> ((SpellFilter) spellFilter).spells.stream().map(AbstractSpell::getSpellResource).toList()).codec();
    private static final Codec<SpellFilter> NO_FILTER_CODEC = Codec.unit(new SpellFilter());
    public static final Codec<SpellFilter> CODEC = Codec.withAlternative(SCHOOL_CODEC, SPELLS_CODEC);

    public boolean isFiltered() {
        return schoolType != null || !spells.isEmpty();
    }

    public List<AbstractSpell> getApplicableSpells() {
        if (!spells.isEmpty()) {
            return spells;
        } else if (schoolType != null) {
            var spells = SPELLS_FOR_SCHOOL.get().get(schoolType);
            if (!spells.isEmpty()) {
                return spells;
            }
        } else {
            var spells = DEFAULT_SPELLS.get();
            if (!spells.isEmpty()) {
                return spells;
            }
        }

        return List.of(SpellRegistry.none());
    }

    public AbstractSpell getRandomSpell(RandomSource random, Predicate<AbstractSpell> filter) {
        var spells = getApplicableSpells().stream().filter(filter).toList();
        if (spells.isEmpty()) {
            return SpellRegistry.none();
        }
        return spells.get(random.nextInt(spells.size()));
    }

    public AbstractSpell getRandomSpell(RandomSource randomSource) {
        return getRandomSpell(randomSource, (spell -> spell.isEnabled() && spell != SpellRegistry.none() && spell.allowLooting()));
    }

    static class Cache<T> {
        Cache(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        Supplier<T> supplier;
        T value;

        T get() {
            if (value == null) {
                value = supplier.get();
            }
            return value;
        }
    }
}