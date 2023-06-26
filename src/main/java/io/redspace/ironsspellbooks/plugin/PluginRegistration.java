package io.redspace.ironsspellbooks.plugin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
public class PluginRegistration {
    public static void LoadPlugins() {
        PluginDiscovery.getPlugins().forEach(plugin -> {
            try {
                IronsSpellbooks.LOGGER.debug("ISS Plugin: Loading: {}", plugin);
                IronsSpellbooks.LOGGER.debug("ISS Plugin: {}", plugin.getPluginId());
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("ISS Plugin Error: Skipping plugin");
            }
        });
    }
}
