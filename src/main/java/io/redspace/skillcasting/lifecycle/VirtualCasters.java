package io.redspace.skillcasting.lifecycle;

import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.cast.CasterId;
import io.redspace.skillcasting.data.cast.VirtualCaster;
import io.redspace.skillcasting.data.cast.VirtualCasterRef;
import io.redspace.skillcasting.network.VirtualCasterAddPacket;
import io.redspace.skillcasting.network.VirtualCasterRemovePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VirtualCasters {
    // one store per logical side: the integrated server runs both in one JVM
    private static final Map<ResourceKey<Level>, Map<UUID, VirtualCaster>> SERVER = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<UUID, VirtualCaster>> CLIENT = new ConcurrentHashMap<>();

    private VirtualCasters() {
    }

    private static Map<UUID, VirtualCaster> store(Level level) {
        return (level.isClientSide() ? CLIENT : SERVER).computeIfAbsent(level.dimension(), key -> new ConcurrentHashMap<>());
    }

    public static @Nullable VirtualCaster get(Level level, UUID id) {
        return store(level).get(id);
    }

    public static VirtualCasterRef spawn(ServerLevel level, Vec3 position, Vec3 forward, @Nullable Entity owner) {
        Vec3 direction = forward.normalize();
        int ownerEntityId = owner == null ? -1 : owner.getId();
        VirtualCaster caster = new VirtualCaster(level, UUID.randomUUID(), position, direction, owner == null ? null : owner.getUUID(), ownerEntityId);
        store(level).put(caster.id(), caster);
        VirtualCasterRef ref = new VirtualCasterRef(caster);
        ref.distributeToClients(new VirtualCasterAddPacket(ref.id(), position, direction, ownerEntityId));
        return ref;
    }

    public static void discard(VirtualCasterRef ref) {
        VirtualCaster caster = ref.caster();
        caster.markRemoved();
        store(caster.level()).remove(caster.id());
        ref.distributeToClients(new VirtualCasterRemovePacket(ref.id()));
    }

    /**
     * Discards every server caster whose cast has ended. Runs after {@link SkillcastingManager#serverTick()}, so a cast's stop packet reaches clients before the remove packet.
     */
    public static void sweep() {
        List<VirtualCasterRef> finished = new ArrayList<>();
        for (Map<UUID, VirtualCaster> casters : SERVER.values()) {
            for (VirtualCaster caster : casters.values()) {
                if (!SkillcastingData.has(caster) || !SkillcastingData.get(caster).isCasting()) {
                    finished.add(new VirtualCasterRef(caster));
                }
            }
        }
        finished.forEach(VirtualCasters::discard);
    }

    public static void addClient(Level level, CasterId.Virtual id, Vec3 position, Vec3 forward, int ownerEntityId) {
        if (level.dimension().equals(id.dimension())) {
            store(level).put(id.id(), new VirtualCaster(level, id.id(), position, forward, null, ownerEntityId));
        }
    }

    public static void removeClient(Level level, CasterId.Virtual id) {
        VirtualCaster caster = store(level).remove(id.id());
        if (caster != null) {
            caster.markRemoved();
        }
    }

    public static void clear(Level level) {
        Map<UUID, VirtualCaster> casters = (level.isClientSide() ? CLIENT : SERVER).remove(level.dimension());
        if (casters != null) {
            casters.values().forEach(VirtualCaster::markRemoved);
        }
    }

    public static void clearAll() {
        SERVER.clear();
        CLIENT.clear();
    }
}
