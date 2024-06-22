package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;


@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Bus.FORGE)
public class DataHandling {

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        var reloadableServerResources = event.getServerResources();
        //LevelStorageSource.LevelStorageAccess();
    }

    @SubscribeEvent
    public static void onChunkDataEvent(ChunkDataEvent event) {
        event.getData();
    }
}