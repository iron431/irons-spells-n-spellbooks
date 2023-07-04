package io.redspace.ironsspellbooks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellType;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class RandomizeSpellFunction extends LootItemConditionalFunction {
    final NumberProvider qualityRange;
    final SpellFilter applicableSpells;

    protected RandomizeSpellFunction(LootItemCondition[] lootConditions, NumberProvider qualityRange, SpellFilter spellFilter) {
        super(lootConditions);
        this.qualityRange = qualityRange;
        this.applicableSpells = spellFilter;
    }


    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        //irons_spellbooks.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof Scroll || Utils.canImbue(itemStack)) {

            var spellList = getWeightedSpellList(applicableSpells.getApplicableSpells());
            int total = spellList.floorKey(Integer.MAX_VALUE);
            SpellType spellType = SpellType.NONE_SPELL;
            if (!spellList.isEmpty()) {
                spellType = spellList.higherEntry(lootContext.getRandom().nextInt(total)).getValue();
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
            if (entry != SpellType.NONE_SPELL && entry.isEnabled()) {
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
            var applicableSpells = SpellFilter.deserializeSpellFilter(json);

            return new RandomizeSpellFunction(lootConditions, numberProvider, applicableSpells);
        }

    }
}
