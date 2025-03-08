package io.redspace.ironsspellbooks.item.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record UpgradeOrbType(
        Holder<Attribute> attribute,
        double amount,
        AttributeModifier.Operation operation,
        Optional<Holder<Item>> containerItem
) {
    public static final Codec<UpgradeOrbType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(UpgradeOrbType::attribute),
            Codec.DOUBLE.fieldOf("amount").forGetter(UpgradeOrbType::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(UpgradeOrbType::operation),
            BuiltInRegistries.ITEM.holderByNameCodec().optionalFieldOf("containerItem").forGetter(UpgradeOrbType::containerItem)
    ).apply(builder, UpgradeOrbType::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeOrbType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
            type -> type.attribute,
            ByteBufCodecs.DOUBLE,
            type -> type.amount,
            AttributeModifier.Operation.STREAM_CODEC,
            type -> type.operation,
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(Registries.ITEM)),
            type -> type.containerItem,
            UpgradeOrbType::new
    );
}
