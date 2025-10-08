package io.redspace.ironsspellbooks.api.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.network.SyncJsonConfigPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@EventBusSubscriber
public class SpellConfigManager extends SimpleJsonResourceReloadListener {
    private final Gson gson;
    private byte[] lastRead = null;

    private static File resolveConfigFile(MinecraftServer server) {
        var serverconfig = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(SUBCONFIG_FOLDER).resolve(CONFIG_FILE).toFile();
        if (serverconfig.exists()) {
            // give precedence to local save/server config
            return serverconfig;
        } else {
            // otherwise, give main config file
            return FMLPaths.CONFIGDIR.get().resolve(SUBCONFIG_FOLDER).resolve(CONFIG_FILE).toFile();
        }
    }

    private byte[] readBytes(File file) {
        try (FileReader reader = new FileReader(file)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString().replaceAll("[ \n]", "").getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("Failed to read config file: {}", e.getMessage());
            return new byte[]{};
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        var player = event.getPlayer();

        if (IronsSpellbooks.CONFIG_MANAGER.dirty) {
            IronsSpellbooks.CONFIG_MANAGER.dirty = false;
            // server datapack load/reload
            var configFile = resolveConfigFile(server);
            if (configFile.exists()) {
                byte[] bytes = IronsSpellbooks.CONFIG_MANAGER.readBytes(configFile);
                IronsSpellbooks.CONFIG_MANAGER.buildConfigManager(bytes);
                IronsSpellbooks.CONFIG_MANAGER.lastRead = bytes;
            }
        }
        if (IronsSpellbooks.CONFIG_MANAGER.lastRead != null) {
            try {
                if (player != null) {
                    PacketDistributor.sendToPlayer(player, new SyncJsonConfigPacket(IronsSpellbooks.CONFIG_MANAGER.lastRead));
                } else {
                    // todo:
                    //  sync to all? singleplayer? when does this actually happen?
                }
            } catch (IOException e) {
                IronsSpellbooks.LOGGER.error("Failed to sync config to players: {}", e.getMessage());
            }
        } else {
            IronsSpellbooks.LOGGER.warn("Failed to sync config to players, buffer is null");
        }
    }

    //TODO: rename this stuff
    public static final String JSON_HEADER = "config";
    public static final String SUBCONFIG_FOLDER = "irons_spellbooks_spells";
    public static final String CONFIG_FILE = "config.json";
    public static final String ID_FIELD = "id";

    private static final Set<SpellConfigParameter<?>> ALL_TYPES = new HashSet<>();
    private static boolean registered = false;
    private boolean dirty = true;

    private static void registerConfigParameterTypes() {
        if (!registered) {
            registered = true;
            ALL_TYPES.add(IronConfigParameters.SCHOOL);
            ALL_TYPES.add(IronConfigParameters.MIN_RARITY);
            ALL_TYPES.add(IronConfigParameters.MAX_LEVEL);
            ALL_TYPES.add(IronConfigParameters.ENABLED);
            ALL_TYPES.add(IronConfigParameters.COOLDOWN_IN_SECONDS);
            ALL_TYPES.add(IronConfigParameters.ALLOW_CRAFTING);
            ALL_TYPES.add(IronConfigParameters.MANA_MULTIPLIER);
            ALL_TYPES.add(IronConfigParameters.POWER_MULTIPLIER);
            NeoForge.EVENT_BUS.post(new RegisterConfigParametersEvent(ALL_TYPES::add));
        }
    }

//    @SubscribeEvent
//    public static void testevent1(ModifyDefaultConfigValuesEvent event) {
//        if (event.getSpell().getSchoolType() == SchoolRegistry.BLOOD.get()) {
//            event.setConfigValue(IronConfigParameters.SCHOOL, SchoolRegistry.ELDRITCH.get());
//        }
//    }

    //
//    @SubscribeEvent
//    public static void testevent2(RegisterConfigParametersEvent event) {
//        event.register(new SpellConfigParameter<Boolean>(ResourceLocation.withDefaultNamespace("test"), Codec.BOOL, false));
//    }
//
    public SpellConfigManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "nodir");
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        handleServerConfigUpdate();
    }

    private static ImmutableMap<AbstractSpell, SpellConfigHolder> INSTANCE;

    public void handleServerConfigUpdate() {
        registerConfigParameterTypes();
        File configFile = initiateOrGetConfig(gson);
        dirty = true;
    }

    public void buildConfigManager(byte[] filestream) {
        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        Map<ResourceLocation, JsonObject> configEntries = new HashMap<>();
        JsonObject root = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(filestream)), JsonObject.class);
        JsonArray array = root.getAsJsonArray(JSON_HEADER);
        for (JsonElement elem : array) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                try {
                    if (!obj.has(ID_FIELD)) {
                        throw new JsonParseException("No member \"id\" found!");
                    }
                    ResourceLocation id = ResourceLocation.parse(obj.get(ID_FIELD).getAsString());
                    configEntries.put(id, obj);
                } catch (Exception e) {
                    IronsSpellbooks.LOGGER.error("Failed to parse id of config entry: {}", e.getMessage());
                }
            }
        }

        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            // Build defaults
            SpellConfigHolder config = new SpellConfigHolder();
            DefaultConfig raw = spell.getDefaultConfig();
            config.set(IronConfigParameters.SCHOOL, SchoolRegistry.getSchool(raw.schoolResource));
            config.set(IronConfigParameters.MIN_RARITY, raw.minRarity);
            config.set(IronConfigParameters.MAX_LEVEL, raw.maxLevel);
            config.set(IronConfigParameters.ENABLED, raw.enabled);
            config.set(IronConfigParameters.COOLDOWN_IN_SECONDS, raw.cooldownInSeconds);
            config.set(IronConfigParameters.ALLOW_CRAFTING, raw.allowCrafting);
            // Handle user-specified Overrides
            ResourceLocation spellId = spell.getSpellResource();
            if (configEntries.containsKey(spellId)) {
                try {
                    JsonObject json = configEntries.get(spellId);
                    for (SpellConfigParameter<?> paramType : ALL_TYPES) {
                        Optional<JsonElement> elem = resolveJsonElement(spellId, paramType, json);
                        if (elem.isPresent()) {
                            var decoded = paramType.datatype().decode(registryops, elem.get()).getOrThrow().getFirst();
                            config.set((SpellConfigParameter) paramType, decoded);
                        }
                    }
                } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                    IronsSpellbooks.LOGGER.error("Parsing error loading spell config {}: {}", spellId, jsonparseexception);
                }
            }
            builder.put(spell, config);
        }
        INSTANCE = builder.build();
        // Second pass for events. Allows for data-completion (can reference existing config values), and higher context (avoid overriding user-input)
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            NeoForge.EVENT_BUS.post(new ModifyDefaultConfigValuesEvent(spell, INSTANCE.get(spell)));
        }
    }

    public static File initiateOrGetConfig(Gson gson) {
        //todo: real error handling
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path spellConfigDir = configDir.resolve(SUBCONFIG_FOLDER);
        File folder = spellConfigDir.toFile();
        if (!folder.exists()) {
            folder.mkdir();
        }
        File config = spellConfigDir.resolve(CONFIG_FILE).toFile();
        if (!config.exists()) {
            JsonArray allDefaultConfig = new JsonArray(1);
            allDefaultConfig.add(createExampleConfig(gson));
            try (FileWriter writer = new FileWriter(config)) {
                gson.toJson(Map.of(JSON_HEADER, allDefaultConfig), writer);
            } catch (IOException e) {
                IronsSpellbooks.LOGGER.error("Failed to write default config file {}: {}", config.getPath(), e.getMessage());
            }
        }
        return config;
    }

    private static JsonElement createExampleConfig(Gson gson) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(ID_FIELD, "irons_spellbooks:example");
        for (SpellConfigParameter param : SpellConfigManager.ALL_TYPES) {
            var codec = param.datatype();
            DataResult<?> result = codec.encodeStart(JsonOps.INSTANCE, param.defaultValue());
            jsonObject.add(param.key().toString(), gson.toJsonTree(result.getOrThrow()));
        }
        return jsonObject;
    }

    private static Optional<JsonElement> resolveJsonElement(ResourceLocation spellId, SpellConfigParameter<?> dataType, JsonObject parent) {
        if (parent.has(dataType.key().toString())) {
            return Optional.of(parent.get(dataType.key().toString()));
        } else if (parent.has(dataType.key().getPath())) {
            if (!dataType.key().getNamespace().equals("irons_spellbooks")) {
                IronsSpellbooks.LOGGER.warn("Config for {} has ambiguous entry \"{}\", adapting to \"{}\"", spellId, dataType.key().getPath(), dataType.key());
            }
            return Optional.of(parent.get(dataType.key().getPath()));
        } else {
            return Optional.empty();
        }
    }

    public static <T> T getSpellConfigValue(AbstractSpell spell, SpellConfigParameter<T> parameterType) {
        if (!INSTANCE.containsKey(spell)) {
            return parameterType.defaultValue();
        }
        return INSTANCE.get(spell).get(parameterType).orElse(parameterType.defaultValue());
    }
}
