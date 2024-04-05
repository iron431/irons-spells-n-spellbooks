package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * This event is no longer in use. See {@link SpellPreCastEvent} and {@link SpellOnCastEvent}
 **/
@Deprecated(forRemoval = true)
public class SpellCastEvent extends SpellPreCastEvent {
    public SpellCastEvent(Player player, String spellId, int spellLevel, SchoolType schoolType, CastSource castSource) {
        super(player, spellId, spellLevel, schoolType, castSource);
    }
}
