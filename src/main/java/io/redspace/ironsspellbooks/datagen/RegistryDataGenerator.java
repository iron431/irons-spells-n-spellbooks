package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.FeatureRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistryDataGenerator extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, FeatureRegistry::bootstrapBiomeModifier)
            .add(Registries.CONFIGURED_FEATURE, FeatureRegistry::bootstrapConfiguredFeature)
            .add(Registries.PLACED_FEATURE, FeatureRegistry::bootstrapPlacedFeature)
            .add(Registries.DAMAGE_TYPE, ISSDamageTypes::bootstrap);

    public RegistryDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, BUILDER, Set.of("minecraft", IronsSpellbooks.MODID));
    }
}
