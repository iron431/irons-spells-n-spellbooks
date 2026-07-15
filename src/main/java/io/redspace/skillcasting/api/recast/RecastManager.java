package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecastManager {
    public static final Codec<RecastManager> CODEC = Codec.list(RecastInstance.CODEC)
            .xmap(RecastManager::fromList, RecastManager::getActiveRecasts);

    private static RecastManager fromList(List<RecastInstance> instances) {
        RecastManager manager = new RecastManager();
        for (RecastInstance instance : instances) {
            manager.addRecast(instance);
        }
        return manager;
    }

    private final Map<Holder<AbstractSkill>, RecastInstance> recasts = new HashMap<>();

    public void removeRecast(CasterRef caster, Holder<AbstractSkill> skill, RecastResult result) {
        RecastInstance instance = recasts.remove(skill);
        if (instance == null) {
            return;
        }
        if (caster.level().isClientSide) {
            return;
        }
        CastContext castContext = new CastContext(skill, caster, caster.level());
        castContext.components().applyFrom(instance.components());
        skill.value().onRecastFinished(castContext, result);
        SkillcastingManager.triggerCooldown(castContext);
        SkillcastingNetwork.syncRecastRemove(caster, skill);
    }

    public void addRecast(RecastInstance instance) {
        recasts.put(instance.skill(), instance);
    }

    public void addRecast(Holder<AbstractSkill> skill, RecastInstance instance) {
        recasts.put(skill, instance);
    }

    public void addRecast(AbstractSkill skill, RecastInstance instance) {
        addRecast(SkillcastingRegistries.SKILL_REGISTRY.wrapAsHolder(skill), instance);
    }

    public boolean hasRecast(Holder<AbstractSkill> skill) {
        return recasts.containsKey(skill);
    }

    public boolean hasRecast(AbstractSkill skill) {
        return hasRecast(SkillcastingRegistries.SKILL_REGISTRY.wrapAsHolder(skill));
    }

    @Nullable
    public RecastInstance get(Holder<AbstractSkill> skill) {
        return recasts.get(skill);
    }

    @Nullable
    public RecastInstance get(AbstractSkill skill) {
        return get(SkillcastingRegistries.SKILL_REGISTRY.wrapAsHolder(skill));
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

    public Map<Holder<AbstractSkill>, RecastInstance> asMap() {
        return recasts;
    }

    public void replaceFrom(RecastManager other) {
        recasts.clear();
        recasts.putAll(other.recasts);
    }

    public void replaceFrom(Map<Holder<AbstractSkill>, RecastInstance> synced) {
        recasts.clear();
        recasts.putAll(synced);
    }

    public void applySynced(Holder<AbstractSkill> skill, @Nullable RecastInstance instance) {
        if (instance == null) {
            recasts.remove(skill);
        } else {
            recasts.put(instance.skill(), instance);
        }
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
     * Ticks recast durations, and handles recast expiry via {@link SkillcastingManager#removeRecast(CasterRef, Holder, RecastInstance)}
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

        for (RecastInstance instance : this.getActiveRecasts()) {
            instance.tick();
            boolean isCastingSelf = instance.skill().equals(castingSkill);
            if (instance.isTimedOut() && !isCastingSelf) {
                removeRecast(casterRef, instance.skill(), RecastResult.TIMEOUT);
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
