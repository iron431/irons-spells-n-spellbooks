package io.redspace.ironsspellbooks.api.backwards_compat;

import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber
public class UpgradeTypeCache {
    public final static HashMap<ResourceKey<UpgradeOrbType>, Holder<UpgradeOrbType>> CACHE = new HashMap<>();

    public static void onClientLoad(RegistryAccess registryAccess, List<ResourceKey<UpgradeOrbType>> types) {
        CACHE.clear();
        for (ResourceKey<UpgradeOrbType> type : types) {
            CACHE.put(type, UpgradeOrbTypeRegistry.upgradeTypeRegistry(registryAccess).getHolderOrThrow(type));
        }
    }



    public static void doCache(RegistryAccess registryAccess) {
        CACHE.clear();
        Registry<UpgradeOrbType> registry = UpgradeOrbTypeRegistry.upgradeTypeRegistry(registryAccess);
        for (var entry : registry.entrySet()) {
            CACHE.put(entry.getKey(), registry.wrapAsHolder(entry.getValue()));
        }
    }

    @SubscribeEvent
    public static void onDatapackLoad(OnDatapackSyncEvent event) {
        var player = event.getPlayer();
        if (player == null || CACHE.isEmpty()) {
            // First time load. perform caching
            RegistryAccess access = event.getPlayerList().getServer().registryAccess();
            doCache(access);
            // sync to all current players
//            PacketDistributor.sendToAllPlayers(new SyncUpgradeOrbTypes(CACHE.keySet()));
        } else {
            // new player joined. sync cache to them
//            PacketDistributor.sendToPlayer(player, new SyncUpgradeOrbTypes(CACHE.keySet()));
        }
    }
}
