package io.redspace.skillcasting.api.cast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

public final class CastContext {
    /**
     * Portable cast state for disk and network. Rehydrate with {@link #restoreFrom(CasterRef)} once a
     * live {@link CasterRef} is available.
     */
    public record Snapshot(Holder<AbstractSkill> skill, Map<ComponentType<?>, Object> components) {
        public CastContext restoreFrom(CasterRef caster) {
            return CastContext.fromSnapshot(this, caster);
        }
    }

    public static final Codec<Map<ComponentType<?>, Object>> COMPONENT_CODEC = Codec.dispatchedMap(
            SkillcastingRegistries.COMPONENT_TYPE_CODEC,
            type -> type.codec().orElseThrow(() -> new IllegalStateException(
                    "Component type is not persistable: " + SkillcastingComponentTypes.id(type))));

    public static final Codec<Snapshot> SNAPSHOT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SkillcastingRegistries.SKILL_HOLDER_CODEC.fieldOf("skill").forGetter(Snapshot::skill),
            COMPONENT_CODEC.fieldOf("components").forGetter(Snapshot::components)
    ).apply(builder, Snapshot::new));

    private final Holder<AbstractSkill> skill;
    private final CasterRef caster;
    private final Level level;
    private final Map<ComponentType<?>, Object> components;
    private final Set<ComponentType<?>> toSync = new HashSet<>();

    private CastContext(Holder<AbstractSkill> skill, CasterRef caster, Level level, Map<ComponentType<?>, Object> components) {
        this.skill = skill;
        this.caster = caster;
        this.level = level;
        this.components = new HashMap<>(components);
    }

    public CastContext(Holder<AbstractSkill> skill, CasterRef caster, Level level) {
        this(skill, caster, level, Map.of());
    }

    public static CastContext fromSnapshot(Snapshot snapshot, CasterRef caster) {
        CastContext context = new CastContext(snapshot.skill(), caster, caster.level(), snapshot.components());
        context.applyRuntimeDefaults();
        return context;
    }

    public Snapshot toSnapshot() {
        Map<ComponentType<?>, Object> persisted = new HashMap<>();
        for (Map.Entry<ComponentType<?>, Object> entry : components.entrySet()) {
            ComponentType<?> type = entry.getKey();
            Object value = entry.getValue();
            if (value != null && type.codec().isPresent()) {
                persisted.put(type, value);
            }
        }
        return new Snapshot(skill, persisted);
    }

    /**
     * Restores ephemeral resolver defaults omitted from persisted snapshots.
     */
    public void applyRuntimeDefaults() {
        if (!has(SkillcastingComponentTypes.POSITION_RESOLVER.get())) {
            set(SkillcastingComponentTypes.POSITION_RESOLVER.get(), PositionResolver.Caster.INSTANCE);
        }
        if (!has(SkillcastingComponentTypes.DIRECTION_RESOLVER.get())) {
            set(SkillcastingComponentTypes.DIRECTION_RESOLVER.get(), DirectionResolver.Caster.INSTANCE);
        }
    }

    public Holder<AbstractSkill> skill() {
        return skill;
    }

    public CasterRef caster() {
        return caster;
    }

    public Level level() {
        return level;
    }

    public SkillcastingData getSkillcastingData() {
        return caster.skillcastingData();
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

    public void applySynced(Map<ComponentType<?>, Object> synced) {
        components.putAll(synced);
    }

    public void markAllSyncedDirty() {
        for (ComponentType<?> type : components.keySet()) {
            if (type.isSynced()) {
                toSync.add(type);
            }
        }
    }

    public Vec3 position(PositionAnchor anchor) {
        PositionResolver resolver = get(SkillcastingComponentTypes.POSITION_RESOLVER.get());
        if (resolver == null) {
            resolver = PositionResolver.Caster.INSTANCE;
        }
        Vec3 pos = resolver.resolve(this, anchor);
        Vec3 offset = get(SkillcastingComponentTypes.POSITION_MODIFIER.get());
        return offset != null ? pos.add(offset) : pos;
    }

    public Vec3 position() {
        return position(PositionAnchor.CASTING_POSITION);
    }

    public Vec3 direction() {
        DirectionResolver resolver = get(SkillcastingComponentTypes.DIRECTION_RESOLVER.get());
        if (resolver == null) {
            resolver = DirectionResolver.Caster.INSTANCE;
        }
        Vec3 base = resolver.resolve(this).normalize();
        Vec2 rotation = get(SkillcastingComponentTypes.ROTATION_MODIFIER.get());
        return rotation == null ? base : base.xRot(rotation.x).yRot(rotation.y);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ComponentType<T> type) {
        return (T) components.get(type);
    }

    public <T> Optional<T> find(ComponentType<T> type) {
        return Optional.ofNullable(get(type));
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
}
