package io.redspace.ironsspellbooks.datagen;

import io.netty.util.concurrent.CompleteFuture;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.ResigterBiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.CompletableFuture;

public class CustomBiomeTags extends TagsProvider<Biome> {

    public CustomBiomeTags(DataGenerator generator, CompletableFuture<HolderLookup.Provider> providerCompleteFuture, ExistingFileHelper helper) {
        super(generator.getPackOutput(), Registries.BIOME, providerCompleteFuture, IronsSpellbooks.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        ForgeRegistries.BIOMES.getValues().forEach(biome -> {
            ForgeRegistries.BIOMES.getResourceKey(biome).ifPresent(key -> {
                tag(ResigterBiomeTags.HAS_TOWER).add(key);
            });
        });
    }

    @Override
    public String getName() {
        return IronsSpellbooks.MODID + " Tags";
    }
}
