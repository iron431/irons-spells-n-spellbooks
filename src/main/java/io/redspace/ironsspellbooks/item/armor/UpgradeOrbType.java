package io.redspace.ironsspellbooks.item.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public record UpgradeOrbType(
        Holder<Attribute> attribute,
        double amount,
        AttributeModifier.Operation operation,
        Optional<ItemStack> containerItem
) {
    private static final Codec<ItemStack> ITEM_OR_ITEMSTACK_CODEC = CodecHelper.withAlternative(ItemStack.CODEC, BuiltInRegistries.ITEM.holderByNameCodec().xmap(ItemStack::new, ItemStack::getItemHolder));
    public static final Codec<UpgradeOrbType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(UpgradeOrbType::attribute),
            Codec.DOUBLE.fieldOf("amount").forGetter(UpgradeOrbType::amount),
            Codec.STRING.xmap(AttributeModifier.Operation::valueOf,AttributeModifier.Operation::name).fieldOf("operation").forGetter(UpgradeOrbType::operation),
//            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(UpgradeOrbType::operation),
            ITEM_OR_ITEMSTACK_CODEC.optionalFieldOf("containerItem").forGetter(UpgradeOrbType::containerItem)
    ).apply(builder, UpgradeOrbType::new));

    public UpgradeOrbType(
            Holder<Attribute> attribute,
            double amount,
            AttributeModifier.Operation operation,
            Supplier<Item> container
    ) {
        this(attribute, amount, operation, Optional.of(new ItemStack(container.get())));
    }

    public UpgradeOrbType(
            Supplier<Attribute> attribute,
            double amount,
            AttributeModifier.Operation operation,
            Supplier<Item> container
    ) {
        this(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get()), amount, operation, Optional.of(new ItemStack(container.get())));
    }

    public UpgradeOrbType(
            Supplier<Attribute> attribute,
            double amount,
            AttributeModifier.Operation operation
    ) {
        this(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get()), amount, operation, Optional.empty());
    }

//    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeOrbType> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
//            type -> type.attribute,
//            ByteBufCodecs.DOUBLE,
//            type -> type.amount,
//            AttributeModifier.Operation.STREAM_CODEC,
//            type -> type.operation,
//            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(ITEM_OR_ITEMSTACK_CODEC)),
//            type -> type.containerItem,
//            UpgradeOrbType::new
//    );
}
