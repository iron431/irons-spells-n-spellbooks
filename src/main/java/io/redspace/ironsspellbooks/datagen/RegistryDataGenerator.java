package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.FeatureRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RegistryDataGenerator extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, FeatureRegistry::bootstrapBiomeModifier)
            .add(Registries.CONFIGURED_FEATURE, FeatureRegistry::bootstrapConfiguredFeature)
            .add(Registries.PLACED_FEATURE, FeatureRegistry::bootstrapPlacedFeature)
            .add(Registries.DAMAGE_TYPE, ISSDamageTypes::bootstrap);

    // Use addProviders() instead
    public RegistryDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, BUILDER, Set.of("minecraft", IronsSpellbooks.MODID));
    }

//    public static void addProviders(boolean isServer, DataGenerator generator, PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper) {
//        generator.addProvider(isServer, new RegistryDataGenerator(output, provider));
//        // This is needed here because Minecraft Forge doesn't properly support tagging custom registries, without problems.
//        // If you think this looks fixable, please ensure the fixes are tested in runData & runClient as these current issues exist entirely within Forge's internals.
//        generator.addProvider(isServer, new DamageTypeTagGenerator(output, provider.thenApply(r -> append(r, BUILDER)), helper));
//    }
//
//    private static HolderLookup.Provider append(HolderLookup.Provider original, RegistrySetBuilder builder) {
//        return builder.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original, );
//    }
}
