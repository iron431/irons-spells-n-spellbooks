package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class FeatureRegistry {
    private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, IronsSpellbooks.MODID);
    private static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registries.PLACED_FEATURE, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        CONFIGURED_FEATURES.register(eventBus);
        PLACED_FEATURES.register(eventBus);
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> MITHRIL_ORE_FEATURE = configuredFeatureResourceKey("ore_mithril_feature");
    public static final ResourceKey<PlacedFeature> MITHRIL_ORE_PLACEMENT = placedFeatureResourceKey("ore_mithril_placement");
    public static final ResourceKey<BiomeModifier> ADD_MITHRIL_TO_BIOMES = biomeModifierResourceKey("add_mithril_ore");

    public static void bootstrapConfiguredFeature(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        //Rules for what ore should replace what block type
        RuleTest deepslateTest = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest stoneTest = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);

        //Create target with rules and ore blockstates
        List<OreConfiguration.TargetBlockState> arcaneDebrisList = List.of(
                OreConfiguration.target(stoneTest, BlockRegistry.MITHRIL_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateTest, BlockRegistry.MITHRIL_ORE_DEEPSLATE.get().defaultBlockState())
        );

        //Register Feature
        FeatureUtils.register(context, MITHRIL_ORE_FEATURE, Feature.ORE, new OreConfiguration(arcaneDebrisList, 3, 1.0f));
    }

    public static void bootstrapPlacedFeature(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(CONFIGURED_FEATURES.getRegistryKey());
        //Get feature
        Holder<ConfiguredFeature<?, ?>> holderArcaneDebris = holdergetter.getOrThrow(MITHRIL_ORE_FEATURE);
        //Create placement. We want the ore to generate from Y = -63 to -38. The ore generates uniformly across this range. 7 is an arbitrary rarity. Biome filter allows us to potentially limit it to certain biomes
        List<PlacementModifier> list = List.of(CountPlacement.of(7), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(-63), VerticalAnchor.absolute(-38)), BiomeFilter.biome());
        //Register Placement
        PlacementUtils.register(context, MITHRIL_ORE_PLACEMENT, holderArcaneDebris, list);
    }

    public static void bootstrapBiomeModifier(final BootstrapContext<BiomeModifier> context) {
        final var biomes = context.lookup(Registries.BIOME);
        final var features = context.lookup(PLACED_FEATURES.getRegistryKey());

        //Register a biome addition of our placement, in any overworld biome
        context.register(ADD_MITHRIL_TO_BIOMES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        tag(biomes, BiomeTags.IS_OVERWORLD),
                        feature(features, MITHRIL_ORE_PLACEMENT),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureResourceKey(final String name) {
        return ResourceKey.create(CONFIGURED_FEATURES.getRegistryKey(), new ResourceLocation(IronsSpellbooks.MODID, name));
    }

    private static ResourceKey<PlacedFeature> placedFeatureResourceKey(final String name) {
        return ResourceKey.create(PLACED_FEATURES.getRegistryKey(), new ResourceLocation(IronsSpellbooks.MODID, name));
    }

    private static ResourceKey<BiomeModifier> biomeModifierResourceKey(final String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(IronsSpellbooks.MODID, name));
    }

    private static HolderSet<Biome> tag(final HolderGetter<Biome> holderGetter, final TagKey<Biome> key) {
        return holderGetter.getOrThrow(key);
    }

    private static HolderSet<PlacedFeature> feature(final HolderGetter<PlacedFeature> holderGetter, final ResourceKey<PlacedFeature> feature) {
        return HolderSet.direct(holderGetter.getOrThrow(feature));
    }
}

