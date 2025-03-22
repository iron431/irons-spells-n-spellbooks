package io.redspace.ironsspellbooks.loot;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SpellFilter {
    SchoolType schoolType = null;
    List<AbstractSpell> spells = new ArrayList<>();
    final boolean force;

    static final Map<SchoolType, List<AbstractSpell>> SPELLS_FOR_SCHOOL = new HashMap<>();
    static final Map<SchoolType, List<AbstractSpell>> SPELLS_FOR_SCHOOL_FORCED = new HashMap<>();

    public SpellFilter(boolean force, SchoolType schoolType) {
        this.force = force;
        this.schoolType = schoolType;
    }

    public SpellFilter(SchoolType type) {
        this(false, type);
    }

    public SpellFilter(boolean force, List<AbstractSpell> spells) {
        this.force = force;
        this.spells = spells;
    }

    public SpellFilter(List<AbstractSpell> spells) {
        this(false, spells);
    }

    public SpellFilter() {
        this.force = false;
    }

    private static final Codec<SpellFilter> SCHOOL_CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.BOOL.optionalFieldOf("force", false).forGetter(f -> f.force),
                    SchoolRegistry.REGISTRY.byNameCodec().fieldOf("school").forGetter(f -> f.schoolType)).apply(builder, SpellFilter::new));
    private static final Codec<SpellFilter> SPELLS_CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.BOOL.optionalFieldOf("force", false).forGetter(f -> f.force),
                    Codec.list(SpellRegistry.REGISTRY.byNameCodec()).fieldOf("spells").forGetter(f -> f.spells)).apply(builder, SpellFilter::new));

    private static final Codec<SpellFilter> NO_FILTER_CODEC = Codec.unit(new SpellFilter());
    public static final Codec<SpellFilter> CODEC = Codec.withAlternative(SCHOOL_CODEC, SPELLS_CODEC);

    private boolean isSpellAllowed(AbstractSpell spell) {
        return spell.isEnabled() && (force || spell.allowLooting());
    }

    public List<AbstractSpell> getApplicableSpells() {
        if (!spells.isEmpty()) {
            return spells.stream().filter(AbstractSpell::isEnabled).toList();
        } else if (schoolType != null) {
            if (force) {
                return SPELLS_FOR_SCHOOL_FORCED.computeIfAbsent(this.schoolType,
                        school -> SpellRegistry.getSpellsForSchool(school).stream().filter(AbstractSpell::isEnabled).toList()
                );
            } else {
                return SPELLS_FOR_SCHOOL.computeIfAbsent(this.schoolType,
                        school -> SpellRegistry.getSpellsForSchool(school).stream().filter(this::isSpellAllowed).toList()
                );
            }
        } else {
            return SpellRegistry.getEnabledSpells().stream().filter(this::isSpellAllowed).toList();
        }
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
}