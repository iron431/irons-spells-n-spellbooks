package io.redspace.skillcasting.api.recast;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
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
        boolean expired = false;
        Iterator<Map.Entry<ResourceLocation, RecastInstance>> it = recasts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, RecastInstance> entry = it.next();
            RecastInstance instance = entry.getValue();
            ActiveCast activeCast = caster.skillcastingData().getActiveCast();
            boolean isCastingSelf = activeCast != null &&
                    activeCast.context().skill().value().getSkillId().equals(entry.getKey());
            if (instance.isTimedOut(gameTime) && !isCastingSelf) {
                Holder<AbstractSkill> skill = SkillRegistry.holder(entry.getKey());
                it.remove();
                // fixme: need canonical pipeline for instantiating and hydrating cast context. this current state will cause issues (literally on the cooldown line)
                CastContext castContext = new CastContext(skill, caster, caster.level());
                skill.value().onRecastFinished(castContext, RecastResult.TIMEOUT);
                SkillcastingManager.triggerCooldown(castContext, skill.value(), castContext.find(SkillcastingComponentTypes.COOLDOWN_TICKS).orElse(skill.value().getCooldownTicks()));
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
