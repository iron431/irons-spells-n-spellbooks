package io.redspace.ironsspellbooks.loot;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class SpellFilter {
    SchoolType schoolType = null;
    List<SpellType> spells = new ArrayList<>();

    public SpellFilter(SchoolType schoolType) {
        this.schoolType = schoolType;
    }

    public SpellFilter(List<SpellType> spells) {
        this.spells = spells;
    }

    public SpellFilter() {

    }

    public boolean isFiltered() {
        return schoolType != null || spells.size() > 0;
    }

    public List<SpellType> getApplicableSpells() {
        if (spells.size() > 0)
            return spells;
        else if (schoolType != null)
            return SpellType.getSpellsFromSchool(schoolType);
        else
            return Arrays.stream(SpellType.values()).filter((spellType) -> spellType.getSchoolType() != SchoolType.VOID).toList();

    }

    public SpellType getRandomSpell(RandomSource random, Predicate<SpellType> filter) {
        //Will throw a non fatal error if the filter empties the list
        var spells = getApplicableSpells().stream().filter(filter).toList();
        return spells.get(random.nextInt(spells.size()));
    }

    public static SpellFilter deserializeSpellFilter(JsonObject json) {
        if (GsonHelper.isValidNode(json, "school")) {
            var schoolType = GsonHelper.getAsString(json, "school");
            return switch (schoolType.toLowerCase()) {
                case "fire" -> new SpellFilter(SchoolType.FIRE);
                case "ice" -> new SpellFilter(SchoolType.ICE);
                case "lightning" -> new SpellFilter(SchoolType.LIGHTNING);
                case "ender" -> new SpellFilter(SchoolType.ENDER);
                case "evocation" -> new SpellFilter(SchoolType.EVOCATION);
                case "holy" -> new SpellFilter(SchoolType.HOLY);
                case "blood" -> new SpellFilter(SchoolType.BLOOD);
                case "void" -> new SpellFilter(SchoolType.VOID);
                default -> new SpellFilter();
            };
        } else if (GsonHelper.isArrayNode(json, "spells")) {
            var spellsFromJson = GsonHelper.getAsJsonArray(json, "spells");
            List<SpellType> applicableSpellList = new ArrayList<>();
            for (JsonElement element : spellsFromJson) {
                String spell = element.getAsString();
                for (SpellType spellType : SpellType.values()) {
                    if (spellType.getId().equalsIgnoreCase(spell))
                        applicableSpellList.add(spellType);
                }
            }
            return new SpellFilter(applicableSpellList);
        } else {
            return new SpellFilter();
//                var nonVoidSpells = new SpellType[SpellType.values().length - SpellType.getSpellsFromSchool(SchoolType.VOID).length];
//                int j = 0;
//                for (int i = 0; i < nonVoidSpells.length; i++) {
//                    if (SpellType.values()[i].getSchoolType() != SchoolType.VOID) {
//                        nonVoidSpells[j++] = SpellType.values()[i];
//                    }
//                }
//                return nonVoidSpells;
        }
    }
}