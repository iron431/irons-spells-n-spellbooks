package io.redspace.ironsspellbooks.worldgen;

import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber()
public class VillageAddition {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.fromNamespaceAndPath("minecraft", "empty"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          Registry<StructureProcessorList> processorListRegistry,
                                          ResourceLocation poolRL,
                                          String nbtPieceRL,
                                          int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        // Use .legacy( for villages/outposts and .single( for everything else
        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL,
                emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
//        pool.rawTemplates.forEach((pair) ->
//                IronsSpellbooks.LOGGER.debug("{}: {}", pair.getFirst().toString(), pair.getSecond()));
    }

    /**
     * We use FMLServerAboutToStartEvent as the dynamic registry exists now and all JSON worldgen files were parsed.
     * Mod compat is best done here.
     */
    @SubscribeEvent
    public static void addNewVillageBuilding(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();

        // Adds our piece to all village houses pool
        // Note, the resourcelocation is getting the pool files from the data folder. Not assets folder.
        int weight = ServerConfigs.PRIEST_TOWER_SPAWNRATE.get();
//        if (weight == ServerConfigs.PRIEST_TOWER_SPAWNRATE.getDefault() && FMLLoader.getLoadingModList().getModFileById("bettervillage") != null)
//            weight = 2;
        //IronsSpellbooks.LOGGER.debug("ServerAboutToStartEvent: addNewVillageBuilding {}", ServerConfigs.PRIEST_TOWER_SPAWNRATE.get());
        if (weight <= 0)
            return;

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.parse("minecraft:village/plains/houses"),
                "irons_spellbooks:priest_house", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.parse("minecraft:village/taiga/houses"),
                "irons_spellbooks:priest_house_taiga", weight);


        //addBuildingToPool(templatePoolRegistry, processorListRegistry,
        //        ResourceLocation.fromNamespaceAndPath("minecraft:village/snowy/houses"),
        //        "modid:structure_nbt_resourcelocation", 5);
        //
        //addBuildingToPool(templatePoolRegistry, processorListRegistry,
        //        ResourceLocation.fromNamespaceAndPath("minecraft:village/savanna/houses"),
        //        "modid:structure_nbt_resourcelocation", 5);
        //
        //addBuildingToPool(templatePoolRegistry, processorListRegistry,
        //        ResourceLocation.fromNamespaceAndPath("minecraft:village/taiga/houses"),
        //        "modid:structure_nbt_resourcelocation", 5);
        //
        //addBuildingToPool(templatePoolRegistry, processorListRegistry,
        //        ResourceLocation.fromNamespaceAndPath("minecraft:village/desert/houses"),
        //        "modid:structure_nbt_resourcelocation", 5);
    }
}
