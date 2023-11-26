package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

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
public class ChangeManaEvent extends PlayerEvent {
    private final MagicData magicData;
    private final float oldMana;
    private float newMana;

    public ChangeManaEvent(Player player, MagicData magicData, float oldMana, float newMana) {
        super(player);
        this.magicData = magicData;
        this.oldMana = oldMana;
        this.newMana = newMana;
    }

    @Override
    public boolean isCancelable() {
        return true;
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
