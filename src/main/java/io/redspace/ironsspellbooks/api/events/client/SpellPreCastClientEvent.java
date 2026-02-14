package io.redspace.ironsspellbooks.api.events.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Similarly to {@link io.redspace.ironsspellbooks.api.events.SpellPreCastEvent},
 * this event is fired whenever a {@link Player} is about to cast a spell,
 * but before any packet is sent to the server.
 * This event is called only on the physical client.
 *
 * <p>This event is {@link ICancellableEvent cancellable}.
 * Cancelling this event prevents the spell cast packet from being sent.</p>
 * <br>
 * This event does not have a result.<br>
 *
 * <p>This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.</p>
 **/
public class SpellPreCastClientEvent extends PlayerEvent implements ICancellableEvent {
    private final @NotNull SpellPreCastClientEvent.CastTriggerType triggerType;

    public SpellPreCastClientEvent(@NotNull LocalPlayer player, @NotNull SpellPreCastClientEvent.CastTriggerType triggerType) {
        super(player);
        this.triggerType = triggerType;
    }

    public enum CastTriggerType {
        CAST_SELECTED_SPELL,
        QUICK_CAST;

        public boolean isSpellBookCast() {
            return this == CAST_SELECTED_SPELL || this == QUICK_CAST;
        }
    }

    public @NotNull SpellPreCastClientEvent.CastTriggerType getTriggerType() {
        return this.triggerType;
    }
}
