package io.redspace.skillcasting.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class SkillcastingTags {
    public static final TagKey<EntityType<?>> CANT_RICOCHET = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "cant_ricochet"));
}
