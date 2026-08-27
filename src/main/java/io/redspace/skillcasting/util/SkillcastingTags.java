package io.redspace.skillcasting.util;

import io.redspace.skillcasting.Skillcasting;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class SkillcastingTags {
    public static final TagKey<EntityType<?>> CANT_RICOCHET = TagKey.create(Registries.ENTITY_TYPE, Skillcasting.id("cant_ricochet"));
}
