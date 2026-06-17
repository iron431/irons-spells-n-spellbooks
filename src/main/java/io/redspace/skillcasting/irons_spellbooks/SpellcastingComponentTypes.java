package io.redspace.skillcasting.irons_spellbooks;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.network.ComponentSyncCodecs;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public class SpellcastingComponentTypes {
    private static final DeferredRegister<ComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(SkillcastingRegistries.COMPONENT_TYPE_REGISTRY_KEY, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        COMPONENT_TYPES.register(eventBus);
    }

    public static ResourceLocation id(ComponentType<?> type) {
        return COMPONENT_TYPES.getRegistry().get().getKey(type);
    }

    @Nullable
    public static ComponentType<?> get(ResourceLocation id) {
        return COMPONENT_TYPES.getRegistry().get().get(id);
    }

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> MANA_COST =
            COMPONENT_TYPES.register("mana_cost", () -> ComponentType.<Integer>builder()
                    .synced(ComponentSyncCodecs.INT)
                    .persisted(Codec.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Unit>> IGNORE_MANA =
            COMPONENT_TYPES.register("ignore_mana", () -> ComponentType.<Unit>builder()
                    .persisted(Unit.CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<PortalData>> PORTAL_DATA =
            COMPONENT_TYPES.register("portal_data", () -> ComponentType.<PortalData>builder()
                    .persisted(PortalData.CODEC)
                    .synced(ComponentSyncCodecs.PORTAL_CAST_DATA)
                    .build());
}
