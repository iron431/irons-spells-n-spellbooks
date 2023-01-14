package com.example.testmod;

import com.example.testmod.gui.inscription_table.InscriptionTableScreen;
import com.example.testmod.gui.scroll_forge.ScrollForgeScreen;
import com.example.testmod.registries.*;
import com.example.testmod.setup.ModSetup;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TestMod.MODID)
public class TestMod {
    // Directly reference a slf4j logger
    public static final String MODID = "testmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    //public static InstancedEventHandler eventHandler = new InstancedEventHandler();
    public TestMod() {

        ModSetup.setup();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ModSetup::init);

        modEventBus.addListener(OverlayRegistry::onRegisterOverlays);

        ItemRegistry.register(modEventBus);
        AttributeRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);
        LootRegistry.register(modEventBus);
        MobEffectRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);


        modEventBus.addListener(this::clientSetup);
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

    private void clientSetup(final FMLClientSetupEvent e) {

        //TODO: find a better place for this
        MenuScreens.register(MenuRegistry.INSCRIPTION_TABLE_MENU.get(), InscriptionTableScreen::new);
        MenuScreens.register(MenuRegistry.SCROLL_FORGE_MENU.get(), ScrollForgeScreen::new);

        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get(), RenderType.cutout());

    }

//    private void setup(final FMLCommonSetupEvent event) {
//
//        // some preinit code
//        LOGGER.info("HELLO FROM PREINIT");
//        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
//    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
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
        return new ResourceLocation(TestMod.MODID, path);
    }
}
