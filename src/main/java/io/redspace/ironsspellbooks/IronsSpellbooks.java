package io.redspace.ironsspellbooks;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.IronsSpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilScreen;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableScreen;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen;
import io.redspace.ironsspellbooks.registries.*;
import io.redspace.ironsspellbooks.setup.ModSetup;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import java.io.IOException;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(IronsSpellbooks.MODID)
public class IronsSpellbooks {
    // Directly reference a slf4j logger
    public static final String MODID = "irons_spellbooks";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MagicManager MAGIC_MANAGER;

    public IronsSpellbooks() {

        ModSetup.setup();

        MAGIC_MANAGER = new MagicManager();
        MagicHelper.MAGIC_MANAGER = MAGIC_MANAGER;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ModSetup::init);

        modEventBus.addListener(OverlayRegistry::onRegisterOverlays);

        IronsSpellRegistry.register(modEventBus);
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
        CommandArgumentRegistry.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addPackFinders);

        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC,"irons_spellbooks-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", IronsSpellbooks.MODID));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", IronsSpellbooks.MODID));

        //AttributesRegistry.register(eventBus);
        //MinecraftForge.EVENT_BUS.register(new AttributesRegistry().getClass());
        //MinecraftForge.EVENT_BUS.register(eventHandler);
        // Register the setup method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    @SuppressWarnings("removal")
    private void clientSetup(final FMLClientSetupEvent e) {
        MenuScreens.register(MenuRegistry.INSCRIPTION_TABLE_MENU.get(), InscriptionTableScreen::new);
        MenuScreens.register(MenuRegistry.SCROLL_FORGE_MENU.get(), ScrollForgeScreen::new);
        MenuScreens.register(MenuRegistry.ARCANE_ANVIL_MENU.get(), ArcaneAnvilScreen::new);

        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.ARMOR_PILE_BLOCK.get(), RenderType.translucent());
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
        var resourcePath = ModList.get().getModFileById(MODID).getFile().findResource(filename);
        var pack = new PathPackResources(ModList.get().getModFileById(MODID).getFile().getFileName() + ":" + resourcePath, resourcePath);
        var metadataSection = pack.getMetadataSection(PackMetadataSection.SERIALIZER);
        String id = "builtin/" + filename;
        if (metadataSection != null) {
            event.addRepositorySource((packConsumer, packConstructor) ->
                    packConsumer.accept(packConstructor.create(
                            id, displayName, false,
                            () -> pack, metadataSection, Pack.Position.TOP, PackSource.BUILT_IN, false)));
        }
    }

//    private void setup(final FMLCommonSetupEvent event) {
//
//        // some preinit code
//        LOGGER.info("HELLO FROM PREINIT");
//        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
//    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
//        InterModComms.sendTo("examplemod", "helloworld", () -> {
//            LOGGER.info("Hello world from the MDK");
//            return "Hello world";
//        });
        registerCurioSlot("ring", 2, false, null);
        registerCurioSlot("necklace", 1, false, null);
    }

    public static void registerCurioSlot(final String identifier, final int slots, final boolean isHidden, @Nullable final ResourceLocation icon) {
        final SlotTypeMessage.Builder message = new SlotTypeMessage.Builder(identifier);

        message.size(slots);

        if (isHidden) {
            message.hide();
        }

        if (icon != null) {
            message.icon(icon);
        }

        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> message.build());

    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

//    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
//    // Event bus for receiving Registry Events)
//    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
//    public static class RegistryEvents {
//        @SubscribeEvent
//        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
//            // Register a new block here
//            LOGGER.info("HELLO from Register Block");
//        }
//    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(IronsSpellbooks.MODID, path);
    }
}
