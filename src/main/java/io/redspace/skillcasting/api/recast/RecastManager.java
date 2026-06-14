package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RecastManager {
    public static final Codec<RecastManager> CODEC = Codec.unboundedMap(SkillcastingRegistries.SKILL_HOLDER_CODEC, RecastInstance.CODEC)
            .xmap(RecastManager::fromMap, RecastManager::toMap);

    private static RecastManager fromMap(Map<Holder<AbstractSkill>, RecastInstance> map) {
        RecastManager manager = new RecastManager();
        manager.recasts.putAll(map);
        return manager;
    }

    private final Map<Holder<AbstractSkill>, RecastInstance> recasts = new HashMap<>();

    public void addRecast(Holder<AbstractSkill> skill, RecastInstance instance) {
        recasts.put(skill, instance);
    }

    public void addRecast(AbstractSkill skill, RecastInstance instance) {
        addRecast(SkillcastingRegistries.SKILLS.wrapAsHolder(skill), instance);
    }

    public boolean hasRecast(Holder<AbstractSkill> skill) {
        return recasts.containsKey(skill);
    }

    public boolean hasRecast(AbstractSkill skill) {
        return hasRecast(SkillcastingRegistries.SKILLS.wrapAsHolder(skill));
    }

    @Nullable
    public RecastInstance get(Holder<AbstractSkill> skill) {
        return recasts.get(skill);
    }

    @Nullable
    public RecastInstance get(AbstractSkill skill) {
        return get(SkillcastingRegistries.SKILLS.wrapAsHolder(skill));
    }

    public boolean isEmpty() {
        return recasts.isEmpty();
    }

    public boolean hasRecastsActive() {
        return !recasts.isEmpty();
    }

    public List<RecastInstance> getActiveRecasts() {
        return List.copyOf(recasts.values());
    }

    public Map<Holder<AbstractSkill>, RecastInstance> view() {
        return recasts;
    }

    private Map<Holder<AbstractSkill>, RecastInstance> toMap() {
        return Map.copyOf(recasts);
    }

    public void replaceFrom(RecastManager other) {
        recasts.clear();
        recasts.putAll(other.recasts);
    }

    public void replaceFrom(Map<Holder<AbstractSkill>, RecastInstance> synced) {
        recasts.clear();
        recasts.putAll(synced);
    }

    /**
     * @return <code>true</code> if there are remaining recasts for this skill
     */
    public boolean handleRecastConsumption(CastContext castContext) {
        Holder<AbstractSkill> skill = castContext.skill();
        RecastInstance instance = recasts.get(skill);
        if (instance == null) {
            return false;
        }
        instance.consumeCast();
        if (instance.usedAllCasts()) {
            skill.value().onRecastFinished(castContext, RecastResult.USED_ALL_RECASTS);
            recasts.remove(skill);
            return false;
        }
        return true;
    }

    /**
     * Ticks recast durations, and handles recast expiry via {@link SkillcastingManager#handleRecastTimeout(CasterRef, Holder)}
     *
     * @return true if any entry was removed
     */
    public boolean tick(CasterRef casterRef) {
        if (recasts.isEmpty()) {
            return false;
        }
        boolean changed = false;
        ActiveCast activeCast = casterRef.skillcastingData().getActiveCast();
        Holder<AbstractSkill> castingSkill = activeCast == null ? null : activeCast.context().skill();

        Iterator<Map.Entry<Holder<AbstractSkill>, RecastInstance>> it = recasts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Holder<AbstractSkill>, RecastInstance> entry = it.next();
            RecastInstance instance = entry.getValue();
            instance.tick();
            boolean isCastingSelf = entry.getKey().equals(castingSkill);
            if (instance.isTimedOut() && !isCastingSelf) {
                Holder<AbstractSkill> skill = entry.getKey();
                it.remove();
                SkillcastingManager.handleRecastTimeout(casterRef, skill);
                changed = true;
            }
        }
        return changed;
    }

    public Map<Holder<AbstractSkill>, RecastInstance> removeAll() {
        if (recasts.isEmpty()) {
            return Map.of();
        }
        Map<Holder<AbstractSkill>, RecastInstance> closed = Map.copyOf(recasts);
        recasts.clear();
        return closed;
    }
}
