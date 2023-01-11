package com.example.testmod.datagen;

import com.example.testmod.TestMod;
import com.example.testmod.registries.ResigterBiomeTags;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomBiomeTags extends TagsProvider<Biome> {

    public CustomBiomeTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, BuiltinRegistries.BIOME, TestMod.MODID, helper);
    }

    @Override
    protected void addTags() {
        ForgeRegistries.BIOMES.getValues().forEach(biome -> {
            tag(ResigterBiomeTags.HAS_TOWER).add(biome);
        });
    }

    @Override
    public String getName() {
        return TestMod.MODID + " Tags";
    }
}
