package io.redspace.ironsspellbooks.api.events;


import io.redspace.skillcasting.data.skill.SkillData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;

/**
 * Called on the client and server at different points:
 * <br>  <code>Client</code>: when a scroll is placed before being inscribed
 * <br>  <code>Server</code>: when the request to inscribe is received
 * <br>This event is cancellable. If canceled, the failure message MUST BE SET!
 * <br>The failure message is displayed on the client, and prevents the player from using the inscribe button
 */
public class InscribeSpellEvent extends PlayerEvent implements ICancellableEvent {
    private Component failureMessage = null;
    private final SkillData scrollData;
    private final ItemStack spellbookStack;
    private final ItemStack scrollStack;

    public InscribeSpellEvent(Player player, ItemStack spellbookStack, ItemStack scrollStack, SkillData scrollData) {
        super(player);
        this.scrollData = scrollData;
        this.spellbookStack = spellbookStack;
        this.scrollStack = scrollStack;
    }

    public void setFailureMessage(Component failureMessage) {
        this.failureMessage = failureMessage;
        setCanceled(true);
    }

    public @Nullable Component getFailureMessage() {
        return failureMessage;
    }

    public ItemStack getScrollStack() {
        return scrollStack;
    }

    public ItemStack getSpellbookStack() {
        return spellbookStack;
    }

    public SkillData getScrollData() {
        return this.scrollData;
    }
}
