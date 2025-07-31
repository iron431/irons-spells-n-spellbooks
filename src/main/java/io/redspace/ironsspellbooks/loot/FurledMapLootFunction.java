package io.redspace.ironsspellbooks.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
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

public class FurledMapLootFunction extends LootItemConditionalFunction {
    final String destination, translation;

    protected FurledMapLootFunction(LootItemCondition[] lootConditions, String destination, String translation) {
        super(lootConditions);
        this.destination = destination;
        this.translation = translation;
    }

    public static LootItemConditionalFunction.Builder<?> create(final String destination, final String translation) {
        return simpleBuilder((functions) -> new FurledMapLootFunction(functions, destination, translation));
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() instanceof FurledMapItem) {
            return FurledMapItem.of(ResourceLocation.parse(destination), Component.translatable(translation));
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
            json.addProperty("destination", scrollFunction.destination);
            json.addProperty("translation", scrollFunction.translation);
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
