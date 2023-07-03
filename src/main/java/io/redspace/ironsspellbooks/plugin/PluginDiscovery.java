package io.redspace.ironsspellbooks.plugin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.IIronsSpellbooksPlugin;
import io.redspace.ironsspellbooks.api.IronsSpellbooksPlugin;
import net.minecraftforge.fml.ModList;
import org.objectweb.asm.Type;

import java.util.*;

public final class PluginDiscovery {
    public static List<IIronsSpellbooksPlugin> getPlugins() {
        var allScanData = ModList.get().getAllScanData();
        Set<String> pluginClassNames = new HashSet<>();

        allScanData.forEach(scanData -> {
            scanData.getAnnotations().forEach(annotationData -> {
                //IronsSpellbooks.LOGGER.debug("ISS Plugin 1 {}", annotationData.annotationType());

                if (Objects.equals(annotationData.annotationType(), Type.getType(IronsSpellbooksPlugin.class))) {
                    pluginClassNames.add(annotationData.memberName());
                }
            });
        });

        List<IIronsSpellbooksPlugin> plugins = new ArrayList<>();
        pluginClassNames.forEach(pluginClassName -> {
            //IronsSpellbooks.LOGGER.debug("ISS Plugin 2 {}", pluginClassName);
            try {
                Class<?> pluginClass = Class.forName(pluginClassName);
                var pluginClassSubclass = pluginClass.asSubclass(IIronsSpellbooksPlugin.class);
                var constructor = pluginClassSubclass.getDeclaredConstructor();
                var instance = constructor.newInstance();
                plugins.add(instance);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("PluginDiscovery:  {}, {}", pluginClassName, e);
            }
        });

        return plugins;
    }
}