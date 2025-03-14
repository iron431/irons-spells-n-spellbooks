package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber
public class MusicManager {
    private static final Map<ResourceKey<Level>, MusicManager> MUSIC_MANAGERS = new HashMap<>();
    private final LinkedHashMap<UUID, IMusicHandler> musicHandlers = new LinkedHashMap<>();
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
            manager.musicHandlers.lastEntry().getValue().stop();
        }
        event.init();
        manager.musicHandlers.put(id, event);
    }

    public static void stopEvent(UUID uuid) {
        // while we only create events per-dimension, if something in any dimension calls for a specific uuid to be cancelled, we cancel it
        for (MusicManager manager : MUSIC_MANAGERS.values()) {
            if (manager.musicHandlers.containsKey(uuid)) {
                manager.musicHandlers.remove(uuid).stop();
                if (!manager.musicHandlers.isEmpty()) {
                    manager.resumeNext = true;
                }
            }
        }
    }

    private static MusicManager getManagerFor(ResourceKey<Level> dimension) {
        return MUSIC_MANAGERS.computeIfAbsent(dimension, (dim) -> new MusicManager());
    }

    public static void clear() {
        for (MusicManager m : MUSIC_MANAGERS.values()) {
            for (IMusicHandler h : m.musicHandlers.values()) {
                h.hardStop();
            }
        }
        MUSIC_MANAGERS.clear();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().isPaused()) {
            var manager = getManagerFor(Minecraft.getInstance().player.level.dimension());
            if (manager.musicHandlers.isEmpty()) {
                return;
            }
            var entry = manager.musicHandlers.lastEntry();
            UUID uuid = entry.getKey();
            IMusicHandler musicHandler = entry.getValue();
            if (manager.resumeNext) {
                musicHandler.triggerResume();
                manager.resumeNext = false;
            }
            if (musicHandler.isDone()) {
                manager.musicHandlers.remove(uuid);
            } else {
                musicHandler.tick();
            }
        }
    }
}
