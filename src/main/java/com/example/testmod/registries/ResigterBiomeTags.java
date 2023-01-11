package com.example.testmod.registries;

import com.example.testmod.TestMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ResigterBiomeTags {
    public static final TagKey<Biome> HAS_TOWER = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(TestMod.MODID, "has_structure/tower"));

}
