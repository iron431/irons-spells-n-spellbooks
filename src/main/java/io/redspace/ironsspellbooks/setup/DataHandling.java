package io.redspace.ironsspellbooks.setup;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid= IronsSpellbooks.MODID, bus=Bus.FORGE)
public class DataHandling {

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        var reloadableServerResources = event.getServerResources();
        //LevelStorageSource.LevelStorageAccess();
    }

    @SubscribeEvent
    public static void onChunkDataEvent(ChunkDataEvent event){
        event.getData();
    }
}