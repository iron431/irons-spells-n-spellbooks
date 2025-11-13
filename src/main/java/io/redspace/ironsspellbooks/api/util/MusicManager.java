package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

@EventBusSubscriber
public class MusicManager {
    private static final Map<ResourceKey<Level>, MusicManager> MUSIC_MANAGERS = new HashMap<>();
    private final Stack<Pair<UUID, IMusicHandler>> musicHandlers = new Stack<>();
    private boolean resumeNext;

    public static void createEvent(Entity entity, IMusicHandler event) {
        createEvent(entity.level.dimension(), entity.getUUID(), event);
    }

    public static void createEvent(ResourceKey<Level> dimension, UUID id, IMusicHandler event) {
        if (!ClientConfigs.ENABLE_BOSS_MUSIC.get()) {
            return;
        }
        var manager = getManagerFor(dimension);
        if (!manager.musicHandlers.isEmpty()) {
            manager.musicHandlers.peek().right().stop();
        }
        event.init();
        manager.musicHandlers.push(new ObjectObjectImmutablePair<>(id, event));
    }

    public static void stopEvent(UUID uuid) {
        // while we only create events per-dimension, if something in any dimension calls for a specific uuid to be cancelled, we cancel it
        for (MusicManager manager : MUSIC_MANAGERS.values()) {
            for (var itr = manager.musicHandlers.iterator(); itr.hasNext(); ) {
                var entry = itr.next();
                if (entry.left().equals(uuid)) {
                    entry.right().stop();
                    if (!manager.musicHandlers.isEmpty()) {
                        manager.resumeNext = true;
                    }
                    itr.remove();
                    break;
                }
            }
        }
    }

    private static MusicManager getManagerFor(ResourceKey<Level> dimension) {
        return MUSIC_MANAGERS.computeIfAbsent(dimension, (dim) -> new MusicManager());
    }

    public static void clear() {
        for (MusicManager manager : MUSIC_MANAGERS.values()) {
            for (var itr = manager.musicHandlers.iterator(); itr.hasNext(); ) {
                var entry = itr.next();
                entry.right().hardStop();
                itr.remove();
            }
        }
        MUSIC_MANAGERS.clear();
    }

    //    static long lastMillis;
//    static long lastTick;
//    static long runningMillis;
//
//    //fixme: something is terribly desynced
//    @SubscribeEvent
//    public static void tick(TickEvent.RenderTickEvent event) {
//        if(event.phase == TickEvent.Phase.END){
//            return;
//        }
//        if (lastMillis == 0) {
//            lastMillis = System.currentTimeMillis();
//            lastTick = System.currentTimeMillis();
//        }
//        long currentMillis = System.currentTimeMillis();
//        if (!Minecraft.getInstance().isPaused()) {
//            runningMillis += currentMillis - lastMillis;
//        }
//        lastMillis = currentMillis;
//        if (Minecraft.getInstance().player != null && runningMillis - lastTick > 50) {
//            lastTick = runningMillis;
//            var manager = getManagerFor(Minecraft.getInstance().player.level.dimension());
//            if (manager.musicHandlers.isEmpty()) {
//                return;
//            }
//            var entry = manager.musicHandlers.peek();
//            UUID uuid = entry.left();
//            IMusicHandler musicHandler = entry.right();
//            if (manager.resumeNext) {
//                musicHandler.triggerResume();
//                manager.resumeNext = false;
//            }
//            if (musicHandler.isDone()) {
//                manager.musicHandlers.remove(uuid);
//            } else {
//                musicHandler.tick();
//            }
//        }
//    }
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().isPaused()) {
            var manager = getManagerFor(Minecraft.getInstance().player.level.dimension());
            if (manager.musicHandlers.isEmpty()) {
                return;
            }
//            var entry = manager.musicHandlers.lastEntry();
//            UUID uuid = entry.getKey();
//            IMusicHandler musicHandler = entry.getValue();
            var entry = manager.musicHandlers.peek();
            UUID uuid = entry.left();
            IMusicHandler musicHandler = entry.right();
            if (manager.resumeNext) {
                musicHandler.triggerResume();
                manager.resumeNext = false;
            }
            if (musicHandler.isDone()) {
                manager.musicHandlers.remove(entry);
            } else {
                musicHandler.tick();
            }
        }
    }
}
