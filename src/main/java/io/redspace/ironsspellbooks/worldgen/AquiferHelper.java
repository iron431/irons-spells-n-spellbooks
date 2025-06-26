package io.redspace.ironsspellbooks.worldgen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Set;

public class AquiferHelper {
    static final Set<ResourceLocation> structuresToTrack;
    private static final Set<Structure> structuresInterruptingAquifers;
    private static boolean cached = false;

    static {
        structuresToTrack = new ObjectOpenHashSet<>();
        structuresInterruptingAquifers = new ObjectOpenHashSet<>();
        structuresToTrack.add(IronsSpellbooks.id("ice_spider_den"));
    }

    public static void registerTrackedStructure(ResourceLocation resourceLocation) {
        structuresToTrack.add(resourceLocation);
    }

    public static Set<Structure> getOrCacheStructures(StructureManager registryAccess) {
        if (!cached) {
            synchronized (structuresInterruptingAquifers) {
                var registry = registryAccess.registryAccess().registryOrThrow(Registries.STRUCTURE);
                for (ResourceLocation r : structuresToTrack) {
                    var str = registry.get(r);
                    if (str != null) {
                        structuresInterruptingAquifers.add(str);
                    }
                }
                cached = true;
            }
        }
        return structuresInterruptingAquifers;
    }
}
