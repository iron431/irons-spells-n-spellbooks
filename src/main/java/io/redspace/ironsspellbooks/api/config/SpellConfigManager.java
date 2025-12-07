package io.redspace.ironsspellbooks.api.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.network.SyncJsonConfigPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@Mod.EventBusSubscriber
public class SpellConfigManager extends SimpleJsonResourceReloadListener {

    /*
     * API Accessible
     */
    public static final String SUBCONFIG_FOLDER = "irons_spellbooks_spell_config";
    public static SpellConfigManager INSTANCE = new SpellConfigManager();

    public static SpellConfigManager getInstance() {
        return INSTANCE;
    }

    /**
     * @return The spell's active configuration value for this world, or the parameter's default if none is defined.
     */
    public static <T> T getSpellConfigValue(AbstractSpell spell, SpellConfigParameter<T> parameterType) {
        if (!INSTANCE.config.containsKey(spell)) {
            return parameterType.defaultValue();
        }
        return INSTANCE.config.get(spell).get(parameterType);
    }

    /**
     * @return The spell's default preset configuration value for this parameter, or the parameter's default if none is defined.
     */
    public static <T> T getSpellDefaultConfigValue(AbstractSpell spell, SpellConfigParameter<T> parameterType) {
        if (!INSTANCE.config.containsKey(spell)) {
            return parameterType.defaultValue();
        }
        return INSTANCE.config.get(spell).getDefaultValue(parameterType).orElse(parameterType.defaultValue());
    }

    /*
     * Implementation
     */
    private final Gson gson;
    @Nullable
    private Map<ResourceLocation, JsonElement> datapackOverride = null;
    private ImmutableMap<AbstractSpell, SpellConfigHolder> config;


    public SpellConfigManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_spellbooks_spell_config");
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (!object.isEmpty()) {
            datapackOverride = object;
        }
        handleServerConfigUpdate();
    }


    public void handleServerConfigUpdate() {
        registerConfigParameterTypes();
        initiateDefaultFiles(gson);
        for (AbstractSpell spell : SpellRegistry.REGISTRY.get()) {
            spell.resetRarityWeights();
        }
        dirty = true;
    }

    public void handleClientSync(SyncJsonConfigPacket packet) {
        buildConfigManager(toJson(packet.data));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        var player = event.getPlayer();

        if (INSTANCE.dirty) {
            INSTANCE.dirty = false;
            if (INSTANCE.datapackOverride != null) {
                INSTANCE.buildConfigManager(INSTANCE.datapackOverride);
                INSTANCE.datapackOverride = null;
            } else {
                INSTANCE.buildConfigManager(INSTANCE.toJson(getConfigFiles(resolveConfigDirectory(server))));
            }
        }
        if (INSTANCE.config != null) {
            if (player != null) {
                // individual player sync (such as logging in)
                PacketDistributor.sendToPlayer(player, new SyncJsonConfigPacket(INSTANCE.createNetworkData()));
            } else {
                // global sync (such as /reload command)
                PacketDistributor.sendToAllPlayers(new SyncJsonConfigPacket(INSTANCE.createNetworkData()));
            }
        } else {
            IronsSpellbooks.LOGGER.warn("Failed to sync config to players, instance is null");
        }
    }

    private static File resolveConfigDirectory(MinecraftServer server) {
        return FMLPaths.CONFIGDIR.get().resolve(SUBCONFIG_FOLDER).toFile();
    }

    private static byte[] readBytes(File file) {
        try (FileReader reader = new FileReader(file)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString().replaceAll("[ \n]", "").getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("Failed to read config file: {}", e);
            return new byte[]{};
        }
    }

    private static Map<ResourceLocation, byte[]> getConfigFiles(File directory) {
        HashMap<ResourceLocation, byte[]> files = new HashMap<>();
        long milis = System.currentTimeMillis();
        File[] namespacedDirectories = directory.listFiles();
        if (namespacedDirectories != null) {
            for (File namespacedDir : namespacedDirectories) {
                if (namespacedDir.isDirectory()) {
                    String namespace = namespacedDir.getName();
                    File[] entries = namespacedDir.listFiles((file, name) -> name.endsWith(".json"));
                    if (entries != null) {
                        for (File entry : entries) {
                            String spellName = entry.getName().split("\\.")[0];
                            ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(namespace, spellName);
                            if (SpellRegistry.REGISTRY.get().containsKey(spellId)) {
                                files.put(spellId, readBytes(entry));
                            } else {
                                IronsSpellbooks.LOGGER.warn("Unknown Spell for Configuration file \"{}/{}\", will be ignored!", namespace, spellName);
                            }
                        }
                    }
                } else if (namespacedDir.getName().endsWith(".json")) {
                    IronsSpellbooks.LOGGER.warn("Spell Configuration file \"{}\", outside of namespaced directory, will be ignored!", namespacedDir.getName());
                }
            }
        }
        if (!files.isEmpty()) {
            IronsSpellbooks.LOGGER.info("Read {} spell config files ({} ms)", files.size(), System.currentTimeMillis() - milis);
        }
        return files;
    }

    private Map<ResourceLocation, byte[]> createNetworkData() {
        Map<ResourceLocation, byte[]> data = new HashMap<>();
        for (var entry : config.entrySet()) {
            JsonObject json = entry.getValue().toJson(gson);
            if (!json.asMap().isEmpty()) {
                data.put(entry.getKey().getSpellResource(), json.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        return data;
    }


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
            MinecraftForge.EVENT_BUS.post(new RegisterConfigParametersEvent(ALL_TYPES::add));
        }
    }


    private Map<ResourceLocation, JsonElement> toJson(Map<ResourceLocation, byte[]> filestreams) {
        Map<ResourceLocation, JsonElement> configEntries = new HashMap<>();
        for (var entry : filestreams.entrySet()/*JsonElement elem : array*/) {
            var id = entry.getKey();
            var file = entry.getValue();
            JsonObject obj = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(file)), JsonObject.class);
            try {
                configEntries.put(id, obj);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("Failed to parse id of config entry: {}", e.getMessage());
            }
        }
        return configEntries;
    }

    private void buildConfigManager(Map<ResourceLocation, JsonElement> configEntries) {
        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
//        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        DynamicOps<JsonElement> registryops = JsonOps.INSTANCE;
        for (AbstractSpell spell : SpellRegistry.REGISTRY.get()) {
            // Build defaults
            SpellConfigHolder config = new SpellConfigHolder();
            DefaultConfig raw = spell.getDefaultConfig();
            config.setDefaultValue(IronConfigParameters.SCHOOL, SchoolRegistry.getSchool(raw.schoolResource));
            config.setDefaultValue(IronConfigParameters.MIN_RARITY, raw.minRarity);
            config.setDefaultValue(IronConfigParameters.MAX_LEVEL, raw.maxLevel);
            config.setDefaultValue(IronConfigParameters.ENABLED, raw.enabled);
            config.setDefaultValue(IronConfigParameters.COOLDOWN_IN_SECONDS, raw.cooldownInSeconds);
            config.setDefaultValue(IronConfigParameters.ALLOW_CRAFTING, raw.allowCrafting);
            // Handle user-specified Overrides
            ResourceLocation spellId = spell.getSpellResource();
            if (configEntries.containsKey(spellId)) {
                try {
                    JsonObject json = configEntries.get(spellId).getAsJsonObject();
                    for (SpellConfigParameter<?> paramType : ALL_TYPES) {
                        Optional<JsonElement> elem = resolveJsonElement(spellId, paramType, json);
                        if (elem.isPresent()) {
                            var decoded = paramType.datatype().decode(registryops, elem.get()).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
                            config.set((SpellConfigParameter) paramType, decoded);
                        }
                    }
                } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                    IronsSpellbooks.LOGGER.error("Parsing error loading spell config {}: {}", spellId, jsonparseexception);
                }
            }
            builder.put(spell, config);
        }
        config = builder.build();
        // Second pass for events. Allows for full context (can reference existing default and modified config values)
        for (AbstractSpell spell : SpellRegistry.REGISTRY.get()) {
            MinecraftForge.EVENT_BUS.post(new ModifyDefaultConfigValuesEvent(spell, config.get(spell)));
        }
    }

    private static File initiateDefaultFiles(Gson gson) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path spellConfigDir = configDir.resolve(SUBCONFIG_FOLDER);
        File folder = spellConfigDir.toFile();
        if (!folder.exists()) {
            folder.mkdir();
        }
        File spellbookDir = spellConfigDir.resolve("irons_spellbooks").toFile();
        if (!spellbookDir.exists()) {
            spellbookDir.mkdir();
            createExampleConfig(gson, spellbookDir.toPath().resolve("example.txt").toFile());
        }
        return spellbookDir;
    }

    private static void createExampleConfig(Gson gson, File file) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_comment1", "Config Files must be placed in a directory labeled with their mod id, and the file name must match the spell id!");
        jsonObject.addProperty("_comment2", "For global config: /config/irons_spellbooks_spell_config/<mod_id>/<spell_id>.json");
        jsonObject.addProperty("_comment3", "For datapacks: /data/<mod_id>/irons_spellbooks_spell_config/<spell_id>.json");
        for (SpellConfigParameter param : SpellConfigManager.ALL_TYPES) {
            var codec = param.datatype();
            DataResult<?> result = codec.encodeStart(JsonOps.INSTANCE, param.defaultValue());
            jsonObject.add(param.key().toString(), gson.toJsonTree(result.getOrThrow(false, IronsSpellbooks.LOGGER::error)));
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            IronsSpellbooks.LOGGER.error("Failed to write default config file {}: {}", file.getPath(), e.getMessage());
        }
    }

    private static Optional<JsonElement> resolveJsonElement(ResourceLocation spellId, SpellConfigParameter<?> dataType, JsonObject parent) {
        if (parent.has(dataType.key().toString())) {
            return Optional.of(parent.get(dataType.key().toString()));
        } else if (parent.has(dataType.key().getPath())) {
            if (!dataType.key().getNamespace().equals("irons_spellbooks")) {
                // Allow use of just path for irons_spellbooks namespaced entries. Give warning when other mods try to do it
                IronsSpellbooks.LOGGER.warn("Config for {} has ambiguous entry \"{}\", adapting to \"{}\"", spellId, dataType.key().getPath(), dataType.key());
            }
            return Optional.of(parent.get(dataType.key().getPath()));
        } else {
            return Optional.empty();
        }
    }


}
