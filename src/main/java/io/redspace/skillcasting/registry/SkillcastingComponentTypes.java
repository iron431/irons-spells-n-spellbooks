package io.redspace.skillcasting.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.network.ComponentSyncCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public final class SkillcastingComponentTypes {
    private static final Codec<Vec2> VEC2_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.FLOAT.fieldOf("x").forGetter(v -> v.x),
            Codec.FLOAT.fieldOf("y").forGetter(v -> v.y)
    ).apply(builder, Vec2::new));

    private static final DeferredRegister<ComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(SkillcastingRegistries.COMPONENT_TYPE_REGISTRY_KEY, Skillcasting.NAMESPACE);

    public static void register(IEventBus eventBus) {
        COMPONENT_TYPES.register(eventBus);
        SpellcastingComponentTypes.register(eventBus);
    }

    public static ResourceLocation id(ComponentType<?> type) {
        return COMPONENT_TYPES.getRegistry().get().getKey(type);
    }

    @Nullable
    public static ComponentType<?> get(ResourceLocation id) {
        return COMPONENT_TYPES.getRegistry().get().get(id);
    }

    public static final DeferredHolder<ComponentType<?>, ComponentType<PositionResolver>> POSITION_RESOLVER =
            COMPONENT_TYPES.register("position_resolver", () -> ComponentType.<PositionResolver>builder()
                    .synced(PositionResolver.codec(SkillcastingRegistries.POSITION_RESOLVER_TYPES))
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<DirectionResolver>> DIRECTION_RESOLVER =
            COMPONENT_TYPES.register("direction_resolver", () -> ComponentType.<DirectionResolver>builder()
                    .synced(DirectionResolver.codec(SkillcastingRegistries.DIRECTION_RESOLVER_TYPES))
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Vec3>> POSITION_MODIFIER =
            COMPONENT_TYPES.register("position_modifier", () -> ComponentType.<Vec3>builder()
                    .persisted(Vec3.CODEC)
                    .synced(ComponentSyncCodecs.VEC3)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Vec2>> ROTATION_MODIFIER =
            COMPONENT_TYPES.register("rotation_modifier", () -> ComponentType.<Vec2>builder()
                    .persisted(VEC2_CODEC)
                    .synced(ComponentSyncCodecs.VEC2)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> SKILL_LEVEL =
            COMPONENT_TYPES.register("skill_level", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

//    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> TARGET =
//            COMPONENT_TYPES.register("target", () -> ComponentType.<Integer>builder()
//                    .persisted(Codec.INT)
//                    .synced(ComponentSyncCodecs.INT)
//                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<MultiTargetEntityCastComponent>> MULTI_TARGET_ENTITIES =
            COMPONENT_TYPES.register("multi_target_entities", () -> ComponentType.<MultiTargetEntityCastComponent>builder()
                    .persisted(MultiTargetEntityCastComponent.CODEC)
                    .synced(ComponentSyncCodecs.MULTI_TARGET_ENTITY)
                    .build());

    // todo: this is an analog for cone spells EntityCastData or target area's TargetAreaEntityCast data. better name may be in order
    public static final DeferredHolder<ComponentType<?>, ComponentType<MultiTargetEntityCastComponent>> ATTACHED_ENTITIES =
            COMPONENT_TYPES.register("attached_entities", () -> ComponentType.<MultiTargetEntityCastComponent>builder()
                    .persisted(MultiTargetEntityCastComponent.CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> CAST_TIME =
            COMPONENT_TYPES.register("cast_time", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> COOLDOWN_TICKS =
            COMPONENT_TYPES.register("cooldown", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<PlayableSound>> CAST_CHANNEL_SOUND =
            COMPONENT_TYPES.register("cast_channel_sound", () -> ComponentType.<PlayableSound>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<PlayableSound>> ON_CAST_SOUND =
            COMPONENT_TYPES.register("on_cast_sound", () -> ComponentType.<PlayableSound>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Unit>> IGNORE_COOLDOWN =
            COMPONENT_TYPES.register("ignore_cooldown", () -> ComponentType.<Unit>builder()
                    .persisted(Unit.CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<String>> CAST_SOURCE =
            COMPONENT_TYPES.register("cast_source", () -> ComponentType.<String>builder()
                    .persisted(Codec.STRING)
                    .synced(ComponentSyncCodecs.STRING)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<RecastConfig>> RECAST_CONFIG =
            COMPONENT_TYPES.register("recast_config", () -> ComponentType.<RecastConfig>builder().build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<HitResult>> HIT_RESULT_TRANSIENT =
            COMPONENT_TYPES.register("hit_result_transient", () -> ComponentType.<HitResult>builder()
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> CAST_RADIUS =
            COMPONENT_TYPES.register("cast_radius", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> CAST_RANGE =
            COMPONENT_TYPES.register("cast_range", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> EFFECT_DURATION_TICKS =
            COMPONENT_TYPES.register("effect_duration_ticks", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> EFFECT_AMPLIFIER =
            COMPONENT_TYPES.register("effect_amplifier", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> HEALING =
            COMPONENT_TYPES.register("healing", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> DAMAGE =
            COMPONENT_TYPES.register("damage", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> WEAPON_DAMAGE =
            COMPONENT_TYPES.register("weapon_damage", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> AOE_DAMAGE =
            COMPONENT_TYPES.register("aoe_damage", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Float>> PROJECTILE_SPEED =
            COMPONENT_TYPES.register("projectile_speed", () -> ComponentType.<Float>builder()
                    .persisted(Codec.FLOAT)
                    .synced(ComponentSyncCodecs.FLOAT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> PROJECTILE_PIERCE =
            COMPONENT_TYPES.register("projectile_pierce", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> PROJECTILE_RICOCHET =
            COMPONENT_TYPES.register("projectile_ricochet", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Unit>> CURSOR_HOMING =
            COMPONENT_TYPES.register("cursor_homing", () -> ComponentType.<Unit>builder()
                    .persisted(Unit.CODEC)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Vec3>> TARGET_POSITION =
            COMPONENT_TYPES.register("target_pos", () -> ComponentType.<Vec3>builder()
                    .persisted(Vec3.CODEC)
                    .synced(ComponentSyncCodecs.VEC3)
                    .build());

    public static final DeferredHolder<ComponentType<?>, ComponentType<Integer>> RANDOM_SEED =
            COMPONENT_TYPES.register("random_seed", () -> ComponentType.<Integer>builder()
                    .persisted(Codec.INT)
                    .synced(ComponentSyncCodecs.INT)
                    .build());

//    public static final DeferredHolder<ComponentType<?>, ComponentType<UUID>> ENTITY_HOMING =
//            COMPONENT_TYPES.register("entity_homing", () -> ComponentType.<UUID>builder()
//                    .persisted(MultiTargetEntityCastComponent.UUID_CODEC)
//                    .synced(ComponentSyncCodecs.UUID)
//                    .build());
}
