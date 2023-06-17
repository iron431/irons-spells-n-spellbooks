package io.redspace.ironsspellbooks.datagen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.DamageTypeRegistry;
import io.redspace.ironsspellbooks.registries.FeatureRegistry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = IronsSpellbooks.MODID)
//https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/84da4e8cf335daacf033a497c82ef8b0fb3d0076/src/main/java/choonster/testmod3/init/ModDataProviders.java#L70
public class DataGenHandler {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            //.add(Registries.CONFIGURED_CARVER, FeatureRegistry::bootstrapCarver)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, FeatureRegistry::bootstrapBiomeModifier)
            .add(Registries.CONFIGURED_FEATURE, FeatureRegistry::bootstrapConfiguredFeature)
            .add(Registries.PLACED_FEATURE, FeatureRegistry::bootstrapPlacedFeature)
            .add(Registries.DAMAGE_TYPE, DamageTypeRegistry::bootstrap);

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        //ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), BUILDER, Set.of(IronsSpellbooks.MODID)));
    }
}
