package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * SpellOnCastEvent is fired whenever a spell is triggered.<br>
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
    private final int manaCost;
    private int newManaCost;

    public SpellOnCastEvent(Player player, String spellId, int spellLevel, int manaCost, SchoolType schoolType, CastSource castSource) {
        super(player);
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        this.schoolType = schoolType;
        this.castSource = castSource;
        this.manaCost = manaCost;
        this.newManaCost = this.manaCost;
        this.newSpellLevel = this.spellLevel;
    }

    @Override
    public boolean isCancelable() {
        return false;
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

    public int getOriginalManaCost() {
        return this.manaCost;
    }

    public int getManaCost() {
        return newManaCost;
    }

    public void setManaCost(int mana) {
        this.newManaCost = mana;
    }

    public CastSource getCastSource() {
        return this.castSource;
    }
}
