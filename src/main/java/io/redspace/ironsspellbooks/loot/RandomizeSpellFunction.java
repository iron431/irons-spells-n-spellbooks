package io.redspace.ironsspellbooks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.*;

public class RandomizeSpellFunction extends LootItemConditionalFunction {
    final NumberProvider qualityRange;
    final List<SpellType> applicableSpells;

    protected RandomizeSpellFunction(LootItemCondition[] lootConditions, NumberProvider qualityRange, List<SpellType> applicableSpells) {
        super(lootConditions);
        this.qualityRange = qualityRange;
        this.applicableSpells = applicableSpells;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        //irons_spellbooks.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof Scroll || itemStack.getItem() instanceof SwordItem) {

            var spellList = getWeightedSpellList(applicableSpells);
            int total = spellList.floorKey(Integer.MAX_VALUE);
            SpellType spellType = SpellType.NONE_SPELL;
            for (int i = 0; i < 16; i++) {
                spellType = spellList.higherEntry(lootContext.getRandom().nextInt(total)).getValue();
                if (spellType.isEnabled())
                    break;
            }

            var spellId = spellType.getValue();
            int maxLevel = spellType.getMaxLevel();
            float quality = qualityRange.getFloat(lootContext);
            //https://www.desmos.com/calculator/ablc1wg06w
            quality = quality * (float) Math.sin(1.57 * quality * quality);
            int spellLevel = 1 + Math.round(quality * (maxLevel - 1));
            SpellData.setSpellData(itemStack, spellId, spellLevel);
        }
        return itemStack;
    }

    private NavigableMap<Integer, SpellType> getWeightedSpellList(List<SpellType> entries) {
        int total = 0;
        NavigableMap<Integer, SpellType> weightedSpells = new TreeMap<>();

        for (SpellType entry : entries) {
            if (entry != SpellType.NONE_SPELL) {
                total += getWeightFromRarity(SpellRarity.values()[entry.getMinRarity()]);
                weightedSpells.put(total, entry);
            }
        }

        return weightedSpells;
    }

    private int getWeightFromRarity(SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> 40;
            case UNCOMMON -> 30;
            case RARE -> 15;
            case EPIC -> 8;
            case LEGENDARY -> 4;
        };
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_SPELL_FUNCTION.get();
    }

    //might not be necesary?
    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomizeSpellFunction> {
        public void serialize(JsonObject json, RandomizeSpellFunction scrollFunction, JsonSerializationContext jsonDeserializationContext) {
            super.serialize(json, scrollFunction, jsonDeserializationContext);
            //write spell data here?
            //i dont think so

        }

        public RandomizeSpellFunction deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            //https://github.com/mickelus/tetra/blob/aedc884203aed78bd5c71e787781cb5511d78540/src/main/java/se/mickelus/tetra/loot/ScrollDataFunction.
            //https://github.com/mickelus/tetra/blob/1e058d250dfd1c18796f6f44c69ca1e21127d057/src/main/java/se/mickelus/tetra/blocks/scroll/ScrollData.java

            //Quality Range
            NumberProvider numberProvider = GsonHelper.getAsObject(json, "quality", jsonDeserializationContext, NumberProvider.class);

            //Spell Selection
            var applicableSpells = getApplicableSpells(json);


            return new RandomizeSpellFunction(lootConditions, numberProvider, applicableSpells);
        }

        private List<SpellType > getApplicableSpells(JsonObject json) {
            if (GsonHelper.isValidNode(json, "school")) {
                var schoolType = GsonHelper.getAsString(json, "school");
                return switch (schoolType.toLowerCase()) {
                    case "fire" -> SpellType.getSpellsFromSchool(SchoolType.FIRE);
                    case "ice" -> SpellType.getSpellsFromSchool(SchoolType.ICE);
                    case "lightning" -> SpellType.getSpellsFromSchool(SchoolType.LIGHTNING);
                    case "ender" -> SpellType.getSpellsFromSchool(SchoolType.ENDER);
                    case "evocation" -> SpellType.getSpellsFromSchool(SchoolType.EVOCATION);
                    case "holy" -> SpellType.getSpellsFromSchool(SchoolType.HOLY);
                    case "blood" -> SpellType.getSpellsFromSchool(SchoolType.BLOOD);
                    case "void" -> SpellType.getSpellsFromSchool(SchoolType.VOID);
                    default -> List.of(SpellType.NONE_SPELL);
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
                return applicableSpellList;
            } else {
                return Arrays.stream(SpellType.values()).filter((spellType) -> spellType.getSchoolType() != SchoolType.VOID).toList();
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
}
