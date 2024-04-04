package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * SpellOnCastEvent is fired whenever a spell is triggered. For players, mana is already consumed. <br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * To prevent spellcast, use {@link SpellPreCastEvent}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class SpellOnCastEvent extends PlayerEvent {
    private final String spellId;
    private final SchoolType schoolType;
    private final CastSource castSource;
    private final int spellLevel;
    private int newSpellLevel;

    public SpellOnCastEvent(Player player, String spellId, int spellLevel, SchoolType schoolType, CastSource castSource) {
        super(player);
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.schoolType = schoolType;
        this.castSource = castSource;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public String getSpellId() {
        return this.spellId;
    }

    public SchoolType getSchoolType() {
        return this.schoolType;
    }

    public int getOriginalSpellLevel() {
        return this.spellLevel;
    }

    public int getSpellLevel() {
        return this.newSpellLevel;
    }

    public void setSpellLevel(int spellLevel) {
        this.newSpellLevel = spellLevel;
    }

    public CastSource getCastSource() {
        return this.castSource;
    }
}
