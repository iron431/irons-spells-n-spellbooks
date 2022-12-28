package com.example.testmod.loot;

import com.example.testmod.item.Scroll;
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

import java.util.List;

//should extend lootmodifer?
public class RandomizeScrollFunction extends LootItemConditionalFunction {

    protected RandomizeScrollFunction(LootItemCondition[] p_80678_) {
        super(p_80678_);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if(itemStack.getItem() instanceof Scroll scroll){
            scroll.setSpellType(SpellType.FIREBALL_SPELL);
            scroll.setLevel(5);
            scroll.getScrollData(itemStack).setSpell(AbstractSpell.getSpell());
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_SCROLL_FUNCTION.get();
    }
    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomizeScrollFunction> {
        public void serialize(JsonObject json, RandomizeScrollFunction scrollFunction, JsonSerializationContext jsc) {
            super.serialize(json, scrollFunction, jsc);
            //write scroll data here?

        }

        public RandomizeScrollFunction deserialize(JsonObject p_80450_, JsonDeserializationContext p_80451_, LootItemCondition[] p_80452_) {
            //https://github.com/mickelus/tetra/blob/aedc884203aed78bd5c71e787781cb5511d78540/src/main/java/se/mickelus/tetra/loot/ScrollDataFunction.
            //https://github.com/mickelus/tetra/blob/1e058d250dfd1c18796f6f44c69ca1e21127d057/src/main/java/se/mickelus/tetra/blocks/scroll/ScrollData.java
            return null;
        }
    }
}
