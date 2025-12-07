package io.redspace.ironsspellbooks;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.*;
import io.redspace.ironsspellbooks.setup.ModSetup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(IronsSpellbooks.MODID)
public class IronsSpellbooks {
    // Directly reference a slf4j logger
    public static final String MODID = "irons_spellbooks";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MagicManager MAGIC_MANAGER;

    public static MinecraftServer MCS;
    public static ServerLevel OVERWORLD;

    public IronsSpellbooks(/*IEventBus modEventBus, ModContainer modContainer*/) {

        ModSetup.setup();

        MAGIC_MANAGER = new MagicManager();
        MagicHelper.MAGIC_MANAGER = MAGIC_MANAGER;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ModSetup::init);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
//        modEventBus.addListener(SchoolRegistry::registerRegistry);
//        modEventBus.addListener(SpellRegistry::registerRegistry);
        modEventBus.addListener(UpgradeOrbTypeRegistry::registerDatapackRegistries);
        //MinecraftForge.EVENT_BUS.register(this);

        //TODO: custom annotation would be nice
        SchoolRegistry.register(modEventBus);
        SpellRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        AttributeRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);
        LootRegistry.register(modEventBus);
        MobEffectRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        FeatureRegistry.register(modEventBus);
        PotionRegistry.register(modEventBus);
        CommandArgumentRegistry.register(modEventBus);
        StructureProcessorRegistry.register(modEventBus);
        StructureElementRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
//        DataAttachmentRegistry.register(modEventBus);
//        ExtendedArmorMaterials.register(modEventBus);
//        ComponentRegistry.register(modEventBus);
        PoiTypeRegistry.register(modEventBus);
        FluidRegistry.register(modEventBus);
        RecipeRegistry.register(modEventBus);

        modEventBus.addListener(this::addPackFinders);
        MinecraftForge.EVENT_BUS.addListener(this::addServerDataListeners);

        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC,"irons_spellbooks-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", IronsSpellbooks.MODID));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", IronsSpellbooks.MODID));

    }

    public void addServerDataListeners(AddReloadListenerEvent event) {
        SpellConfigManager.INSTANCE = new SpellConfigManager();
        event.addListener(SpellConfigManager.INSTANCE);
    }

    public void addPackFinders(AddPackFindersEvent event) {
        IronsSpellbooks.LOGGER.debug("addPackFinders");
        try {
            if (event.getPackType() == PackType.CLIENT_RESOURCES) {
                addBuiltinPack(event, "legacy_dead_king_resource_pack", Component.literal("Legacy Dead King"));
            }
        } catch (IOException ex) {
            IronsSpellbooks.LOGGER.error("Failed to load a builtin resource pack! If you are seeing this message, please report an issue to https://github.com/iron431/Irons-Spells-n-Spellbooks/issues");
            // throw new RuntimeException(ex);
        }
    }

    private static void addBuiltinPack(AddPackFindersEvent event, String filename, Component displayName) throws IOException {
        filename = "builtin_resource_packs/" + filename;
        String id = "builtin/" + filename;
        var resourcePath = ModList.get().getModFileById(MODID).getFile().findResource(filename);
        var pack = Pack.readMetaAndCreate(id, displayName, false,
                (path) -> new PathPackResources(path, true, resourcePath), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
        event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {

    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, path);
    }
}
