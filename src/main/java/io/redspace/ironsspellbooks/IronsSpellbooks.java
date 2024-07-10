package io.redspace.ironsspellbooks;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.*;
import io.redspace.ironsspellbooks.setup.ModSetup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
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

    public IronsSpellbooks(IEventBus modEventBus, ModContainer modContainer) {

        ModSetup.setup();

        MAGIC_MANAGER = new MagicManager();
        MagicHelper.MAGIC_MANAGER = MAGIC_MANAGER;

        modEventBus.addListener(ModSetup::init);
        modEventBus.addListener(OverlayRegistry::onRegisterOverlays);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        NeoForge.EVENT_BUS.register(this);

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
        DataAttachmentRegistry.register(modEventBus);
        ArmorMaterialRegistry.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addPackFinders);

        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC,"irons_spellbooks-client.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", IronsSpellbooks.MODID));
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", IronsSpellbooks.MODID));

    }

    private void clientSetup(final FMLClientSetupEvent e) {
        //TODO: 1.21: make sure json is being used
        //ItemBlockRenderTypes.setRenderLayer(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get(), RenderType.cutout());
        //ItemBlockRenderTypes.setRenderLayer(BlockRegistry.ARMOR_PILE_BLOCK.get(), RenderType.translucent());
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
        //Fixme: 1.21: this seems very different now
//        filename = "builtin_resource_packs/" + filename;
//        String id = "builtin/" + filename;
//        var resourcePath = ModList.get().getModFileById(MODID).getFile().findResource(filename);
//        var pack = Pack.readMetaAndCreate(id, displayName, false,
//                (path) -> new PathPackResources(new PackLocationInfo(), path, resourcePath), PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
//        event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
    }

//    private void setup(final FMLCommonSetupEvent event) {
//
//        // some preinit code
//        LOGGER.info("HELLO FROM PREINIT");
//        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
//    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        Curios.registerCurioSlot(Curios.RING_SLOT, 2, false, null);
        Curios.registerCurioSlot(Curios.NECKLACE_SLOT, 1, false, null);
        Curios.registerCurioSlot(Curios.SPELLBOOK_SLOT, 1, false, ResourceLocation.parse("curios:slot/spellbook_slot"));
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(IronsSpellbooks.MODID, path);
    }
}
