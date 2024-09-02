package io.redspace.ironsspellbooks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.curios.AffinityRing;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomizeRingEnhancementFunction extends LootItemConditionalFunction {
    final SpellFilter spellFilter;

    protected RandomizeRingEnhancementFunction(LootItemCondition[] lootConditions, SpellFilter spellFilter) {
        super(lootConditions);
        this.spellFilter = spellFilter;
    }

    public static LootItemConditionalFunction.Builder<?> create(final SpellFilter filter) {
        return simpleBuilder((functions) -> new RandomizeRingEnhancementFunction(functions, filter));
    }

    public static LootItemConditionalFunction.Builder<?> allSpells() {
        return simpleBuilder((functions) -> new RandomizeRingEnhancementFunction(functions, new SpellFilter()));
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        //irons_spellbooks.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof AffinityRing) {
            var spell = spellFilter.getRandomSpell(lootContext.getRandom(), (s) -> s.isEnabled() && s.getMaxLevel() > 1);
            AffinityData.setAffinityData(itemStack, spell);
            return spell == SpellRegistry.none() ? ItemStack.EMPTY : itemStack;
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_SPELL_RING_FUNCTION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomizeRingEnhancementFunction> {
        public void serialize(JsonObject json, RandomizeRingEnhancementFunction scrollFunction, JsonSerializationContext jsonDeserializationContext) {
            super.serialize(json, scrollFunction, jsonDeserializationContext);
            scrollFunction.spellFilter.serialize(json);
        }

        public RandomizeRingEnhancementFunction deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            var applicableSpells = SpellFilter.deserializeSpellFilter(json);
            return new RandomizeRingEnhancementFunction(lootConditions, applicableSpells);
        }
    }
}
