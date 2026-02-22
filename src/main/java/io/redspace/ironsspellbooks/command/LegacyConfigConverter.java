package io.redspace.ironsspellbooks.command;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
        Enabled = true
		School = "irons_spellbooks:nature"
		MaxLevel = 10
		#Allowed Values: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
		MinRarity = "COMMON"
		ManaCostMultiplier = 1.0
		SpellPowerMultiplier = 1.0
		CooldownInSeconds = 15.0
		AllowCrafting = true
 */
public class LegacyConfigConverter {
    public static int runCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String path;
        try {
            path = run(commandSourceStackCommandContext);
        } catch (RuntimeException e) {
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal("Failed to execute conversion: " + e.getMessage() + ". See log for full details."));
            IronsSpellbooks.LOGGER.error("[Config Converter] Failed to execute: {}", e.toString());
            return 0;
        }
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Saved to " + path), false);
        return 1;
    }

    private static @Nullable File resolveLastBakFile(File configDir) {
        File[] options = configDir.listFiles(file -> file.getName().startsWith("irons_spellbooks-server") && file.getName().endsWith(".bak"));
        if (options == null || options.length == 0) {
            return null;
        }
        Arrays.sort(options);
        return options[0];
    }

    private static String run(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws RuntimeException {
        var commandSourceStack = commandSourceStackCommandContext.getSource();
        var server = commandSourceStack.getServer();
        File spellbooksConfig;
        // First, check server config directory for config files
        File configDir = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").toFile();
        if (!configDir.exists()) {
            // if entire server config directory absent, skip to global config
            configDir = FMLPaths.CONFIGDIR.get().toFile();
            if (!configDir.exists()) {
                // if neither found, throw
                throw new RuntimeException("Failed to find config directory");
            }
            spellbooksConfig = resolveLastBakFile(configDir);
        } else {
            // check server config for bak files
            spellbooksConfig = resolveLastBakFile(configDir);
            if (spellbooksConfig == null || !spellbooksConfig.exists()) {
                // if nothing present, check global config
                configDir = FMLPaths.CONFIGDIR.get().toFile();
                spellbooksConfig = resolveLastBakFile(configDir);
            }
        }
        if (spellbooksConfig == null || !spellbooksConfig.exists()) {
            throw new RuntimeException("No existing config backup to convert (backups are not automatically generated on 1.20.1!)");
        }
        TomlParser parser = new TomlParser();
        Config toml = parser.parse(spellbooksConfig, FileNotFoundAction.THROW_ERROR);
        Config spellToml = toml.get("Spells");
        Map<String, SpellConfigParameter<?>> conversionMap = Map.of(
                "Enabled", SpellConfigParameter.ENABLED,
                "School", SpellConfigParameter.SCHOOL,
                "MaxLevel", SpellConfigParameter.MAX_LEVEL,
                "MinRarity", SpellConfigParameter.MIN_RARITY,
                "ManaCostMultiplier", SpellConfigParameter.MANA_MULTIPLIER,
                "SpellPowerMultiplier", SpellConfigParameter.POWER_MULTIPLIER,
                "CooldownInSeconds", SpellConfigParameter.COOLDOWN_IN_SECONDS,
                "AllowCrafting", SpellConfigParameter.ALLOW_CRAFTING
        );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        List<Map<String, Object>> configOutput = new ArrayList<>();
        Map<ResourceLocation, Map<String, Object>> configOutput = new HashMap<>();
        for (var entry : spellToml.entrySet()) {
            ResourceLocation spellId = ResourceLocation.parse(entry.getKey());
            if (entry.isNull() || !(entry.getRawValue() instanceof Config)) {
                continue;
            }
            Config config = entry.getValue();
            Map<String, Object> jsonEntry = new HashMap<>();
//            jsonEntry.put(SpellConfigManager.ID_FIELD, spellId.toString());
            if (SpellRegistry.getSpell(spellId) == SpellRegistry.none()) {
                IronsSpellbooks.LOGGER.info("[Config Converter] Skipping spell {}, not a valid spell", spellId);
                continue;
            }
            for (var conversion : conversionMap.entrySet()) {
                Object configValue = config.get(conversion.getKey());
                var param = conversion.getValue();
                if (configValue == null) {
                    continue;
                }
                if (configValue instanceof String string) {
                    configValue = string.toLowerCase(Locale.ROOT);
                }
                if (checkIsDefaultValue(spellId, configValue, param)) {
                    continue;
                }
                jsonEntry.put(param.key().toString(), configValue);
            }
            if (jsonEntry.size() > 0) {
                configOutput.put(spellId, jsonEntry);
            } else {
                IronsSpellbooks.LOGGER.info("[Config Converter] Skipping config entry {}, all values are default", spellId);
            }

        }
        File outdir = configDir.toPath().resolve(SpellConfigManager.SUBCONFIG_FOLDER).toFile();
        for (var configEntry : configOutput.entrySet()) {
            File modDir = outdir.toPath().resolve(configEntry.getKey().getNamespace()).toFile();
            if (!modDir.exists()) {
                modDir.mkdir();
            }
            File fileout = modDir.toPath().resolve(configEntry.getKey().getPath() + ".json").toFile();
            try (FileWriter writer = new FileWriter(fileout)) {
                gson.toJson(configEntry.getValue(), writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        var path = outdir.toPath().toString();
        IronsSpellbooks.LOGGER.info("[Config Converter] Saved {} entries to {}", configOutput.size(), path);
        return path;
    }

    private static boolean checkIsDefaultValue(ResourceLocation spellId, Object value, SpellConfigParameter<?> param) {
        Object toCompare = value;
        if (param.equals(SpellConfigParameter.SCHOOL)) {
            try {
                toCompare = SchoolRegistry.getSchool(ResourceLocation.parse((String) toCompare));
            } catch (Exception e) {
                throw new RuntimeException("Failed to read school entry for spell " + spellId.toString());
            }
        } else if (param.equals(SpellConfigParameter.MIN_RARITY)) {
            try {
                toCompare = SpellRarity.valueOf(((String) toCompare).toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                throw new RuntimeException("Failed to read rarity entry for spell " + spellId.toString());
            }
        }
        return toCompare.equals(SpellConfigManager.getSpellDefaultConfigValue(SpellRegistry.getSpell(spellId), param));
    }
}
