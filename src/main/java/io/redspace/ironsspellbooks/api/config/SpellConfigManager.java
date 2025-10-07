package io.redspace.ironsspellbooks.api.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

//@EventBusSubscriber
public class SpellConfigManager {
    public static final String JSON_HEADER = "config";
    public static final String ID_FIELD = "id";
    private static final Set<SpellConfigParameter<?>> ALL_TYPES = new HashSet<>();
    private static boolean registered = false;

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
//    public SpellConfigManager() {
//        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "irons_spellbooks/spell_config");
//    }
//
//    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
//        scanDirectory(resourceManager, this.directory, this.gson, map);
//        return map;
//    }
//
//    public static Map<ResourceLocation, JsonElement> scanDirectory(String directory, Gson gson) {
//        Map<ResourceLocation, JsonElement> output = new HashMap<>();
//
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*.json")) {
//            for (Path filePath : stream) {
//                try (FileReader reader = new FileReader(filePath.toFile())) {
//                    JsonElement jsonElement = JsonParser.parseReader(reader);
//                    output.put(filePath, jsonElement);
//                } catch (IOException e) {
//                    System.err.println("Failed to read " + filePath + ": " + e.getMessage());
//                }
//            }
//        }
//
//        return output;
//    }

    private static ImmutableMap<AbstractSpell, SpellConfigHolder> INSTANCE;

    public static void handleServerConfigUpdate() {
        registerConfigParameterTypes();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = initiateOrGetConfig(gson);
        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
//        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
        var registryops = JsonOps.INSTANCE;
        Map<ResourceLocation, JsonObject> configEntries = new HashMap<>();
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
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
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
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
        String configFolderName = "irons_spellbooks_spells";
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path spellConfigDir = configDir.resolve(configFolderName);
        File folder = spellConfigDir.toFile();
        if (!folder.exists()) {
            folder.mkdir();
//                File exampleConfig = spellConfigDir.resolve("example.json").toFile();
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                Map<String, Object> defaultConfig = SpellConfigManager.ALL_TYPES.stream().collect(Collectors.toMap(param -> param.key().toString(),
//                        param -> param.defaultValue()));
//                try (FileWriter writer = new FileWriter(exampleConfig)) {
//                    gson.toJson(defaultConfig, writer);
//                } catch (IOException e) {
//                    IronsSpellbooks.LOGGER.error("Failed to write example config {}, {}", exampleConfig.getPath(), e.getMessage());
//                }
        }
        File config = spellConfigDir.resolve("config.json").toFile();
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

//    @Override
//    protected void apply(@NotNull Map<ResourceLocation, JsonElement> data, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
//        registerConfigParameterTypes();
//        ImmutableMap.Builder<AbstractSpell, SpellConfigHolder> builder = ImmutableMap.builder();
//        RegistryOps<JsonElement> registryops = this.makeConditionalOps();
//        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
//            // Build defaults
//            SpellConfigHolder config = new SpellConfigHolder();
//            DefaultConfig raw = spell.getDefaultConfig();
//            config.set(IronConfigParameters.SCHOOL, SchoolRegistry.getSchool(raw.schoolResource));
//            config.set(IronConfigParameters.MIN_RARITY, raw.minRarity);
//            config.set(IronConfigParameters.MAX_LEVEL, raw.maxLevel);
//            config.set(IronConfigParameters.ENABLED, raw.enabled);
//            config.set(IronConfigParameters.COOLDOWN_IN_SECONDS, raw.cooldownInSeconds);
//            config.set(IronConfigParameters.ALLOW_CRAFTING, raw.allowCrafting);
//            // Data Driven Overrides
//            ResourceLocation spellId = spell.getSpellResource();
//            if (data.containsKey(spellId)) {
//                try {
//                    JsonObject json = data.get(spellId).getAsJsonObject();
//                    for (SpellConfigParameter<?> paramType : ALL_TYPES) {
//                        Optional<JsonElement> elem = resolveJsonElement(spellId, paramType, json);
//                        if (elem.isPresent()) {
//                            var decoded = paramType.datatype().decode(registryops, elem.get()).getOrThrow().getFirst();
//                            config.set((SpellConfigParameter) paramType, decoded);
//                        }
//                    }
//                } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
//                    IronsSpellbooks.LOGGER.error("Parsing error loading spell config {}: {}", spellId, jsonparseexception);
//                }
//            }
//            builder.put(spell, config);
//        }
//        INSTANCE = builder.build();
//        // Second pass for events. Allows for data-completion (can reference existing config values), and higher context (avoid overriding user-input)
//        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
//            NeoForge.EVENT_BUS.post(new ModifyDefaultConfigValuesEvent(spell, INSTANCE.get(spell)));
//        }
//    }

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
