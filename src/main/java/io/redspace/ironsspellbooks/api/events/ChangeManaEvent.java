package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


/**
 * ChangeManaEvent is fired whenever a {@link Player}'s mana is changed via {@link io.redspace.ironsspellbooks.api.magic.MagicData#setMana(float)}.<br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * If this event is canceled, the player's mana does not change.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class ChangeManaEvent extends PlayerEvent implements ICancellableEvent {
    private final MagicData magicData;
    private final float oldMana;
    private float newMana;

    public ChangeManaEvent(Player player, MagicData magicData, float oldMana, float newMana) {
        super(player);
        this.magicData = magicData;
        this.oldMana = oldMana;
        this.newMana = newMana;
    }

    public MagicData getMagicData() {
        return magicData;
    }

    public float getOldMana() {
        return oldMana;
    }

    public float getNewMana() {
        return newMana;
    }

    public void setNewMana(float newMana) {
        this.newMana = newMana;
    }
}
