package io.redspace.ironsspellbooks.api.events;

import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class SpellCooldownAddedEvent extends Event {
    private final AbstractSpellSkill spell;
    private int effectiveCooldown;
    private final Player entity;
    private CastSource castSource;

    public SpellCooldownAddedEvent(int effectiveCooldown, AbstractSpellSkill spell, Player entity, CastSource castSource) {
        this.spell = spell;
        this.effectiveCooldown = effectiveCooldown;
        this.entity = entity;
        this.castSource = castSource;
    }

    public AbstractSpellSkill getSpell() {
        return spell;
    }

    public int getEffectiveCooldown() {
        return effectiveCooldown;
    }

    private void setEffectiveCooldown(int effectiveCooldown) {
        this.effectiveCooldown = effectiveCooldown;
    }

    public CastSource getCastSource() {
        return castSource;
    }

    public Player getEntity() {
        return entity;
    }

    public static class Pre extends SpellCooldownAddedEvent implements ICancellableEvent {
        public Pre(int effectiveCooldown, AbstractSpellSkill spell, Player entity, CastSource castSource) {
            super(effectiveCooldown, spell, entity, castSource);
        }

        public void setEffectiveCooldown(int newCooldown) {
            super.setEffectiveCooldown(newCooldown);
        }
    }

    public static class Post extends SpellCooldownAddedEvent {
        public Post(int effectiveCooldown, AbstractSpellSkill spell, Player entity, CastSource castSource) {
            super(effectiveCooldown, spell, entity, castSource);
        }
    }
}
