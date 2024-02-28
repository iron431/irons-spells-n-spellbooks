package io.redspace.ironsspellbooks.loot;

import com.google.gson.*;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.item.FurledMapItem;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;

public class FurledMapLootFunction extends LootItemConditionalFunction {
    final String destination, translation;

    protected FurledMapLootFunction(LootItemCondition[] lootConditions, String destination, String translation) {
        super(lootConditions);
        this.destination = destination;
        this.translation = translation;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() instanceof FurledMapItem) {
            return FurledMapItem.of(new ResourceLocation(destination), Component.translatable(translation));
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.SET_FURLED_MAP_FUNCTION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<FurledMapLootFunction> {
        public void serialize(JsonObject json, FurledMapLootFunction scrollFunction, JsonSerializationContext jsonDeserializationContext) {
            super.serialize(json, scrollFunction, jsonDeserializationContext);
        }

        public FurledMapLootFunction deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            if (!GsonHelper.isValidNode(json, "destination")) {
                throw new JsonSyntaxException("set_furled_map missing key: destination!");
            } else if (!GsonHelper.isValidNode(json, "translation")) {
                throw new JsonSyntaxException("set_furled_map missing key: translation!");
            } else {
                return new FurledMapLootFunction(lootConditions, GsonHelper.getAsString(json, "destination"), GsonHelper.getAsString(json, "translation"));
            }
        }
    }
}
