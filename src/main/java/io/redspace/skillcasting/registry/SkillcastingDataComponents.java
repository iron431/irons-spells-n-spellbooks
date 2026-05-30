package io.redspace.skillcasting.registry;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

/**
 * Item data components owned by the skillcasting API (skill containers on items).
 */
public final class SkillcastingDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Skillcasting.NAMESPACE);

    private SkillcastingDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerDataComponent(
            String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ISkillContainer>> SKILL_CONTAINER =
            registerDataComponent("skill_container",
                    builder -> builder.persistent(SkillContainer.CODEC).cacheEncoding());
}
