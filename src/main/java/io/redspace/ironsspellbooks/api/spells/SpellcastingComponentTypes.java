package io.redspace.ironsspellbooks.api.spells;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.capabilities.magic.TelekinesisData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.spells.FireWallCastComponent;
import io.redspace.ironsspellbooks.spells.StarfallCastComponent;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.network.StreamCodecUtils;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
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
                    .synced(StreamCodecUtils.INT)
                    .persisted(Codec.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Unit>> IGNORE_MANA =
            COMPONENT_TYPES.register("ignore_mana", () -> ComponentType.<Unit>builder()
                    .persisted(Unit.CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<PortalData>> PORTAL_DATA =
            COMPONENT_TYPES.register("portal_data", () -> ComponentType.<PortalData>builder()
                    .persisted(PortalData.CODEC)
                    .synced(PortalData.PORTAL_CAST_DATA)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> SPELL_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("spell_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> FIRE_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("fire_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> ICE_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("ice_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> LIGHTNING_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("lightning_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> HOLY_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("holy_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> ENDER_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("ender_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> BLOOD_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("blood_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> EVOCATION_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("evocation_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> NATURE_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("nature_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> ELDRITCH_POWER_MULTIPLIER =
            COMPONENT_TYPES.register("eldritch_power", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> SUMMON_HEALTH =
            COMPONENT_TYPES.register("summon_health", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> CONSTRUCT_HEALTH =
            COMPONENT_TYPES.register("construct_health", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> SUMMON_ATTACK_DAMAGE =
            COMPONENT_TYPES.register("summon_attack_damage", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(StreamCodecUtils.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> SUMMON_COUNT =
            COMPONENT_TYPES.register("summon_count", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(StreamCodecUtils.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<SummonedEntitiesCastData>> SUMMONED_ENTITY_DATA =
            COMPONENT_TYPES.register("summoned_entity_data", () -> ComponentType.<SummonedEntitiesCastData>builder()
                    .persisted(SummonedEntitiesCastData.CODEC)
                    .synced(SummonedEntitiesCastData.SUMMONED_ENTITIES_CAST_DATA)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<FireWallCastComponent>> FIRE_WALL_DATA =
            COMPONENT_TYPES.register("fire_wall_data", () -> ComponentType.<FireWallCastComponent>builder()
                    .persisted(FireWallCastComponent.CODEC)
                    .synced(FireWallCastComponent.FIRE_WALL_CAST_DATA)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<TelekinesisData>> TELEKINESIS_DATA =
            COMPONENT_TYPES.register("telekinesis_data", () -> ComponentType.<TelekinesisData>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<StarfallCastComponent>> STARFALL_DATA =
            COMPONENT_TYPES.register("starfall_data", () -> ComponentType.<StarfallCastComponent>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> HIT_COUNT =
            COMPONENT_TYPES.register("hit_count", () -> ComponentType.<Integer>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<AnimationHolder>> CAST_START_ANIMATION =
            COMPONENT_TYPES.register("cast_start_animation", () -> ComponentType.<AnimationHolder>builder()
                    .synced(AnimationHolder.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<AnimationHolder>> CAST_FINISH_ANIMATION =
            COMPONENT_TYPES.register("cast_finish_animation", () -> ComponentType.<AnimationHolder>builder()
                    .synced(AnimationHolder.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<ItemStack>> SCROLL_STACK =
            COMPONENT_TYPES.register("scroll_stack", () -> ComponentType.<ItemStack>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> RING_COUNT =
            COMPONENT_TYPES.register("ring_count", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(StreamCodecUtils.INT)
                    .build());
}
