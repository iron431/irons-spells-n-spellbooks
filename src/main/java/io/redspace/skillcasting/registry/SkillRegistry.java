package io.redspace.skillcasting.registry;

import io.redspace.skillcasting.api.skill.AbstractSkill;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;


public final class SkillRegistry {
    public static Holder<AbstractSkill> holder(ResourceLocation id) {
        return holder(get(id));
    }

    public static Holder<AbstractSkill> holder(AbstractSkill skill) {
        return SkillcastingRegistries.SKILL_REGISTRY.wrapAsHolder(skill);
    }

    public static ResourceLocation id(AbstractSkill skill) {
        return SkillcastingRegistries.SKILL_REGISTRY.getKey(skill);
    }

    public static AbstractSkill get(ResourceLocation id) {
        return SkillcastingRegistries.SKILL_REGISTRY.get(id);
    }

}
