package io.redspace.ironsspellbooks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    public String getDestination() {
        return destination;
    }

    public String getTranslation() {
        return translation;
    }

    private final String destination, translation;


    public static final MapCodec<FurledMapLootFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> commonFields(builder).and(
            builder.group(
                    Codec.STRING.fieldOf("destination").forGetter(FurledMapLootFunction::getDestination),
                    Codec.STRING.fieldOf("description_translation").forGetter(FurledMapLootFunction::getTranslation)
            )
    ).apply(builder, FurledMapLootFunction::new));

    protected FurledMapLootFunction(List<LootItemCondition> lootConditions, String destination, String translation) {
        super(lootConditions);
        this.destination = destination;
        this.translation = translation;
    }

//    public static LootItemConditionalFunction.Builder<?> create(final String destination, final String translation) {
//        return simpleBuilder((functions) -> new FurledMapLootFunction(functions, destination, translation));
//    }


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
}
