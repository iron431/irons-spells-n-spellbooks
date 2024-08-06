package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = event.getGenerator().getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        var provider = event.getLookupProvider();

        DatapackBuiltinEntriesProvider datapackProvider = new RegistryDataGenerator(output, provider);
        CompletableFuture<HolderLookup.Provider> lookupProvider = datapackProvider.getRegistryProvider();
        generator.addProvider(event.includeServer(), datapackProvider);
        generator.addProvider(event.includeServer(), new DamageTypeTagGenerator(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new LootTableProvider(output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(IronLootTableProviders.Block::new, LootContextParamSets.BLOCK)),
                provider
        ));

        //pack.mcmeta
        generator.addProvider(true, new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(
                Component.literal("Resources for Iron's Spells N Spellbooks"),
                DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
                Optional.empty())));
    }
}
