package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
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

    public void addRecast(RecastInstance instance) {
        recasts.put(instance.castContext().skill().value().getSkillId(), instance);
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

    public void rehydrate(CasterRef caster) {
        for (RecastInstance instance : recasts.values()) {
            instance.rehydrate(caster);
        }
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
        instance.consumeCast(castContext.level().getGameTime());
        if (instance.exhausted()) {
            castContext.skill().value().onRecastFinished(castContext, RecastResult.USED_ALL_RECASTS);
            recasts.remove(skillId);
            return false;
        }
        return true;
    }

    /**
     * @return <code>true</code>> if any recasts expired
     */
    public boolean pruneExpired(CasterRef caster, long gameTime) {
        if (recasts.isEmpty()) {
            return false;
        }
        rehydrate(caster);
        boolean expired = false;
        Iterator<Map.Entry<ResourceLocation, RecastInstance>> it = recasts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, RecastInstance> entry = it.next();
            RecastInstance instance = entry.getValue();
            CastContext context = instance.castContext();
            boolean isCastingSelf = context.getSkillcastingData().isCasting() &&
                    context.getSkillcastingData().getActiveCast().context().skill()
                            .equals(context.skill());
            if (instance.isTimedOut(gameTime) && !isCastingSelf) {
                it.remove();
                context.skill().value().onRecastFinished(context, RecastResult.TIMEOUT);
                SkillcastingManager.triggerCooldown(context, context.skill().value(), context.get(SkillcastingComponentTypes.COOLDOWN_TICKS.get()));
                expired = true;
            }
        }
        return expired;
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
