package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.network.ClientboundSyncCooldowns;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.api.spells.SpellType;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class PlayerCooldowns {
    public static final String SPELL_ID = "sid";
    public static final String SPELL_COOLDOWN = "scd";
    public static final String COOLDOWN_REMAINING = "cdr";

    //spell type and for how many more ticks it will be on cooldown
    private final Map<SpellType, CooldownInstance> spellCooldowns;

    //This is used to deal with the client and server tick's not in sync so
    // the client has a little grace period so it's remove doesn't happen before the server's
    private int tickBuffer = 0;

    public PlayerCooldowns() {
        this(Maps.newEnumMap(SpellType.class));
    }

    public PlayerCooldowns(Map<SpellType, CooldownInstance> spellCooldowns) {
        this.spellCooldowns = spellCooldowns;
    }

    public void setTickBuffer(int tickBuffer) {
        this.tickBuffer = tickBuffer;
    }

    public void tick(int actualTicks) {
        spellCooldowns.forEach((spell, cooldown) -> {
            if (decrementCooldown(cooldown, actualTicks))
                spellCooldowns.remove(spell);
        });
    }

    public boolean hasCooldownsActive() {
        return !spellCooldowns.isEmpty();
    }

    public Map<SpellType, CooldownInstance> getSpellCooldowns() {
        return spellCooldowns;
    }

    public void addCooldown(SpellType spell, int durationTicks) {
        spellCooldowns.put(spell, new CooldownInstance(durationTicks));
    }

    public void addCooldown(SpellType spell, int durationTicks, int remaining) {
        //irons_spellbooks.LOGGER.debug("addCooldown: {} {} {}", spell, durationTicks, remaining);
        spellCooldowns.put(spell, new CooldownInstance(durationTicks, remaining));
    }

    public boolean isOnCooldown(SpellType spell) {
        return spellCooldowns.containsKey(spell);
    }

    public float getCooldownPercent(SpellType spell) {
        return spellCooldowns.getOrDefault(spell, new CooldownInstance(0)).getCooldownPercent();
    }

    private boolean decrementCooldown(CooldownInstance c, int amount) {
        c.decrementBy(amount);
        return c.getCooldownRemaining() <= tickBuffer;
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientboundSyncCooldowns(this.spellCooldowns), serverPlayer);
    }

    public void saveNBTData(ListTag listTag) {
        spellCooldowns.forEach((spell, cooldown) -> {
            if (cooldown.getCooldownRemaining() > 0) {
                CompoundTag ct = new CompoundTag();
                ct.putInt(SPELL_ID, spell.getValue());
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
                SpellType spellType = SpellType.values()[t.getInt(SPELL_ID)];
                int spellCooldown = t.getInt(SPELL_COOLDOWN);
                int cooldownRemaining = t.getInt(COOLDOWN_REMAINING);
                spellCooldowns.put(spellType, new CooldownInstance(spellCooldown, cooldownRemaining));
            });
        }
    }
}
