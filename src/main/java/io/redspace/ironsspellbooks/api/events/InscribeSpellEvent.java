package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

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
public class InscribeSpellEvent extends PlayerEvent {
    private final SpellData spellData;

    public InscribeSpellEvent(Player player, SpellData spellData)
    {
        super(player);
        this.spellData = spellData;
    }

    @Override
    public boolean isCancelable() { return true; }

    public SpellData getSpellData() {
        return this.spellData;
    }
}
