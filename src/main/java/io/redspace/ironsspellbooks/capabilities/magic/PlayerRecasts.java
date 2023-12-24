package io.redspace.ironsspellbooks.capabilities.magic;

import com.google.common.collect.Maps;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.network.ClientBoundRemoveRecast;
import io.redspace.ironsspellbooks.network.ClientBoundSyncRecast;
import io.redspace.ironsspellbooks.network.ClientboundSyncRecasts;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PlayerRecasts {
    private static final RecastInstance EMPTY = new RecastInstance(SpellRegistry.none().getSpellId(), 0, 0, 0, null);
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
        recastLookup.values().forEach(recastInstance -> {
            recastInstance.expireOnTick = recastInstance.ticksRemaining + Minecraft.getInstance().level.getGameTime();
        });
        this.recastLookup = recastLookup;
        this.serverPlayer = null;
    }

    private void setExpiration(RecastInstance recastInstance) {
        if (recastInstance == null) {
            return;
        }

        if (serverPlayer == null) {
            recastInstance.expireOnTick = Minecraft.getInstance().level.getGameTime() + recastInstance.ticksToLive;
        } else {
            recastInstance.expireOnTick = serverPlayer.level.getGameTime() + recastInstance.ticksToLive;
        }
    }

    public void addRecast(RecastInstance recastInstance) {
        var existingRecastInstance = recastLookup.get(recastInstance.spellId);

        if (!isRecastActive(existingRecastInstance)) {
            setExpiration(recastInstance);
            recastLookup.put(recastInstance.spellId, recastInstance);
            syncToPlayer(recastInstance);
        }
    }

    public void addRecast(@NotNull String spellId, int spellLevel, int recastCount, int ticksRemaining, ICastDataSerializable castData) {
        addRecast(new RecastInstance(spellId, spellLevel, recastCount, ticksRemaining, castData));
    }

    public boolean isRecastActive(RecastInstance recastInstance) {
        if (recastInstance == null) {
            return false;
        }

        if (serverPlayer == null) {
            return recastInstance.remainingRecasts > 0 && recastInstance.expireOnTick > Minecraft.getInstance().level.getGameTime();
        }

        return recastInstance.expireOnTick > serverPlayer.level.getGameTime();
    }

    @OnlyIn(Dist.CLIENT)
    public void removeRecast(String spellId) {
        recastLookup.remove(spellId);
    }

    @OnlyIn(Dist.CLIENT)
    public void forceAddRecast(RecastInstance recastInstance) {
        recastInstance.expireOnTick = Minecraft.getInstance().level.getGameTime() + recastInstance.ticksRemaining;
        recastLookup.put(recastInstance.spellId, recastInstance);
    }

    public boolean hasRecastsActive() {
        return !recastLookup.isEmpty() && recastLookup.values().stream().anyMatch(this::isRecastActive);
    }

    public boolean hasRecastForSpell(AbstractSpell spell) {
        return isRecastActive(recastLookup.get(spell.getSpellId()));
    }

    public int getRemainingRecastsForSpell(AbstractSpell spell) {
        var recastInstance = recastLookup.getOrDefault(spell.getSpellId(), EMPTY);

        if (isRecastActive(recastInstance)) {
            return recastInstance.remainingRecasts;
        }

        return 0;
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

//    public void tick() {
//        recastLookup.values()
//                .stream()
//                .filter(r -> --r.ticksToLive <= 0)
//                .toList()
//                .forEach(recastInstance -> {
//                    recastLookup.remove(recastInstance.spellId);
//                    syncRemoveToPlayer(recastInstance.spellId);
//                });
//    }

    public void decrementRecastCount(AbstractSpell spell) {
        var recastInstance = recastLookup.get(spell.getSpellId());

        if (isRecastActive(recastInstance)) {
            recastInstance.remainingRecasts--;

            if (recastInstance.remainingRecasts > 0) {
                recastInstance.expireOnTick = recastInstance.ticksToLive + serverPlayer.level.getGameTime();
                syncToPlayer(recastInstance);
            } else {
                recastLookup.remove(spell.getSpellId());
                syncRemoveToPlayer(spell.getSpellId());
            }
        } else if (recastInstance != null) {
            recastLookup.remove(spell.getSpellId());
            syncRemoveToPlayer(spell.getSpellId());
        }
    }

    private void updateAllRemainingTime() {
        recastLookup.values().forEach(recastInstance -> {
            var gameTicks = serverPlayer.level.getGameTime();
            recastInstance.ticksRemaining = recastInstance.expireOnTick - gameTicks;
        });
    }

    public void syncToPlayer(RecastInstance recastInstance) {
        if (serverPlayer != null) {
            var gameTicks = serverPlayer.level.getGameTime();
            recastInstance.ticksRemaining = recastInstance.expireOnTick - gameTicks;
            Messages.sendToPlayer(new ClientBoundSyncRecast(recastInstance), serverPlayer);
        }
    }

    public void syncAllToPlayer() {
        if (serverPlayer != null) {
            updateAllRemainingTime();
            Messages.sendToPlayer(new ClientboundSyncRecasts(recastLookup), serverPlayer);
        }
    }

    public void syncRemoveToPlayer(String spellId) {
        if (serverPlayer != null) {
            Messages.sendToPlayer(new ClientBoundRemoveRecast(spellId), serverPlayer);
        }
    }

    public void clear() {
        recastLookup.clear();
        syncAllToPlayer();
    }

    public ListTag saveNBTData() {
        updateAllRemainingTime();
        var listTag = new ListTag();
        recastLookup.values().stream().filter(this::isRecastActive).forEach(recastInstance -> {
            if (recastInstance.remainingRecasts > 0) {
                listTag.add(recastInstance.serializeNBT());
            }
        });
        return listTag;
    }

    public void loadNBTData(ListTag listTag) {
        if (listTag != null) {
            listTag.forEach(tag -> {
                var tmp = new RecastInstance();
                tmp.deserializeNBT((CompoundTag) tag);
                if (tmp.remainingRecasts > 0) { //failsafe
                    recastLookup.put(tmp.spellId, tmp);
                }

                tmp.expireOnTick = serverPlayer.level.getGameTime() + tmp.ticksRemaining;
            });
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        recastLookup.values().forEach(recast -> {
            sb.append(recast.toString());
            sb.append("\n");
        });

        return sb.toString();
    }
}
