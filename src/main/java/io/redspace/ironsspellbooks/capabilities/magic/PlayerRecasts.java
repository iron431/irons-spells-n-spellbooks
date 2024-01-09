package io.redspace.ironsspellbooks.capabilities.magic;

import com.google.common.collect.Maps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.network.ClientBoundRemoveRecast;
import io.redspace.ironsspellbooks.network.ClientBoundSyncRecast;
import io.redspace.ironsspellbooks.network.ClientboundSyncRecasts;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

public class PlayerRecasts {
    private static final RecastInstance EMPTY = new RecastInstance(SpellRegistry.none().getSpellId(), 0, 0, 0, CastSource.NONE, null);
    private final Map<String, RecastInstance> recastLookup;

    //This will only be null on the client side
    private final ServerPlayer serverPlayer;

    public PlayerRecasts() {
        this.recastLookup = Maps.newHashMap();
        this.serverPlayer = null;
    }

    public PlayerRecasts(ServerPlayer serverPlayer) {
        this.recastLookup = Maps.newHashMap();
        this.serverPlayer = serverPlayer;
    }

    @OnlyIn(Dist.CLIENT)
    public PlayerRecasts(Map<String, RecastInstance> recastLookup) {
        this.recastLookup = recastLookup;
        this.serverPlayer = null;
    }

    public boolean addRecast(RecastInstance recastInstance, MagicData magicData) {
        var existingRecastInstance = recastLookup.get(recastInstance.spellId);

        if (!isRecastActive(existingRecastInstance)) {
            magicData.getPlayerCooldowns().removeCooldown(recastInstance.spellId);
            recastLookup.put(recastInstance.spellId, recastInstance);
            syncToPlayer(recastInstance);
            return true;
        }

        return false;
    }

    public boolean isRecastActive(RecastInstance recastInstance) {
        return recastInstance != null && recastInstance.remainingRecasts > 0 && recastInstance.remainingTicks > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public void removeRecast(String spellId) {
        recastLookup.remove(spellId);
    }

    @OnlyIn(Dist.CLIENT)
    public void forceAddRecast(RecastInstance recastInstance) {
        recastLookup.put(recastInstance.spellId, recastInstance);
    }

    @OnlyIn(Dist.CLIENT)
    public void tickRecasts() {
        if (!recastLookup.isEmpty()) {
            recastLookup.values().stream().toList().forEach(x -> x.remainingTicks--);
        }
    }

    public boolean hasRecastsActive() {
        return !recastLookup.isEmpty();
    }

    public boolean hasRecastForSpell(AbstractSpell spell) {
        return isRecastActive(recastLookup.get(spell.getSpellId()));
    }

    public boolean hasRecastForSpell(String spellId) {
        return isRecastActive(recastLookup.get(spellId));
    }

    public int getRemainingRecastsForSpell(String spellId) {
        var recastInstance = recastLookup.getOrDefault(spellId, EMPTY);

        if (isRecastActive(recastInstance)) {
            return recastInstance.remainingRecasts;
        }

        return 0;
    }

    public int getRemainingRecastsForSpell(AbstractSpell spell) {
        return getRemainingRecastsForSpell(spell.getSpellId());
    }

    public RecastInstance getRecastInstance(String spellId) {
        return recastLookup.get(spellId);
    }

    public List<RecastInstance> getAllRecasts() {
        return recastLookup.values().stream().toList();
    }

    public List<RecastInstance> getActiveRecasts() {
        return recastLookup.values().stream().filter(this::isRecastActive).toList();
    }

    public void decrementRecastCount(String spellId) {
        //IronsSpellbooks.LOGGER.debug("PlayerRecasts: {} {}", serverPlayer, spellId);
        var recastInstance = recastLookup.get(spellId);

        if (isRecastActive(recastInstance)) {
            recastInstance.remainingRecasts--;

            if (recastInstance.remainingRecasts > 0) {
                recastInstance.remainingTicks = recastInstance.ticksToLive;
                syncToPlayer(recastInstance);
            } else {
                removeRecast(recastInstance, RecastResult.USED_ALL_RECASTS);
            }
        } else if (recastInstance != null) {
            removeRecast(recastInstance, RecastResult.TIMEOUT);
        }
    }

    public void decrementRecastCount(AbstractSpell spell) {
        decrementRecastCount(spell.getSpellId());
    }

    public void syncAllToPlayer() {
        if (serverPlayer != null) {
            Messages.sendToPlayer(new ClientboundSyncRecasts(recastLookup), serverPlayer);
        }
    }

    public void syncToPlayer(RecastInstance recastInstance) {
        if (serverPlayer != null) {
            Messages.sendToPlayer(new ClientBoundSyncRecast(recastInstance), serverPlayer);
        }
    }

    public void syncRemoveToPlayer(String spellId) {
        if (serverPlayer != null) {
            Messages.sendToPlayer(new ClientBoundRemoveRecast(spellId), serverPlayer);
        }
    }

    private void triggerRecastComplete(RecastInstance recastInstance, RecastResult recastResult) {
        SpellRegistry.getSpell(recastInstance.getSpellId()).onRecastFinished(serverPlayer, recastInstance, recastResult, recastInstance.castData);
    }

    private void removeRecast(RecastInstance recastInstance, RecastResult recastResult, boolean doSync) {
        recastLookup.remove(recastInstance.spellId);
        if (doSync) {
            syncRemoveToPlayer(recastInstance.spellId);
        }
        triggerRecastComplete(recastInstance, recastResult);
    }

    public void removeRecast(RecastInstance recastInstance, RecastResult recastResult) {
        removeRecast(recastInstance, recastResult, true);
    }

    public void removeAll(RecastResult recastResult) {
        recastLookup.values().stream().toList().forEach(recastInstance -> removeRecast(recastInstance, recastResult, false));
        syncAllToPlayer();
    }

    public ListTag saveNBTData() {
        var listTag = new ListTag();
        recastLookup.values().stream().filter(this::isRecastActive).forEach(recastInstance -> {
            if (recastInstance.remainingRecasts > 0 && recastInstance.remainingTicks > 0) {
                listTag.add(recastInstance.serializeNBT());
            }
        });
        return listTag;
    }

    public void loadNBTData(ListTag listTag) {
        if (listTag != null) {
            listTag.forEach(tag -> {
                var recastInstance = new RecastInstance();
                recastInstance.deserializeNBT((CompoundTag) tag);
                if (recastInstance.remainingRecasts > 0 && recastInstance.remainingTicks > 0) {
                    recastLookup.put(recastInstance.spellId, recastInstance);
                } else {
                    //cull anything leftover not removed. shouldn't get here
                    IronsSpellbooks.LOGGER.warn("Trimming recast data: {}", recastInstance);
                }
            });
        }
    }

    public void tick(int actualTicks) {
        if (serverPlayer != null && serverPlayer.level.getGameTime() % actualTicks == 0) {
            recastLookup.values()
                    .stream()
                    .filter(r -> {
                        r.remainingTicks -= actualTicks;
                        return r.remainingTicks <= 0;
                    })
                    .toList()
                    .forEach(recastInstance -> removeRecast(recastInstance, RecastResult.TIMEOUT));
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        recastLookup.values().forEach(recastInstance -> {
            sb.append(recastInstance.toString());
            sb.append("\n");
        });

        return sb.toString();
    }
}
