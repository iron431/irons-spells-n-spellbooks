package io.redspace.skillcasting.cooldown;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CooldownManager {
    public static final Codec<CooldownManager> CODEC = Codec.unboundedMap(SkillcastingRegistries.SKILL_HOLDER_CODEC, CooldownInstance.CODEC)
            .xmap(CooldownManager::fromMap, CooldownManager::toMap);

    private final Map<Holder<AbstractSkill>, CooldownInstance> cooldowns = new HashMap<>();

    private static CooldownManager fromMap(Map<Holder<AbstractSkill>, CooldownInstance> map) {
        CooldownManager manager = new CooldownManager();
        manager.cooldowns.putAll(map);
        return manager;
    }

    private Map<Holder<AbstractSkill>, CooldownInstance> toMap() {
        return Map.copyOf(cooldowns);
    }

    public void replaceFrom(CooldownManager other) {
        cooldowns.clear();
        cooldowns.putAll(other.cooldowns);
    }

    public void replaceFrom(Map<Holder<AbstractSkill>, CooldownInstance> synced) {
        cooldowns.clear();
        cooldowns.putAll(synced);
    }

    public void addCooldown(Holder<AbstractSkill> skill, CooldownInstance instance) {
        cooldowns.put(skill, instance);
    }

    public void addCooldown(AbstractSkill skill, CooldownInstance instance) {
        addCooldown(SkillcastingRegistries.SKILLS.wrapAsHolder(skill), instance);
    }

    public boolean isOnCooldown(Holder<AbstractSkill> skill) {
        CooldownInstance instance = cooldowns.get(skill);
        return instance != null && !instance.isFinished();
    }

    public boolean isOnCooldown(AbstractSkill skill) {
        return isOnCooldown(SkillcastingRegistries.SKILLS.wrapAsHolder(skill));
    }

    public int remainingTicks(Holder<AbstractSkill> skill) {
        CooldownInstance instance = cooldowns.get(skill);
        return instance == null ? 0 : instance.remainingTicks();
    }

    public int remainingTicks(AbstractSkill skill) {
        return remainingTicks(SkillcastingRegistries.SKILLS.wrapAsHolder(skill));
    }

    public boolean isEmpty() {
        return cooldowns.isEmpty();
    }

    public Map<Holder<AbstractSkill>, CooldownInstance> view() {
        return cooldowns;
    }

    /**
     * Decrements all active cooldowns and removes finished entries.
     *
     * @return true if any entry was removed
     */
    public boolean tick() {
        if (cooldowns.isEmpty()) {
            return false;
        }
        boolean changed = false;
        Iterator<Map.Entry<Holder<AbstractSkill>, CooldownInstance>> it = cooldowns.entrySet().iterator();
        while (it.hasNext()) {
            CooldownInstance instance = it.next().getValue();
            instance.tick();
            if (instance.isFinished()) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    public void clear() {
        cooldowns.clear();
    }

    public float getCooldownPercent(Holder<AbstractSkill> skill) {
        CooldownInstance instance = cooldowns.get(skill);
        return instance == null ? 0 : instance.getCooldownPercent();
    }

    public float getCooldownPercent(AbstractSkill skill) {
        return getCooldownPercent(SkillcastingRegistries.SKILLS.wrapAsHolder(skill));
    }

    public boolean hasCooldownsActive() {
        return !cooldowns.isEmpty();
    }

    @Nullable
    public CooldownInstance get(Holder<AbstractSkill> skill) {
        return cooldowns.get(skill);
    }

    @Nullable
    public CooldownInstance get(AbstractSkill skill) {
        return get(SkillcastingRegistries.SKILLS.wrapAsHolder(skill));
    }

    public void applySynced(Holder<AbstractSkill> skill, @Nullable CooldownInstance instance) {
        if (instance == null) {
            cooldowns.remove(skill);
        } else {
            cooldowns.put(skill, instance);
        }
    }
}
