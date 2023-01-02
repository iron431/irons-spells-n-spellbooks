package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.spells.cold.ConeOfColdSpell;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import com.example.testmod.spells.lightning.ElectrocuteSpell;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public enum SpellType {
    /*
    When adding spell, add:
        Spell Type
        Cast Type
        School Type
        SpellType.getForType
        Translation
        Icon
     */
    NONE_SPELL(0),
    FIREBALL_SPELL(1),
    BURNING_DASH_SPELL(2),
    TELEPORT_SPELL(3),
    MAGIC_MISSILE_SPELL(4),
    ELECTROCUTE_SPELL(5),
    CONE_OF_COLD_SPELL(6);

    private final int value;

    SpellType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public AbstractSpell getSpellForType(int level) {
        switch (this) {
            case BURNING_DASH_SPELL -> {
                return new BurningDashSpell(level);
            }
            case FIREBALL_SPELL -> {
                return new FireballSpell(level);
            }
            case TELEPORT_SPELL -> {
                return new TeleportSpell(level);
            }
            case MAGIC_MISSILE_SPELL -> {
                return new MagicMissileSpell(level);
            }
            case CONE_OF_COLD_SPELL -> {
                return new ConeOfColdSpell(level);
            }
            case ELECTROCUTE_SPELL -> {
                return new ElectrocuteSpell(level);
            }
            default -> {
                return new NoneSpell(0);
            }
        }
    }

    public TranslatableComponent getDisplayName() {
        return new TranslatableComponent("spell." + TestMod.MODID + "." + this.getId());
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(TestMod.MODID, "textures/gui/spell_icons/" + this.getId() + ".png");
    }

    private String getId() {
        return this.toString().toLowerCase().replace("_spell", "");
    }

    public CastType getCastType() {
        return switch (this) {
            case FIREBALL_SPELL, TELEPORT_SPELL -> CastType.LONG;
            case ELECTROCUTE_SPELL, CONE_OF_COLD_SPELL -> CastType.CONTINUOUS;
            default -> CastType.INSTANT;
        };
    }

    public SchoolType getSchoolType() {
        //
        //   Don't put default, just add the new spell
        //
        return switch (this) {
            case FIREBALL_SPELL, BURNING_DASH_SPELL -> SchoolType.FIRE;
            case CONE_OF_COLD_SPELL -> SchoolType.ICE;
            case ELECTROCUTE_SPELL -> SchoolType.LIGHTNING;
            //case NONE_SPELL -> SchoolType.HOLY;
            case TELEPORT_SPELL, MAGIC_MISSILE_SPELL -> SchoolType.ENDER;
            //case NONE_SPELL -> SchoolType.BLOOD;
            case NONE_SPELL -> SchoolType.EVOCATION;
        };
    }
}