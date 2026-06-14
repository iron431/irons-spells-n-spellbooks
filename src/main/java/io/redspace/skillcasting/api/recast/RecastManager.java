package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RecastManager {
    public static final Codec<RecastManager> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, RecastInstance.CODEC)
            .xmap(RecastManager::fromMap, RecastManager::byId);

    private static RecastManager fromMap(Map<ResourceLocation, RecastInstance> map) {
        RecastManager manager = new RecastManager();
        manager.recasts.putAll(map);
        return manager;
    }

    //todo: replace resourcelocation to holders?
    private final Map<ResourceLocation, RecastInstance> recasts = new HashMap<>();

    public void addRecast(Holder<AbstractSkill> skill, RecastInstance instance) {
        recasts.put(skill.value().getSkillId(), instance);
    }

    public boolean hasRecast(ResourceLocation skillId) {
        return recasts.containsKey(skillId);
    }

    public boolean hasRecast(AbstractSkill skill) {
        return hasRecast(skill.getSkillId());
    }

    public boolean hasRecast(Holder<AbstractSkill> skill) {
        return hasRecast(skill.value());
    }

    @Nullable
    public RecastInstance get(ResourceLocation skillId) {
        return recasts.get(skillId);
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

    public Map<ResourceLocation, RecastInstance> byId() {
        return recasts;
    }

    public void replaceFrom(Map<ResourceLocation, RecastInstance> synced) {
        recasts.clear();
        recasts.putAll(synced);
    }

    /**
     * @return <code>true</code> if there are remaining recasts for this skill
     */
    public boolean handleRecastConsumption(CastContext castContext) {
        var skillId = castContext.skill().value().getSkillId();
        RecastInstance instance = recasts.get(skillId);
        if (instance == null) {
            return false;
        }
        instance.consumeCast();
        if (instance.usedAllCasts()) {
            castContext.skill().value().onRecastFinished(castContext, RecastResult.USED_ALL_RECASTS);
            recasts.remove(skillId);
            return false;
        }
        return true;
    }

    /**
     * Ticks recast durations, and handles recast expiry via {@link SkillcastingManager#handleRecastTimeout(CasterRef, ResourceLocation)}
     *
     * @return true if any entry was removed
     */
    public boolean tick(CasterRef casterRef) {
        if (recasts.isEmpty()) {
            return false;
        }
        boolean changed = false;
        ActiveCast activeCast = casterRef.skillcastingData().getActiveCast();
        ResourceLocation castingSkillId = activeCast == null ? null : activeCast.context().skill().value().getSkillId();

        Iterator<Map.Entry<ResourceLocation, RecastInstance>> it = recasts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, RecastInstance> entry = it.next();
            RecastInstance instance = entry.getValue();
            instance.tick();
            boolean isCastingSelf = entry.getKey().equals(castingSkillId);
            if (instance.isTimedOut() && !isCastingSelf) {
                it.remove();
                SkillcastingManager.handleRecastTimeout(casterRef, entry.getKey());
                changed = true;
            }
        }
        return changed;
    }

    public Map<ResourceLocation, RecastInstance> removeAll() {
        if (recasts.isEmpty()) {
            return Map.of();
        }
        Map<ResourceLocation, RecastInstance> closed = Map.copyOf(recasts);
        recasts.clear();
        return closed;
    }
}
