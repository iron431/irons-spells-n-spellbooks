package io.redspace.skillcasting.api.cast;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.resolver.DirectionResolver;
import io.redspace.skillcasting.api.resolver.PositionResolver;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public final class CastContext {

    private final Holder<AbstractSkill> skill;
    private final CasterRef caster;
    private final Level level;
    private final CastComponentMap components;

    public CastContext(Holder<AbstractSkill> skill, CasterRef caster, Level level) {
        this.skill = skill;
        this.caster = caster;
        this.level = level;
        this.components = new CastComponentMap();
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

    public CastComponentMap components() {
        return components;
    }

    public Vec3 position(PositionAnchor anchor) {
        PositionResolver resolver = getOrDefault(SkillcastingComponentTypes.POSITION_RESOLVER, PositionResolver.Caster.INSTANCE);
        Vec3 pos = resolver.resolve(this, anchor);
        Vec3 offset = getOrDefault(SkillcastingComponentTypes.POSITION_MODIFIER, Vec3.ZERO);
        return pos.add(offset);
    }

    public Vec3 direction() {
        DirectionResolver resolver = getOrDefault(SkillcastingComponentTypes.DIRECTION_RESOLVER, DirectionResolver.Caster.INSTANCE);
        Vec3 base = resolver.resolve(this).normalize();
        Vec2 rotation = getOrNull(SkillcastingComponentTypes.ROTATION_MODIFIER);
        return rotation == null ? base : base.xRot(rotation.x).yRot(rotation.y);
    }

    /*
     * QOL shortcuts
     */
    public SkillcastingData getSkillcastingData() {
        return caster.skillcastingData();
    }

    public Vec3 position() {
        return position(PositionAnchor.CASTING_POSITION);
    }

    /**
     * @return If the context owner is a {@link EntityCasterRef}, returns the {@link Entity} behind it. Otherwise, <code>null</code>
     */
    public @Nullable Entity asEntityCaster() {
        return caster instanceof EntityCasterRef entity ? entity.entity() : null;
    }

    public int getRecastsRemaining() {
        var recast = getSkillcastingData().recasts().get(this.skill());
        return recast == null ? 0 : recast.remainingCasts();
    }

    public int getSkillLevel() {
        return getOrDefault(SkillcastingComponentTypes.SKILL_LEVEL, 1);
    }

    @Nullable
    public <T> T getOrNull(Supplier<ComponentType<T>> type) {
        return components.getOrNull(type.get());
    }

    public <T> T getOrDefault(Supplier<ComponentType<T>> type, T defaultValue) {
        return find(type).orElse(defaultValue);
    }

    public <T> Optional<T> find(Supplier<ComponentType<T>> type) {
        return components.find(type.get());
    }

    public <T> void set(Supplier<ComponentType<T>> type, T value) {
        components.set(type.get(), value);
    }

    public <T> boolean has(Supplier<ComponentType<T>> type) {
        return components.has(type.get());
    }

    public <T> T remove(Supplier<ComponentType<T>> type) {
        return components.remove(type.get());
    }
}
