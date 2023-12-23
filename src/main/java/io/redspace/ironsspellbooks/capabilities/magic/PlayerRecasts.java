package io.redspace.ironsspellbooks.capabilities.magic;

import com.google.common.collect.Maps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.network.ClientBoundSyncRecast;
import io.redspace.ironsspellbooks.network.ClientboundSyncRecasts;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class PlayerRecasts {
    private static final RecastInstance EMPTY = new RecastInstance(SpellRegistry.none().getSpellId(), 0, 0, null);
    private final Map<String, RecastInstance> recastLookup;

    public PlayerRecasts() {
        this.recastLookup = Maps.newHashMap();
    }

    public PlayerRecasts(Map<String, RecastInstance> recastLookup) {
        this.recastLookup = recastLookup;
    }

    public void addRecast(RecastInstance recastInstance, Player player) {
        recastLookup.put(recastInstance.spellId, recastInstance);

        if (player instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientBoundSyncRecast(recastInstance), serverPlayer);
        }
    }

    public void addRecast(@NotNull String spellId, int spellLevel, @NotNull Player player, ICastDataSerializable castData, int recastCount) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("RecastManager.addRecast spellId:{}, spellLevel:{}, player:{}", spellId, spellLevel, player);
        }

        var recastInstance = recastLookup.compute(spellId, (key, val) -> {
            if (val == null) {
                return new RecastInstance(spellId, spellLevel, recastCount, castData);
            } else {
                IronsSpellbooks.LOGGER.debug("RecastManager.addRecastData: recast data for spell already exists.. ignoring");
                return val;
            }
        });

        if (player instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientBoundSyncRecast(recastInstance), serverPlayer);
        }
    }

    public boolean hasRecastsActive() {
        return !recastLookup.isEmpty();
    }

    public boolean hasRecastForSpell(AbstractSpell spell) {
        return recastLookup.containsKey(spell.getSpellId());
    }

    public int getRemainingRecastsForSpell(AbstractSpell spell) {
        return recastLookup.getOrDefault(spell.getSpellId(), EMPTY).remainingRecasts;
    }

    public RecastInstance getRecastInstance(String spellId) {
        return recastLookup.get(spellId);
    }

    public Collection<RecastInstance> getAllActiveRecasts() {
        return recastLookup.values();
    }

    public void decrementRecastCount(AbstractSpell spell) {
        var recastData = recastLookup.compute(spell.getSpellId(), (k, v) -> {
            if (v == null || v.remainingRecasts <= 1) {
                return null;
            } else {
                v.remainingRecasts--;
                return v;
            }
        });
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientboundSyncRecasts(recastLookup), serverPlayer);
    }

    public void saveNBTData(ListTag listTag) {
        recastLookup.forEach((spellId, recastInstance) -> {
            if (recastInstance.remainingRecasts > 0) {
                listTag.add(recastInstance.serializeNBT());
            }
        });
    }

    public void loadNBTData(ListTag listTag) {
        if (listTag != null) {
            listTag.forEach(tag -> {
                var tmp = new RecastInstance();
                tmp.deserializeNBT((CompoundTag) tag);
                recastLookup.put(tmp.spellId, tmp);
            });
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        recastLookup.values().forEach(recast -> {
            sb.append(recast.toString());
        });

        return sb.toString();
    }
}
