package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


/**
 * InscribeSpellEvent is fired whenever a {@link Player} inscribes a spell into a spellbook.<br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * If this event is canceled, the spell is not inscribed.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class InscribeSpellEvent extends PlayerEvent implements ICancellableEvent {
    private final SpellData spellData;

    public InscribeSpellEvent(Player player, SpellData spellData) {
        super(player);
        this.spellData = spellData;
    }

    public SpellData getSpellData() {
        return this.spellData;
    }
}
