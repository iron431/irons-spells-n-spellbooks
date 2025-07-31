package io.redspace.ironsspellbooks.loot;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpellFilter {
    SchoolType schoolType = null;
    List<AbstractSpell> spells = new ArrayList<>();
    static final LazyOptional<List<AbstractSpell>> DEFAULT_SPELLS = LazyOptional.of(() -> SpellRegistry.REGISTRY.get().getValues().stream().filter(AbstractSpell::allowLooting).toList());
    static final LazyOptional<Map<SchoolType, List<AbstractSpell>>> SPELLS_FOR_SCHOOL = LazyOptional.of(() -> SchoolRegistry.REGISTRY.get().getValues().stream().collect(Collectors.toMap((school -> school), (school -> SpellRegistry.getSpellsForSchool(school).stream().filter(AbstractSpell::allowLooting).toList()))));

    public SpellFilter(SchoolType schoolType) {
        this.schoolType = schoolType;
    }

    public SpellFilter(List<AbstractSpell> spells) {
        this.spells = spells;
    }

    public SpellFilter() {
    }

    public boolean isFiltered() {
        return schoolType != null || !spells.isEmpty();
    }

    public List<AbstractSpell> getApplicableSpells() {
        if (!spells.isEmpty()) {
            return spells;
        } else if (schoolType != null) {
            var spells = SPELLS_FOR_SCHOOL.resolve().get().get(schoolType);
            if (!spells.isEmpty()) {
                return spells;
            }
        } else {
            var spells = DEFAULT_SPELLS.resolve().get();
            if (!spells.isEmpty()) {
                return spells;
            }
        }
        return List.of(SpellRegistry.none());
    }

    public AbstractSpell getRandomSpell(RandomSource random, Predicate<AbstractSpell> filter) {
        var spells = getApplicableSpells().stream().filter(filter).toList();
        if(spells.isEmpty()){
            return SpellRegistry.none();
        }
        return spells.get(random.nextInt(spells.size()));
    }

    public AbstractSpell getRandomSpell(RandomSource randomSource) {
        return getRandomSpell(randomSource, (spell -> spell.isEnabled() && spell != SpellRegistry.none() && spell.allowLooting()));
    }

    public static SpellFilter deserializeSpellFilter(JsonObject json) {
        if (GsonHelper.isValidNode(json, "school")) {
            var schoolType = GsonHelper.getAsString(json, "school");
            return new SpellFilter(SchoolRegistry.getSchool(ResourceLocation.parse(schoolType)));
        } else if (GsonHelper.isArrayNode(json, "spells")) {
            var spellsFromJson = GsonHelper.getAsJsonArray(json, "spells");
            List<AbstractSpell> applicableSpellList = new ArrayList<>();
            for (JsonElement element : spellsFromJson) {
                String spellId = element.getAsString();

                var spell = SpellRegistry.getSpell(spellId);

                if (spell != SpellRegistry.none()) {
                    applicableSpellList.add(spell);
                }
            }
            return new SpellFilter(applicableSpellList);
        } else {
            return new SpellFilter();
        }
    }

    public void serialize(final JsonObject json) {
        if (schoolType != null) {
            json.addProperty("school", schoolType.getId().toString());
        } else if (!spells.isEmpty()) {
            JsonArray elements = new JsonArray();

            for (AbstractSpell spell : spells) {
                elements.add(spell.getSpellId());
            }

            json.add("spells", elements);
        }
    }
}