package io.redspace.skillcasting.registry;

import io.redspace.skillcasting.Skillcasting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public final class SkillcastingDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Skillcasting.NAMESPACE);

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerDataComponent(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> CASTING_IMPLEMENT =
            registerDataComponent("casting_implement",
                    (builder) -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).cacheEncoding());

}
