package io.redspace.ironsspellbooks.worldgen;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Set;

@EventBusSubscriber
public class AquiferHelper {
    static final Set<ResourceLocation> structuresToTrack;
    private static final Set<Structure> structuresInterruptingAquifers;
    private static volatile boolean cached = false;

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

    @SubscribeEvent
    public static void clearCache(ServerStoppedEvent event) {
        // each server builds its own Structure instances
        synchronized (structuresInterruptingAquifers) {
            structuresInterruptingAquifers.clear();
            cached = false;
        }
    }
}
