package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EventBusSubscriber
public class SummonManager implements INBTSerializable<CompoundTag> {

    record ExpirationInstance(UUID uuid, int summonedServerTick, int expirationServerTick) {
    }

    public static final SummonManager INSTANCE = new SummonManager();

    /**
     * Map of Owner UUID to a List of their serialized summoned mobs, including additional "summon_duration_remaining" entry.
     * It is populated when a player disconnects, including when the server stops. This means it is the only thing that must be written to disk.
     */
    private final HashMap<UUID, List<CompoundTag>> offlineSummonersToSavedEntities = new HashMap<>();
    /**
     * A quick lookup holding a summon's owner information. A summon can only have 1 owner
     */
    private final HashMap<UUID, UUID> summonToOwner = new HashMap<>();
    /**
     * Map of Owner UUID to a set of UUID's of active summoned creatures
     */
    private final HashMap<UUID, Set<UUID>> ownerToSummons = new HashMap<>();
    /**
     * Heap-Based Priority Queue of Expiration Instances, sorted by nearest to expiration time. Chosen for Constant-time peeking, as queue/dequeue operations are separated by large quantities of real-life time
     */
    private final PriorityQueue<ExpirationInstance> summonExpirations = new PriorityQueue<>(Comparator.comparingInt(ExpirationInstance::expirationServerTick));

    /**
     * Attempts to perform entity-lookup for the owner of the summon. Always returns null on the client.
     */
    public static @Nullable Entity getOwner(@NotNull Entity summon) {
        //todo: cache results?
        if (summon.level instanceof ServerLevel serverLevel) {
            if (INSTANCE.summonToOwner.containsKey(summon.getUUID())) {
                return serverLevel.getEntity(INSTANCE.summonToOwner.get(summon.getUUID()));
            }
        } else {
            IronsSpellbooks.LOGGER.warn("Summon {} attempting to lookup owner from client!", summon);
        }
        return null;
    }

    public static Set<UUID> getSummons(Entity owner) {
        return Set.copyOf(INSTANCE.ownerToSummons.getOrDefault(owner.getUUID(), Set.of()));
    }

    /**
     * Assigns summon to given owner. Removes any previously present associations
     */
    public static void setOwner(@NotNull Entity summon, @NotNull Entity owner) {
        removeSummon(summon);
        INSTANCE.summonToOwner.put(summon.getUUID(), owner.getUUID());
        startTrackingSummon(owner, summon);
        IronsDataStorage.INSTANCE.setDirty();
    }

    /**
     * Helper to handle all summon initialization logic
     */
    public static void initSummon(Entity owner, Entity summon, int duration, SummonedEntitiesCastData summonedEntitiesCastData) {
        setOwner(summon, owner);
        setDuration(summon, duration);
        summonedEntitiesCastData.add(summon);
    }

    /**
     * Begins tracking this summon for a duration, upon which calling {@link IMagicSummon#onUnSummon()} or {@link Entity#discard()} after the set time
     *
     * @param summon   Entity to track
     * @param duration Duration to live in ticks
     */
    public static void setDuration(Entity summon, int duration) {
        if (!(summon.level instanceof ServerLevel serverLevel)) return;
        INSTANCE.summonExpirations.add(new ExpirationInstance(summon.getUUID(), serverLevel.getServer().getTickCount(), serverLevel.getServer().getTickCount() + duration));
    }

    /**
     * Removes summon from ownership of its owner, and handles all cleanup thereof
     *
     * @param summon Entity to decouple from its owner
     */
    public static void removeSummon(Entity summon) {
        // decouple summon from owner
        UUID owner = INSTANCE.summonToOwner.remove(summon.getUUID());
        if (owner == null) {
            return;
        }
        IronsDataStorage.INSTANCE.setDirty();
        Set<UUID> summons = INSTANCE.ownerToSummons.get(owner);
        if (summons == null) {
            return;
        }
        // remove summon from owner's "party"
        var summonUuid = summon.getUUID();
        summons.remove(summonUuid);
        IronsDataStorage.INSTANCE.setDirty();
        if (summons.isEmpty()) {
            INSTANCE.ownerToSummons.remove(owner);
        }
        if (summon.level instanceof ServerLevel serverLevel) {
            removeFromRecastData(serverLevel, owner, summonUuid);
        }
    }

    /**
     * Removes active summons from the world and serializes them to world storage
     *
     * @param serverPlayer
     */
    public void handlePlayerDisconnect(ServerPlayer serverPlayer) {
        Set<UUID> summons = ownerToSummons.get(serverPlayer.getUUID());
        if (summons == null) {
            return;
        }
        var serverLevel = serverPlayer.serverLevel();
        var savedSummons = new ArrayList<CompoundTag>();
        for (UUID uuid : summons) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity != null) {
                CompoundTag saveData = new CompoundTag();
                entity.save(saveData);
                int durationRemaining = INSTANCE.getExpirationTick(entity.getUUID()) - serverLevel.getServer().getTickCount();
                saveData.putInt("summon_duration_remaining", durationRemaining);
                entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                savedSummons.add(saveData);
            }
        }
        IronsDataStorage.INSTANCE.setDirty();
        INSTANCE.offlineSummonersToSavedEntities.put(serverPlayer.getUUID(), savedSummons);
        INSTANCE.stopTrackingSummonerAndSummons(serverPlayer);
    }

    /**
     * Handles unsummon functionality of a Recast Finishing, including manual recast or recast timing out. Takes item buffs into account.
     *
     * @return Whether the cooldown should be applied for the spell
     */
    public static boolean recastFinishedHelper(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (recastResult == RecastResult.COUNTERSPELL) {
            //ignore counterspell
            MagicData.getPlayerMagicData(serverPlayer).getPlayerRecasts().forceAddRecast(recastInstance);
        } else if (recastResult != RecastResult.TIMEOUT) { // timeouts are handled by summon manager
            if (castDataSerializable instanceof SummonedEntitiesCastData summonedEntitiesCastData) {
                var serverLevel = serverPlayer.serverLevel();
                summonedEntitiesCastData.getSummons().forEach(uuid -> {
                    var toRemove = serverLevel.getEntity(uuid);
                    if (toRemove instanceof IMagicSummon summon) {
                        summon.onUnSummon();
                    } else if (toRemove != null) {
                        toRemove.discard();
                    }
                });
            }
        } else if (ItemRegistry.GREATER_CONJURERS_TALISMAN.get().isEquippedBy(serverPlayer)) {
            return false;
        }
        return true;
    }

    /**
     * Iterates over summoner's recast data until finding where the given summon lives, and handles {@link SummonedEntitiesCastData#handleRemove(UUID, MagicData, RecastInstance)}
     */
    private static void removeFromRecastData(ServerLevel level, UUID ownerUuid, UUID summonUuid) {
        if (!(level.getEntity(ownerUuid) instanceof Player player)) return;
        var playerMagicData = MagicData.getPlayerMagicData(player);
        var recasts = playerMagicData.getPlayerRecasts();
        for (RecastInstance recastInstance : recasts.getActiveRecasts()) {
            if (recastInstance.getCastData() instanceof SummonedEntitiesCastData summonData) {
                if (summonData.getSummons().contains(summonUuid)) {
                    summonData.handleRemove(summonUuid, playerMagicData, recastInstance);
                    break;
                }
            }
        }
    }

    /**
     * Linear search over {@link SummonManager#summonExpirations}
     */
    private Optional<ExpirationInstance> getExpirationInstance(UUID uuid) {
        for (ExpirationInstance inst : summonExpirations) {
            if (inst.uuid.equals(uuid)) {
                return Optional.of(inst);
            }
        }
        return Optional.empty();
    }

    private int getExpirationTick(UUID uuid) {
        return getExpirationInstance(uuid).map(ExpirationInstance::expirationServerTick).orElse(0);
    }

    /**
     * Links summon to owner, and adds to or initializes owner's summon tracking
     */
    private static void startTrackingSummon(Entity owner, Entity summon) {
        Set<UUID> summons = INSTANCE.ownerToSummons.computeIfAbsent(owner.getUUID(), uuid -> new HashSet<>());
        summons.add(summon.getUUID());
        IronsDataStorage.INSTANCE.setDirty();
    }

    /**
     * Removes the summoner and their summons from {@link SummonManager#ownerToSummons}, {@link SummonManager#summonToOwner}, and {@link SummonManager#summonExpirations}
     */
    private void stopTrackingSummonerAndSummons(Entity summoner) {
        var summons = ownerToSummons.remove(summoner.getUUID());
        if (summons != null) {
            IronsDataStorage.INSTANCE.setDirty();
            summons.forEach(summonUUID -> {
                if (summonToOwner.remove(summonUUID) != null) {
                    getExpirationInstance(summonUUID).ifPresent(summonExpirations::remove);
                }
            });
        }
    }

    /**
     * Stops tracking the expiration time for this entity
     */
    public static void stopTrackingExpiration(Entity summon){
        INSTANCE.getExpirationInstance(summon.getUUID()).ifPresent(INSTANCE.summonExpirations::remove);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider pRegistries) {
        CompoundTag manager = new CompoundTag();
        ListTag offlineSummonsInstances = new ListTag();
        for (var entry : offlineSummonersToSavedEntities.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("summoner", entry.getKey());
            ListTag summons = new ListTag();
            summons.addAll(entry.getValue());
            tag.put("summons", summons);
            offlineSummonsInstances.add(tag);
        }
        manager.put("OfflineSummons", offlineSummonsInstances);
        return manager;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider pRegistries, CompoundTag compoundTag) {
        ListTag offline = compoundTag.getList("OfflineSummons", Tag.TAG_COMPOUND);
        for (Tag tag : offline) {
            CompoundTag entry = (CompoundTag) tag;
            var uuid = entry.getUUID("summoner");
            ListTag summons = entry.getList("summons", Tag.TAG_COMPOUND);
            ArrayList<CompoundTag> summonsList = new ArrayList<>();
            summons.forEach(t -> summonsList.add((CompoundTag) t));
            offlineSummonersToSavedEntities.put(uuid, summonsList);
        }
    }

    @SubscribeEvent
    public static void levelTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        int tick = server.getTickCount();
        if (!INSTANCE.summonExpirations.isEmpty() && tick % 20 == 0) {
            var nextDespawn = INSTANCE.summonExpirations.peek();
            while (nextDespawn.expirationServerTick < tick) {
                INSTANCE.summonExpirations.remove();

                var uuid = nextDespawn.uuid;
                Entity toRemove = Streams.of(server.getAllLevels())
                        .map(level -> level.getEntity(uuid))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
                if (toRemove instanceof IMagicSummon summon) {
                    summon.onUnSummon();
                } else if (toRemove != null) {
                    toRemove.discard();
                }

                if (INSTANCE.summonExpirations.isEmpty()) {
                    break;
                } else {
                    nextDespawn = INSTANCE.summonExpirations.peek();
                }
            }
        }
        if (Log.SUMMON_MANAGER && tick % 100 == 0) {
            IronsSpellbooks.LOGGER.debug("SummonManagerDump -------------");
            IronsSpellbooks.LOGGER.debug("SummonManagerDump ownerToSummons: {}", INSTANCE.ownerToSummons.toString());
            IronsSpellbooks.LOGGER.debug("SummonManagerDump summonToOwner: {}", INSTANCE.summonToOwner.toString());
            IronsSpellbooks.LOGGER.debug("SummonManagerDump summonExpirations: {}", INSTANCE.summonExpirations.toString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            INSTANCE.handlePlayerDisconnect(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        event.getServer().getPlayerList().getPlayers().forEach(INSTANCE::handlePlayerDisconnect);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        if (player.level instanceof ServerLevel serverLevel) {
            IronsDataStorage.INSTANCE.setDirty();
            var savedSummons = INSTANCE.offlineSummonersToSavedEntities.remove(player.getUUID());
            var server = serverLevel.getServer();
            if (savedSummons != null) {
                //fixme: summoner's dimension takes precedent
                Set<UUID> summonsSet = new HashSet<>();
                UUID ownerUUID = player.getUUID();
                for (CompoundTag summon : savedSummons) {
                    var summonedEntity = EntityType.create(summon, serverLevel).orElse(null);
                    if (summonedEntity != null) {
                        serverLevel.addFreshEntityWithPassengers(summonedEntity);
                        var summonUUID = summonedEntity.getUUID();
                        summonsSet.add(summonUUID);
                        INSTANCE.summonToOwner.put(summonUUID, ownerUUID);
                        INSTANCE.summonExpirations.add(new ExpirationInstance(summonUUID, server.getTickCount(), server.getTickCount() + summon.getInt("summon_duration_remaining")));
                    }
                    INSTANCE.ownerToSummons.put(ownerUUID, summonsSet);
                }
            }
        }
    }
}