package io.redspace.ironsspellbooks.api.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.network.SyncJsonConfigPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@EventBusSubscriber
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
            return parameterType.defaultValue().get();
        }
        return INSTANCE.config.get(spell).get(parameterType);
    }

    /**
     * @return The spell's default preset configuration value for this parameter, or the parameter's default if none is defined.
     */
    public static <T> T getSpellDefaultConfigValue(AbstractSpell spell, SpellConfigParameter<T> parameterType) {
        if (!INSTANCE.config.containsKey(spell)) {
            return parameterType.defaultValue().get();
        }
        return INSTANCE.config.get(spell).getDefaultValue(parameterType).orElse(parameterType.defaultValue().get());
    }

    /*
     * Implementation
     */
    private final Gson gson;
    @Nullable
    private Map<ResourceLocation, JsonElement> datapackOverride = null;
    /**
     * A non-sparse map containing all spells and their fully defined config holders
     */
    private ImmutableMap<AbstractSpell, SpellConfigHolder> config = ImmutableMap.of();

    public SpellConfigManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_spellbooks_spell_config");
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (!object.isEmpty()) {
            Map<ResourceLocation, JsonElement> data = new HashMap<>(object.size());
            for (var entry : object.entrySet()) {
                // Map file ids to spell ids by omitting intermediary directories
                ResourceLocation key = entry.getKey();
                if (key.getPath().contains("/")) {
                    var path = key.getPath().split("/");
                    key = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), path[path.length - 1]);
                }
                data.put(key, entry.getValue());
            }
            datapackOverride = data;
        }
        handleServerConfigUpdate();
    }


    public void handleServerConfigUpdate() {
        registerConfigParameterTypes();
        initiateDefaultFiles(gson);
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            spell.resetRarityWeights();
        }
        dirty = true;
    }

    public void handleClientSync(SyncJsonConfigPacket packet) {
        IronsSpellbooks.LOGGER.info("Handling spell config sync {} files", packet.data.size());
        handleServerConfigUpdate();
        buildConfigManager(toJson(packet.data));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        var player = event.getPlayer();
        boolean noErrors = true;
        if (INSTANCE.dirty) {
            INSTANCE.dirty = false;
            if (INSTANCE.datapackOverride != null) {
                noErrors = INSTANCE.buildConfigManager(INSTANCE.datapackOverride);
                INSTANCE.datapackOverride = null;
            } else {
                noErrors = INSTANCE.buildConfigManager(INSTANCE.toJson(getConfigFiles(resolveConfigDirectory(server))));
            }
        }
        if (INSTANCE.config != null) {
            if (player != null) {
                // individual player sync (such as logging in)
                PacketDistributor.sendToPlayer(player, new SyncJsonConfigPacket(INSTANCE.createNetworkData()));
                if (!noErrors) {
                    player.displayClientMessage(Component.translatable("commands.irons_spellbooks.config_load_errors").withStyle(ChatFormatting.RED), false);
                }
            } else {
                // global sync (such as /reload command)
                PacketDistributor.sendToAllPlayers(new SyncJsonConfigPacket(INSTANCE.createNetworkData()));
                if (!noErrors) {
                    for (Player p : server.getPlayerList().getPlayers()) {
                        p.displayClientMessage(Component.translatable("commands.irons_spellbooks.config_load_errors").withStyle(ChatFormatting.RED), false);
                    }
                }
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

    private static List<File> expandAllJsonFiles(File directory) {
        List<File> files = new ArrayList<>();
        File[] sub = directory.listFiles();
        if (sub == null) {
            return List.of();
        }
        for (File file : sub) {
            if (file.getName().endsWith(".json")) {
                files.add(file);
            } else if (file.isDirectory()) {
                files.addAll(expandAllJsonFiles(file));
            }
        }
        return files;
    }

    private static Map<ResourceLocation, byte[]> getConfigFiles(File directory) {
        HashMap<ResourceLocation, byte[]> files = new HashMap<>();
        long milis = System.currentTimeMillis();
        File[] namespacedDirectories = directory.listFiles();
        if (namespacedDirectories != null) {
            for (File namespacedDir : namespacedDirectories) {
                if (namespacedDir.isDirectory()) {
                    String namespace = namespacedDir.getName();
                    // use the topmost directory as the namespace. traverse and ignore all other subdirectories. use endpoint filename as spellid.
                    List<File> entries = expandAllJsonFiles(namespacedDir);
                    for (File entry : entries) {
                        String spellName = entry.getName().split("\\.")[0];
                        ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(namespace, spellName);
                        if (SpellRegistry.REGISTRY.containsKey(spellId)) {
                            if (files.containsKey(spellId)) {
                                IronsSpellbooks.LOGGER.warn("Duplicate spell config for spell {}! Overriding config.", spellId);
                            }
                            files.put(spellId, readBytes(entry));
                        } else {
                            IronsSpellbooks.LOGGER.warn("Unknown Spell for Configuration file \"{}:{}\", will be ignored!", namespace, spellName);
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
            if (!json.isEmpty()) {
                data.put(entry.getKey().getSpellResource(), json.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        return data;
    }


    public static final Set<SpellConfigParameter<?>> ALL_TYPES = new HashSet<>();
    private static boolean registered = false;
    private boolean dirty = true;

    private static void registerConfigParameterTypes() {
        if (!registered) {
            registered = true;
            ALL_TYPES.add(SpellConfigParameter.SCHOOL);
            ALL_TYPES.add(SpellConfigParameter.MIN_RARITY);
            ALL_TYPES.add(SpellConfigParameter.MAX_LEVEL);
            ALL_TYPES.add(SpellConfigParameter.ENABLED);
            ALL_TYPES.add(SpellConfigParameter.COOLDOWN_IN_SECONDS);
            ALL_TYPES.add(SpellConfigParameter.ALLOW_CRAFTING);
            ALL_TYPES.add(SpellConfigParameter.MANA_MULTIPLIER);
            ALL_TYPES.add(SpellConfigParameter.POWER_MULTIPLIER);
            NeoForge.EVENT_BUS.post(new RegisterConfigParametersEvent(ALL_TYPES::add));
        }
    }


    private Map<ResourceLocation, JsonElement> toJson(Map<ResourceLocation, byte[]> filestreams) {
        Map<ResourceLocation, JsonElement> configEntries = new HashMap<>();
        for (var entry : filestreams.entrySet()/*JsonElement elem : array*/) {
            var id = entry.getKey();
            var file = entry.getValue();
            try {
                JsonObject obj = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(file)), JsonObject.class);
                configEntries.put(id, obj);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("Failed to parse config file \"{}\": {}", id, e.getMessage());
            }
        }
        return configEntries;
    }

    /**
     * @return <code>true</code> if all entries loaded successfully. <code>false</code> if any entries had errors loading
     */
    private boolean buildConfigManager(Map<ResourceLocation, JsonElement> configEntries) {
        boolean hasErrors = false;
        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            // Build defaults
            SpellConfigHolder config = new SpellConfigHolder();
            DefaultConfig raw = spell.getDefaultConfig();
            config.setDefaultValue(SpellConfigParameter.SCHOOL, SchoolRegistry.getSchool(raw.schoolResource));
            config.setDefaultValue(SpellConfigParameter.MIN_RARITY, raw.minRarity);
            config.setDefaultValue(SpellConfigParameter.MAX_LEVEL, raw.maxLevel);
            config.setDefaultValue(SpellConfigParameter.ENABLED, raw.enabled);
            config.setDefaultValue(SpellConfigParameter.COOLDOWN_IN_SECONDS, raw.cooldownInSeconds);
            config.setDefaultValue(SpellConfigParameter.ALLOW_CRAFTING, raw.allowCrafting);
            // Handle user-specified Overrides
            ResourceLocation spellId = spell.getSpellResource();
            if (configEntries.containsKey(spellId)) {
                try {
                    JsonObject json = configEntries.get(spellId).getAsJsonObject();
                    for (SpellConfigParameter<?> paramType : ALL_TYPES) {
                        Optional<JsonElement> elem = resolveJsonElement(spellId, paramType, json);
                        if (elem.isPresent()) {
                            try {
                                var decoded = paramType.datatype().decode(registryops, elem.get()).getOrThrow().getFirst();
                                config.set((SpellConfigParameter) paramType, decoded);
                            } catch (Exception e) {
                                IronsSpellbooks.LOGGER.error("Parsing error loading spell config \"{}\" value for \"{}\": {}", spellId, paramType.key(), e.getLocalizedMessage());
                                hasErrors = true;
                            }
                        }
                    }
                } catch (IllegalStateException e) {
                    IronsSpellbooks.LOGGER.error("Parsing error loading spell config {}: {}", spellId, e);
                    hasErrors = true;
                }
            }
            builder.put(spell, config);
        }
        config = builder.build();
        // Second pass for events. Allows for full context (can reference existing default and modified config values)
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            NeoForge.EVENT_BUS.post(new ModifyDefaultConfigValuesEvent(spell, config.get(spell)));
        }
        return !hasErrors;
    }

    public static File getSpellConfigDir() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path spellConfigDir = configDir.resolve(SUBCONFIG_FOLDER);
        File folder = spellConfigDir.toFile();
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

    private static File initiateDefaultFiles(Gson gson) {
        File spellConfigDir = getSpellConfigDir();
        File spellbookDir = spellConfigDir.toPath().resolve("irons_spellbooks").toFile();
        if (!spellbookDir.exists()) {
            spellbookDir.mkdir();
            createExampleConfig(gson, spellbookDir.toPath().resolve("example.txt").toFile());
        }
        return spellbookDir;
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

    public static <T> Pair<Boolean, File> createExampleConfig(Gson gson, File file) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("_comment1", "Config Files must be placed in a directory labeled with their mod id, and the file name must match the spell id!");
        jsonObject.addProperty("_comment2", "For global config: /config/irons_spellbooks_spell_config/<mod_id>/<spell_id>.json");
        jsonObject.addProperty("_comment3", "For datapacks: /data/<mod_id>/irons_spellbooks_spell_config/<spell_id>.json");
        for (SpellConfigParameter<?> _param : SpellConfigManager.ALL_TYPES) {
            SpellConfigParameter<T> param = (SpellConfigParameter<T>) _param;
            Codec<T> codec = param.datatype();
            DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, param.defaultValue().get());
            jsonObject.add(param.key().toString(), gson.toJsonTree(result.getOrThrow()));
//            extracted(gson, param, jsonObject);
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(jsonObject, writer);
            return Pair.of(true, file);
        } catch (IOException e) {
            IronsSpellbooks.LOGGER.error("Failed to write default config file {}: {}", file.getPath(), e.getMessage());
            return Pair.of(false, null);
        }
    }

    public static <T> Pair<Boolean, File> generateSpellConfigFile(Gson gson, AbstractSpell spell, boolean full, boolean override) {
        ResourceLocation resourceLocation = spell.getSpellResource();
        try {
            File spellConfigDir = getSpellConfigDir();
            File modDir = spellConfigDir.toPath().resolve(resourceLocation.getNamespace()).toFile();
            if (!modDir.exists()) {
                modDir.mkdir();
            }
            File spellConfig = modDir.toPath().resolve(resourceLocation.getPath() + ".json").toFile();
            if (spellConfig.exists() && !override) {
                return Pair.of(false, spellConfig);
            }
            JsonObject json = new JsonObject();
            if (full) {
                for (SpellConfigParameter<?> _param : SpellConfigManager.ALL_TYPES) {
                    // fill file with spell's default values
                    SpellConfigParameter<T> param = (SpellConfigParameter<T>) _param;
                    Codec<T> codec = param.datatype();
                    DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, SpellConfigManager.getSpellDefaultConfigValue(spell, param));
                    json.add(param.key().toString(), gson.toJsonTree(result.getOrThrow()));
                }
            }
            try (FileWriter writer = new FileWriter(spellConfig)) {
                gson.toJson(json, writer);
            }
            return Pair.of(true, spellConfig);
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("Could not generate config file: {}", e.getMessage());
            return Pair.of(false, null);
        }
    }
}
