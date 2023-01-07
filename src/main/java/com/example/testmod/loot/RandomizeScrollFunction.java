package com.example.testmod.loot;

import com.example.testmod.TestMod;
import com.example.testmod.item.Scroll;
import com.example.testmod.registries.LootRegistry;
import com.example.testmod.spells.SpellType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

//should extend lootmodifer?
public class RandomizeScrollFunction extends LootItemConditionalFunction {
    final NumberProvider levelRange;
    private int counter = 0;

    protected RandomizeScrollFunction(LootItemCondition[] lootConditions, NumberProvider levelRange) {
        super(lootConditions);
        this.levelRange = levelRange;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        TestMod.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof Scroll scroll) {
            int spellLevel = levelRange.getInt(lootContext);
            var spellId = (++counter) % SpellType.values().length;
            scroll.setSpellType(SpellType.values()[spellId]);
            scroll.setLevel(spellLevel);

//            TestMod.LOGGER.debug("RandomizeScrollFunction.getScrollData.1");
//            var scrollData = scroll.getScrollData(itemStack);
//            scrollData.setData(spellId, spellLevel);
//            TestMod.LOGGER.debug("RandomizeScrollFunction.getScrollData.2");
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_SCROLL_FUNCTION.get();
    }

    //might not be necesary?
    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomizeScrollFunction> {
        public void serialize(JsonObject json, RandomizeScrollFunction scrollFunction, JsonSerializationContext jsonDeserializationContext) {
            super.serialize(json, scrollFunction, jsonDeserializationContext);
            //write scroll data here?
            //i dont think so

        }

        public RandomizeScrollFunction deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            //https://github.com/mickelus/tetra/blob/aedc884203aed78bd5c71e787781cb5511d78540/src/main/java/se/mickelus/tetra/loot/ScrollDataFunction.
            //https://github.com/mickelus/tetra/blob/1e058d250dfd1c18796f6f44c69ca1e21127d057/src/main/java/se/mickelus/tetra/blocks/scroll/ScrollData.java
            NumberProvider numberProvider = GsonHelper.getAsObject(json, "levels", jsonDeserializationContext, NumberProvider.class);

            return new RandomizeScrollFunction(lootConditions, numberProvider);
        }
    }
}
