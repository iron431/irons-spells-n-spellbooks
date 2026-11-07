package io.redspace.ironsspellbooks.setup;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.skillcasting.client.keybinds.KeyMappings;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo(legacy-data): between MC versions; 3.x keybinds were saved as key.irons_spellbooks.*. Delete at the 26.x port
@EventBusSubscriber(modid = IronsSpellbooks.MODID, value = Dist.CLIENT)
public final class LegacyKeyMappingMigration {
    private static final String LEGACY_PREFIX = "key_key.irons_spellbooks.";

    private LegacyKeyMappingMigration() {
    }

    // NeoForge reads mod keybinds from options.txt after this event, so a key set here stays unless the new name is already saved.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void migrate(RegisterKeyMappingsEvent event) {
        Path optionsFile = FMLPaths.GAMEDIR.get().resolve("options.txt");
        if (!Files.exists(optionsFile)) {
            return;
        }
        Map<String, String> saved = new HashMap<>();
        try {
            for (String line : Files.readAllLines(optionsFile)) {
                int separator = line.indexOf(':');
                if (separator > 0) {
                    saved.put(line.substring(0, separator), line.substring(separator + 1));
                }
            }
        } catch (IOException e) {
            IronsSpellbooks.LOGGER.warn("Could not read options.txt to migrate 3.x keybinds", e);
            return;
        }
        legacyNames().forEach((legacyName, mapping) -> {
            String legacyValue = saved.get(LEGACY_PREFIX + legacyName);
            if (legacyValue != null && !saved.containsKey("key_" + mapping.getName())) {
                apply(mapping, legacyValue);
            }
        });
    }

    private static void apply(KeyMapping mapping, String savedValue) {
        String[] parts = savedValue.split(":");
        KeyModifier modifier = KeyModifier.NONE;
        if (parts.length > 1) {
            modifier = KeyModifier.valueFromString(parts[1]);
        }
        mapping.setKeyModifierAndCode(modifier, InputConstants.getKey(parts[0]));
    }

    private static Map<String, KeyMapping> legacyNames() {
        Map<String, KeyMapping> names = new HashMap<>();
        names.put("spell_wheel", KeyMappings.SKILL_WHEEL_KEYMAP);
        names.put("spell_wheel_toggle", KeyMappings.SKILL_WHEEL_TOGGLE_KEYMAP);
        names.put("spellbook_cast", KeyMappings.CAST_SELECTED_SKILL_KEYMAP);
        names.put("spell_bar_modifier", KeyMappings.SKILLBAR_SCROLL_MODIFIER_KEYMAP);
        List<? extends KeyMapping> quickCasts = KeyMappings.QUICK_CAST_MAPPINGS;
        for (int i = 0; i < quickCasts.size(); i++) {
            names.put("spell_quick_cast_" + (i + 1), quickCasts.get(i));
        }
        return names;
    }
}
