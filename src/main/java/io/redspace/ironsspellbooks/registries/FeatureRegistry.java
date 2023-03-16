package io.redspace.ironsspellbooks.registries;

import com.google.common.base.Suppliers;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class FeatureRegistry {
    private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, IronsSpellbooks.MODID);
    private static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        CONFIGURED_FEATURES.register(eventBus);
        PLACED_FEATURES.register(eventBus);
    }

    /*
        Arcane Debris
     */
    //What blocks the ore can generate in
    public static final Supplier<List<OreConfiguration.TargetBlockState>> ARCANE_DEBRIS_ORE_TARGET = Suppliers.memoize(() -> List.of(
            OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, BlockRegistry.ARCANE_DEBRIS.get().defaultBlockState())
    ));
    //Vein size/conditions (this ore cannot spawn exposed to air)
    public static final RegistryObject<ConfiguredFeature<?, ?>> ORE_ARCANE_DEBRIS = CONFIGURED_FEATURES.register("ore_arcane_debris",
            () -> new ConfiguredFeature<>(Feature.SCATTERED_ORE, new OreConfiguration(ARCANE_DEBRIS_ORE_TARGET.get(), 3, 1.0f)));

    public static final RegistryObject<PlacedFeature> ORE_ARCANE_DEBRIS_FEATURE = PLACED_FEATURES.register("ore_arcane_debris_feature",
            () -> new PlacedFeature(ORE_ARCANE_DEBRIS.getHolder().get(),
                    List.of(InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-38)))));

    //Copied private helpers from OrePlacements
    private static List<PlacementModifier> orePlacement(PlacementModifier p_195347_, PlacementModifier p_195348_) {
        return List.of(p_195347_, InSquarePlacement.spread(), p_195348_, BiomeFilter.biome());
    }

    private static List<PlacementModifier> commonOrePlacement(int pCount, PlacementModifier pHeightRange) {
        return orePlacement(CountPlacement.of(pCount), pHeightRange);
    }

    private static List<PlacementModifier> rareOrePlacement(int pChance, PlacementModifier pHeightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(pChance), pHeightRange);
    }
}
