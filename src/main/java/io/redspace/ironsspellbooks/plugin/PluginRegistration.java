package io.redspace.ironsspellbooks.plugin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.IIronsSpellbooksPlugin;

import java.util.ArrayList;

public class PluginRegistration {
    public static void LoadPlugins() {
        var validPlugins = new ArrayList<IIronsSpellbooksPlugin>();
        PluginDiscovery.getPlugins().forEach(plugin -> {
            try {
                IronsSpellbooks.LOGGER.debug("ISS Plugin: Loading: {}", plugin);
                plugin.registerSpells(null);//TODO: this needs real code
                validPlugins.add(plugin);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("ISS Plugin Error: Skipping plugin {} {}", plugin, e);
            }
        });

        validPlugins.forEach(validPlugin -> {
            try {
                IronsSpellbooks.LOGGER.debug("ISS Plugin: Finalizing: {}", validPlugin);
                validPlugin.onAllSpellsRegistered();
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("ISS Plugin Error: {} {}", validPlugin, e);
            }
        });
    }
}
