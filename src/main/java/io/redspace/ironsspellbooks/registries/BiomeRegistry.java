package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BiomeRegistry {
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, IronsSpellbooks.MODID);

    public static final String ICE_CAVES = "ice_caves";
    public static final ResourceKey<Biome> ICE_CAVES_KEY = ResourceKey.create(ForgeRegistries.Keys.BIOMES, new ResourceLocation(IronsSpellbooks.MODID, ICE_CAVES));

    //RegistryObject<Biome> ice = ;

    public static RegistryObject<Biome> registerBiome(String id, Supplier<Biome> biomeSupplier) {
        return BIOMES.register(id, biomeSupplier);
    }

    public static void register(IEventBus eventBus) {
        BIOMES.register(eventBus);
        // this goes here.
        BiomeRegistry.registerBiome(BiomeRegistry.ICE_CAVES, OverworldBiomes::lushCaves);
    }
}
