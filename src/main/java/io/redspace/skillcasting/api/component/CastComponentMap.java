package io.redspace.skillcasting.api.component;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CastComponentMap {
    public static final Codec<CastComponentMap> COMPONENT_CODEC = new CastComponentMapCodec();
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final StreamCodec<RegistryFriendlyByteBuf, CastComponentMap> STREAM_CODEC = StreamCodec.of(
            (buf, map) -> {
                HashMap<ComponentType<?>, Object> toSync = new HashMap<>();
                for (var entry : map.components.entrySet()) {
                    var type = entry.getKey();
                    if (type.isSynced()) {
                        toSync.put(type, entry.getValue());
                    }
                }
                buf.writeInt(toSync.size());
                for (var entry : toSync.entrySet()) {
                    buf.writeResourceLocation(Objects.requireNonNull(SkillcastingRegistries.COMPONENT_TYPES.getKey(entry.getKey()), "Cannot sync non-registered component type"));
                    ((StreamCodec) entry.getKey().streamCodec().orElseThrow()).encode(buf, entry.getValue());
                }
            },
            buf -> {
                int count = buf.readInt();
                HashMap<ComponentType<?>, Object> result = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    ResourceLocation id = buf.readResourceLocation();
                    ComponentType<?> componentType = Objects.requireNonNull(SkillcastingComponentTypes.get(id), "Cannot sync non-registered component type");
                    Object value = componentType.streamCodec().orElseThrow().decode(buf);
                    result.put(componentType, value);
                }
                return new CastComponentMap(result);
            }
    );
    private final Map<ComponentType<?>, Object> components;
    private final Set<ComponentType<?>> toSync = new HashSet<>();

    private CastComponentMap(Map<ComponentType<?>, Object> components) {
        this.components = components;
    }

    public CastComponentMap() {
        this(new HashMap<>());
    }

    /**
     * Reconstructs a map from persisted disk/network data
     */
    public static CastComponentMap from(Map<ComponentType<?>, Object> persisted) {
        return new CastComponentMap(new HashMap<>(persisted));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getOrNull(ComponentType<T> type) {
        return (T) components.get(type);
    }
    @SuppressWarnings("unchecked")
    public <T> T remove(ComponentType<T> type) {
        return (T) components.remove(type);
    }

    public <T> Optional<T> find(ComponentType<T> type) {
        return Optional.ofNullable(getOrNull(type));
    }

    public <T> void set(ComponentType<T> type, T value) {
        Object prev = components.put(type, value);
        if (type.isSynced() && !Objects.equals(prev, value)) {
            toSync.add(type);
        }
    }

    public boolean has(ComponentType<?> type) {
        return components.containsKey(type);
    }

    public Map<ComponentType<?>, Object> popDirtySync() {
        Map<ComponentType<?>, Object> dirty = new HashMap<>(toSync.size());
        for (ComponentType<?> type : toSync) {
            dirty.put(type, components.get(type));
        }
        toSync.clear();
        return dirty;
    }

    public Map<ComponentType<?>, Object> getAllSynced() {
        Map<ComponentType<?>, Object> all = new HashMap<>();
        for (Map.Entry<ComponentType<?>, Object> entry : components.entrySet()) {
            if (entry.getKey().isSynced()) {
                all.put(entry.getKey(), entry.getValue());
            }
        }
        return all;
    }

    public void applyFrom(CastComponentMap map) {
        this.components.putAll(map.components);
    }

    public void markAllSyncedDirty() {
        for (ComponentType<?> type : components.keySet()) {
            if (type.isSynced()) {
                toSync.add(type);
            }
        }
    }

    public Map<ComponentType<?>, Object> getAllPersisted() {
        Map<ComponentType<?>, Object> all = new HashMap<>();
        for (Map.Entry<ComponentType<?>, Object> entry : components.entrySet()) {
            if (entry.getKey().isPersisted()) {
                all.put(entry.getKey(), entry.getValue());
            }
        }
        return all;
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }
}
