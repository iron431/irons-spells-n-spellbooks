package io.redspace.ironsspellbooks.api.events;


import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;


public class InscribeSpellEvent extends PlayerEvent {
    private final SpellData spellData;

    public InscribeSpellEvent(Player player, SpellData spellData) {
        super(player);
        this.spellData = spellData;
    }

    public SpellData getSpellData() {
        return this.spellData;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
