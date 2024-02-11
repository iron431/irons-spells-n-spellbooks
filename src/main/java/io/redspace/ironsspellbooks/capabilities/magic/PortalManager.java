package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

//@Mod.EventBusSubscriber
public class PortalManager implements INBTSerializable<CompoundTag> {

    public static final PortalManager INSTANCE = new PortalManager();

    //HashMap<PortalID, HashMap<EntityId, CooldownExpiration>>
    private final HashMap<UUID, HashMap<UUID, AtomicInteger>> cooldownLookup = new HashMap<>();

    //HashMap<PortalID, PortalData>
    private final HashMap<UUID, PortalData> portalLookup = new HashMap<>();

    private static final int cooldownTicks = 10;

    public PortalData getPortalData(PortalEntity portalEntity) {
        return portalLookup.get(portalEntity.getUUID());
    }

    public void addPortalData(UUID portalEntityUUID, PortalData portalData) {
        portalLookup.put(portalEntityUUID, portalData);
        IronsDataStorage.INSTANCE.setDirty();
    }

    public void addPortalCooldown(Entity entity, UUID portalId) {
        //IronsSpellbooks.LOGGER.debug("addPortalCooldown: entity:{} portal:{}", entity, portalId);
        var portalData = portalLookup.get(portalId);

        if (portalData == null) {
            return;
        }

        var playerMap = cooldownLookup.computeIfAbsent(portalData.getConnectedPortalUUID(portalId), k -> new HashMap<>());
        playerMap.put(entity.getUUID(), new AtomicInteger(cooldownTicks));
    }

    public boolean isEntityOnCooldown(Entity entity, UUID portalId) {
        var playerMap = cooldownLookup.get(portalId);

        if (playerMap != null && playerMap.containsKey(entity.getUUID())) {
            //IronsSpellbooks.LOGGER.debug("isEntityOnCooldown.true entity:{}, portal:{}", entity, portalId);
            return true;
        }

        //IronsSpellbooks.LOGGER.debug("isEntityOnCooldown.false entity:{}, portal:{}", entity, portalId);
        return false;
    }

    public boolean isPortalConnected(UUID portalID) {
        var portalData = portalLookup.get(portalID);
        if (portalData != null) {
            var connectedPortal = portalData.getConnectedPortalUUID(portalID);
            if (connectedPortal != null) {
                return true;
            }
        }
        return false;
    }

    public boolean canUsePortal(PortalEntity portalEntity, Entity entity) {
        //IronsSpellbooks.LOGGER.debug("canUsePortal portal:{}, entity:{}", portalEntity.getUUID(), entity);
        if (portalEntity == null || entity == null) {
            return false;
        }

        var portalData = portalLookup.get(portalEntity.getUUID());

        return !entity.isPassenger() &&
                portalData != null &&
                portalData.portalEntityId1 != null &&
                portalData.portalEntityId2 != null &&
                portalLookup.containsKey(portalData.portalEntityId1) &&
                portalLookup.containsKey(portalData.portalEntityId2) &&
                !isEntityOnCooldown(entity, portalEntity.getUUID());
    }

    public void processCooldownTick(UUID portalUUID, int delta) {
        var playerCooldownsForPortal = cooldownLookup.get(portalUUID);
        if (playerCooldownsForPortal != null) {
            playerCooldownsForPortal.entrySet()
                    .stream()
                    .filter(item -> item.getValue().addAndGet(delta) <= 0)
                    .toList()
                    .forEach(itemToRemove -> playerCooldownsForPortal.remove(itemToRemove.getKey()));
        }
    }

    public void processDelayCooldown(UUID portalUUID, UUID playerUUID, int delta) {
        var playerCooldownsForPortal = cooldownLookup.get(portalUUID);
        if (playerCooldownsForPortal != null) {
            var cooldown = playerCooldownsForPortal.get(playerUUID);
            if (cooldown != null) {
                cooldown.addAndGet(delta);
            }
        }
    }

    public void removePortalData(UUID portalUUID) {
        portalLookup.remove(portalUUID);
        cooldownLookup.remove(portalUUID);
        IronsDataStorage.INSTANCE.setDirty();
    }

    public void killPortal(UUID portalUUID, UUID ownerUUID) {
        var removedPortalData = portalLookup.remove(portalUUID);

        if (removedPortalData != null) {
            if (removedPortalData.portalEntityId2 == null || removedPortalData.globalPos2 == null) {
                tryCancelRecast(portalUUID, ownerUUID);
            } else {
                var connectedPortalUUID = removedPortalData.getConnectedPortalUUID(portalUUID);
                if (connectedPortalUUID != null) {
                    removedPortalData = portalLookup.remove(connectedPortalUUID);

                    removedPortalData.getConnectedPortalPos(portalUUID).ifPresent(globalPos -> {
                        var level = IronsSpellbooks.MCS.getLevel(globalPos.dimension());
                        if (level != null) {
                            var connectedPortalToRemove = level.getEntity(connectedPortalUUID);
                            if (connectedPortalToRemove != null) {
                                connectedPortalToRemove.discard();
                            }
                        }
                    });
                    cooldownLookup.remove(connectedPortalUUID);
                }
            }
        }

        cooldownLookup.remove(portalUUID);
        IronsDataStorage.INSTANCE.setDirty();
    }

    private void tryCancelRecast(UUID portalUUID, UUID ownerUUID) {
        IronsSpellbooks.MCS.getAllLevels().forEach(level -> {
            var player = level.getPlayerByUUID(ownerUUID);
            if (player != null) {
                var magicData = MagicData.getPlayerMagicData(player);
                var playerRecasts = magicData.getPlayerRecasts();
                var spellId = SpellRegistry.PORTAL_SPELL.get().getSpellId();
                var recastInstance = playerRecasts.getRecastInstance(spellId);
                if (recastInstance != null) {
                    if (recastInstance.castData instanceof PortalData portalData) {
                        if (portalData.portalEntityId1 == portalUUID) {
                            playerRecasts.removeRecast(recastInstance, RecastResult.COUNTERSPELL);
                            return;
                        }
                    }
                }
            }
        });
    }

    @Override
    public CompoundTag serializeNBT() {
        //Ignoring cooldowns. Too short to care about persisting them

        var tag = new CompoundTag();
        var portalLookupTag = new ListTag();

        if (!portalLookup.isEmpty()) {
            portalLookupTag.addAll(portalLookup.entrySet().stream().map(entry -> {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putUUID("key", entry.getKey());
                itemTag.put("value", entry.getValue().serializeNBT());
                return itemTag;
            }).toList());
        }

        tag.put("portalLookup", portalLookupTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        if (compoundTag.contains("portalLookup")) {
            var portalLookupTag = (ListTag) compoundTag.get("portalLookup");
            if (portalLookupTag != null) {
                portalLookupTag.forEach(tag -> {
                    var portalLookupItem = (CompoundTag) tag;
                    var portalData = new PortalData();
                    portalData.deserializeNBT(portalLookupItem.getCompound("value"));
                    portalLookup.put(portalLookupItem.getUUID("key"), portalData);
                });
            }
        }
    }
}
