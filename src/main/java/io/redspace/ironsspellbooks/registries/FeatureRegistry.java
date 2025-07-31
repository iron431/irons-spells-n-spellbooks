package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FeatureRegistry {
    private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, IronsSpellbooks.MODID);
    private static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registries.PLACED_FEATURE, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        CONFIGURED_FEATURES.register(eventBus);
        PLACED_FEATURES.register(eventBus);
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> ARCANE_DEBRIS_FEATURE = configuredFeatureResourceKey("ore_arcane_debris");
    public static final ResourceKey<PlacedFeature> ARCANE_DEBRIS_PLACEMENT = placedFeatureResourceKey("ore_arcane_debris");
    public static final ResourceKey<BiomeModifier> ADD_ARCANE_DEBRIS_ORE = biomeModifierResourceKey("add_arcane_debris_ore");

    public static void bootstrapConfiguredFeature(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest ruleTestArcaneDebris = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        List<OreConfiguration.TargetBlockState> arcaneDebrisList = List.of(OreConfiguration.target(ruleTestArcaneDebris, BlockRegistry.ARCANE_DEBRIS.get().defaultBlockState()));
        FeatureUtils.register(context, ARCANE_DEBRIS_FEATURE, Feature.ORE, new OreConfiguration(arcaneDebrisList, 3, 1.0f));
    }

    public static void bootstrapPlacedFeature(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(CONFIGURED_FEATURES.getRegistryKey());
        Holder<ConfiguredFeature<?, ?>> holderArcaneDebris = holdergetter.getOrThrow(ARCANE_DEBRIS_FEATURE);
        List<PlacementModifier> list = List.of(CountPlacement.of(7), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(-63), VerticalAnchor.absolute(-38)), BiomeFilter.biome());
        PlacementUtils.register(context, ARCANE_DEBRIS_PLACEMENT, holderArcaneDebris, list);
    }

    public static void bootstrapBiomeModifier(final BootstapContext<BiomeModifier> context) {
        final var biomes = context.lookup(ForgeRegistries.BIOMES.getRegistryKey());
        final var features = context.lookup(PLACED_FEATURES.getRegistryKey());

        context.register(ADD_ARCANE_DEBRIS_ORE,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        tag(biomes, BiomeTags.IS_OVERWORLD),
                        feature(features, ARCANE_DEBRIS_PLACEMENT),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureResourceKey(final String name) {
        return ResourceKey.create(CONFIGURED_FEATURES.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, name));
    }

    private static ResourceKey<PlacedFeature> placedFeatureResourceKey(final String name) {
        return ResourceKey.create(PLACED_FEATURES.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, name));
    }

    private static ResourceKey<BiomeModifier> biomeModifierResourceKey(final String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, name));
    }

    private static HolderSet<Biome> tag(final HolderGetter<Biome> holderGetter, final TagKey<Biome> key) {
        return holderGetter.getOrThrow(key);
    }

    private static HolderSet<PlacedFeature> feature(final HolderGetter<PlacedFeature> holderGetter, final ResourceKey<PlacedFeature> feature) {
        return HolderSet.direct(holderGetter.getOrThrow(feature));
    }
}

