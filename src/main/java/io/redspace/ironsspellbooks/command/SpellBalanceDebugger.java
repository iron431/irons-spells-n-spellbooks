package io.redspace.ironsspellbooks.command;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SpellBalanceDebugger {
    record Info(AbstractSpell spell, Map<String, String> values) {
    }

    Map<String, Integer> trackedProperties;
    List<Info> spellInfo;

    public SpellBalanceDebugger() {
        this.spellInfo = new ArrayList<>();
        this.trackedProperties = new HashMap<>();
    }

    private void trackOccurance(String property) {
        int occurrences = trackedProperties.getOrDefault(property, 0);
        occurrences++;
        trackedProperties.put(property, occurrences);
    }

    private Map<String, String> getValuesFromSpell(AbstractSpell spell) {
        var info = spell.getUniqueInfo(spell.getMaxLevel(), null);
        var map = new HashMap<String, String>();
        map.put("Name", Component.translatable(spell.getComponentId()).getString());
        map.put("Mana Cost", String.valueOf(spell.getManaCost(spell.getMaxLevel())));
        map.put("Cooldown", Utils.timeFromTicks(spell.getSpellCooldown(), 0));
        map.put("Cast Type", spell.getCastType().toString());
        Set<String> tracked = new HashSet<>();
        for (Component component : info) {
            if (component.getContents() instanceof TranslatableContents translatableContents) {
//                String property = Component.translatable(translatableContents.getKey()).getString().replace("%s ",""); // get translation key, generate without args, then remove empty args from resulting string
                String fullLine = component.getString();
                String translate = Component.translatable(translatableContents.getKey()).getString();
                int valueBeginIndex = translate.indexOf("%s");
                if (valueBeginIndex < 0) {
                    IronsSpellbooks.LOGGER.info("skipping property {}", translate);
                    continue;
                }
                int valueEndIndex = fullLine.indexOf(" ", valueBeginIndex);
                if (valueEndIndex < 0) {
                    valueEndIndex = fullLine.length();
                }
                String value = fullLine.substring(valueBeginIndex, valueEndIndex);
                String property = translate.replace("%s", "");
                map.put(property, value);
                if (!tracked.contains(property)) {
                    trackOccurance(property);
                    tracked.add(property); // prevent same spell adding duplicates
                }
            }
        }
        return map;
    }

    private List<String> generateCSV() {
        List<String> propertiesToExport = new ArrayList<>();
        for (var entry : trackedProperties.entrySet()) {
            if (entry.getValue() >= 3) {
                propertiesToExport.add(entry.getKey());
            }
        }
        propertiesToExport.addFirst("Cast Type");
        propertiesToExport.addFirst("Cooldown");
        propertiesToExport.addFirst("Mana Cost");
        propertiesToExport.addFirst("Name");
        String header = String.join(",", propertiesToExport);
        List<String> contents = new ArrayList<>();
        contents.add(header);
        for (Info spellInfo : this.spellInfo) {
            StringBuilder line = new StringBuilder();
            for (var property : propertiesToExport) {
                line.append(spellInfo.values.getOrDefault(property, "")).append(",");
            }
            contents.add(line.toString());
        }
        return contents;
    }

    private void exportCSV(String fileName, List<String> contents) throws IOException {
        if (!fileName.endsWith(".csv")) {
            fileName += ".csv";
        }
        Path dirPath = Path.of("screenshots/irons_jewelry");
        Path filePath = dirPath.resolve(fileName);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);  // creates all nonexistent parent directories
        }
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);  // creates the actual file
        }
        Files.write(filePath, contents);
    }

    public void run() {
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            spellInfo.add(new Info(spell, getValuesFromSpell(spell)));
        }
        try {
            exportCSV("spell_info.csv", generateCSV());
        } catch (IOException e) {
            IronsSpellbooks.LOGGER.error("Failed to generate csv: {}", e.getMessage());
        }
    }
}