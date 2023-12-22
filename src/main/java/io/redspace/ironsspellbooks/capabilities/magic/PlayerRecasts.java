package io.redspace.ironsspellbooks.capabilities.magic;

import com.google.common.collect.Maps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.network.ClientboundSyncRecasts;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlayerRecasts {
    public static final String SPELL_ID = "id";
    private static final RecastInstance EMPTY = new RecastInstance(0, null, 0);

    //spell type and for how many more ticks it will be on cooldown
    private final Map<String, RecastInstance> recastLookup;

    public PlayerRecasts() {
        this.recastLookup = Maps.newHashMap();
    }

    public boolean hasRecastsActive() {
        return !recastLookup.isEmpty();
    }

    public Map<String, RecastInstance> getAllRecastData() {
        return recastLookup;
    }

    public void addRecastData(@NotNull Level level, @NotNull AbstractSpell spell, int spellLevel, @NotNull Player player, ICastDataSerializable castData, int recastCount) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("RecastManager.addRecast spell:{}, spellLevel:{}, player:{}", spell, spellLevel, player);
        }

        if (level.isClientSide) return;

        recastLookup.compute(spell.getSpellId(), (key, val) -> {
            if (val == null) {
                return new RecastInstance(spellLevel, castData, recastCount);
            } else {
                IronsSpellbooks.LOGGER.debug("RecastManager.addRecastData: recast data for spell already exists.. ignoring");
                return val;
            }
        });
    }

    public boolean hasRecastData(AbstractSpell spell) {
        return recastLookup.containsKey(spell.getSpellId());
    }

    public int getRemainingRecasts(AbstractSpell spell) {
        return recastLookup.getOrDefault(spell.getSpellId(), EMPTY).remainingRecasts;
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
        recastLookup.forEach((spellId, recastData) -> {
            if (recastData.remainingRecasts > 0) {
                CompoundTag ct = new CompoundTag();
                ct.putString(SPELL_ID, spellId);
                recastData.
                        ct.putInt(SPELL_COOLDOWN, cooldown.getSpellCooldown());
                ct.putInt(COOLDOWN_REMAINING, cooldown.getCooldownRemaining());
                listTag.add(ct);
            }
        });
    }

    public void loadNBTData(ListTag listTag) {
        if (listTag != null) {
            listTag.forEach(tag -> {
                CompoundTag t = (CompoundTag) tag;
                String spellId = t.getString(SPELL_ID);
                int spellCooldown = t.getInt(SPELL_COOLDOWN);
                int cooldownRemaining = t.getInt(COOLDOWN_REMAINING);
                spellCooldowns.put(spellId, new CooldownInstance(spellCooldown, cooldownRemaining));
            });
        }
    }


}
