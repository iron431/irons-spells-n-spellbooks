package io.redspace.skillcasting.api.cast;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class CastContext {
    private final Holder<AbstractSkill> skill;
    private final CasterRef caster;
    private final Level level;

    private final Map<ComponentType<?>, Object> components = new HashMap<>();
    private final Set<ComponentType<?>> toSync = new HashSet<>();

    public CastContext(Holder<AbstractSkill> skill, CasterRef caster, Level level) {
        this.skill = skill;
        this.caster = caster;
        this.level = level;
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
