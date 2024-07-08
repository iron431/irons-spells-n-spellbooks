package io.redspace.ironsspellbooks.loot;

import io.redspace.ironsspellbooks.item.FurledMapItem;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class FurledMapLootFunction extends LootItemConditionalFunction {
    final String destination, translation;

    protected FurledMapLootFunction(List<LootItemCondition> lootConditions, String destination, String translation) {
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

    //FIXME: 1.21: probably need a codec, but it aint complaining yet
//    public static class Serializer extends LootItemConditionalFunction.Serializer<FurledMapLootFunction> {
//        public void serialize(JsonObject json, FurledMapLootFunction scrollFunction, JsonSerializationContext jsonDeserializationContext) {
//            super.serialize(json, scrollFunction, jsonDeserializationContext);
//            json.addProperty("destination", scrollFunction.destination);
//            json.addProperty("translation", scrollFunction.translation);
//        }
//
//        public FurledMapLootFunction deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
//            if (!GsonHelper.isValidNode(json, "destination")) {
//                throw new JsonSyntaxException("set_furled_map missing key: destination!");
//            } else if (!GsonHelper.isValidNode(json, "translation")) {
//                throw new JsonSyntaxException("set_furled_map missing key: translation!");
//            } else {
//                return new FurledMapLootFunction(lootConditions, GsonHelper.getAsString(json, "destination"), GsonHelper.getAsString(json, "translation"));
//            }
//        }
//    }
}
