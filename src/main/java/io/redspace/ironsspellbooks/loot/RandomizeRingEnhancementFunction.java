package io.redspace.ironsspellbooks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.redspace.ironsspellbooks.item.curios.CasterRing;
import io.redspace.ironsspellbooks.item.curios.RingData;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
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

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        //irons_spellbooks.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof CasterRing) {
            RingData.setRingData(itemStack, spellFilter.getRandomSpell(lootContext.getRandom(), (spellType -> spellType.isEnabled() && spellType != SpellType.NONE_SPELL)));
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
        }

        public RandomizeRingEnhancementFunction deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            var applicableSpells = SpellFilter.deserializeSpellFilter(json);
            return new RandomizeRingEnhancementFunction(lootConditions, applicableSpells);
        }
    }
}
