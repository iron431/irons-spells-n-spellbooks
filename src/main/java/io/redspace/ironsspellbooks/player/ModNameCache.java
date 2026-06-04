package io.redspace.ironsspellbooks.player;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.HashMap;
import java.util.Optional;

public class ModNameCache {
    public static final HashMap<String, String> MOD_NAME_LOOKUP_CACHE = new HashMap<>();

    public static Optional<String> getModName(String modid) {
        if (!MOD_NAME_LOOKUP_CACHE.containsKey(modid)) {
            // force null entries, preventing repeated cache misses, which computeIfAbsent does not provide
            MOD_NAME_LOOKUP_CACHE.put(modid, ModList.get().getModContainerById(modid)
                    .map(ModContainer::getModInfo).map(IModInfo::getDisplayName)
                    .orElse(null));
        }
        return Optional.ofNullable(MOD_NAME_LOOKUP_CACHE.get(modid));
    }
}
