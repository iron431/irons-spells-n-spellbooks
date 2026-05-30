package io.redspace.skillcasting.cooldown;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CooldownManager {
    public static final Codec<CooldownManager> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, CooldownInstance.CODEC)
            .xmap(CooldownManager::fromMap, CooldownManager::toMap);

    private final Map<ResourceLocation, CooldownInstance> cooldowns = new HashMap<>();

    private static CooldownManager fromMap(Map<ResourceLocation, CooldownInstance> map) {
        CooldownManager manager = new CooldownManager();
        manager.cooldowns.putAll(map);
        return manager;
    }

    private Map<ResourceLocation, CooldownInstance> toMap() {
        return Map.copyOf(cooldowns);
    }

    public void replaceFrom(CooldownManager other) {
        cooldowns.clear();
        cooldowns.putAll(other.cooldowns);
    }

    public void replaceFrom(Map<ResourceLocation, CooldownInstance> synced) {
        cooldowns.clear();
        cooldowns.putAll(synced);
    }

    public void addCooldown(AbstractSkill skill, CooldownInstance instance) {
        cooldowns.put(skill.getSkillId(), instance);
    }

    public boolean isOnCooldown(ResourceLocation skillId, long gameTime) {
        CooldownInstance instance = cooldowns.get(skillId);
        return instance != null && !instance.isFinished(gameTime);
    }

    public int remainingTicks(ResourceLocation skillId, long gameTime) {
        CooldownInstance instance = cooldowns.get(skillId);
        return instance == null ? 0 : instance.remainingTicks(gameTime);
    }

    public boolean isEmpty() {
        return cooldowns.isEmpty();
    }

    public Map<ResourceLocation, CooldownInstance> view() {
        return cooldowns;
    }

    /**
     * Removes expired entries. Returns true if any were removed.
     */
    public boolean pruneExpired(long gameTime) {
        if (cooldowns.isEmpty()) {
            return false;
        }
        boolean changed = false;
        Iterator<Map.Entry<ResourceLocation, CooldownInstance>> it = cooldowns.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isFinished(gameTime)) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    public void clear() {
        cooldowns.clear();
    }

    public float getCooldownPercent(ResourceLocation skillId, long gameTime) {
        CooldownInstance instance = cooldowns.get(skillId);
        return instance == null ? 0 : instance.getCooldownPercent(gameTime);
    }

    public float getCooldownPercent(AbstractSkill skill, long gameTime) {
        ResourceLocation id = SkillcastingRegistries.SKILLS.getKey(skill);
        return id == null ? 0 : getCooldownPercent(id, gameTime);
    }

    public boolean hasCooldownsActive(long gameTime) {
        return cooldowns.values().stream().anyMatch(instance -> !instance.isFinished(gameTime));
    }

    @Nullable
    public CooldownInstance get(ResourceLocation skillId) {
        return cooldowns.get(skillId);
    }
}
