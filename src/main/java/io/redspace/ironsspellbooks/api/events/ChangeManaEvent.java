package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.capabilities.magic.ActualMagicData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;



public class ChangeManaEvent extends PlayerEvent implements ICancellableEvent {
    private final ActualMagicData magicData;
    private final float oldMana;
    private float newMana;

    public ChangeManaEvent(Player player, ActualMagicData magicData, float oldMana, float newMana) {
        super(player);
        this.magicData = magicData;
        this.oldMana = oldMana;
        this.newMana = newMana;
    }

    public ActualMagicData getMagicData() {
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
