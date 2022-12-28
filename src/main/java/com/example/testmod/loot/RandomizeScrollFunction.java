package com.example.testmod.loot;

import com.example.testmod.TestMod;
import com.example.testmod.item.AbstractScroll;
import com.example.testmod.item.FireballScroll;
import com.example.testmod.registries.LootRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.List;

//should extend lootmodifer?
public class RandomizeScrollFunction extends LootItemConditionalFunction {
    final NumberProvider levelRange;
    protected RandomizeScrollFunction(LootItemCondition[] lootConditions,NumberProvider levelRange) {
        super(lootConditions);
        this.levelRange=levelRange;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if(itemStack.getItem() instanceof AbstractScroll scroll){
            TestMod.LOGGER.info("loot table function running");
            TestMod.LOGGER.info("number provided: "+levelRange.getInt(lootContext));
//            scroll.setSpellType(SpellType.FIREBALL_SPELL);
//            scroll.setLevel(5);
//            scroll.getScrollData(itemStack).setSpell(AbstractSpell.getSpell());
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

            return new RandomizeScrollFunction(lootConditions,numberProvider);
        }
    }
}
