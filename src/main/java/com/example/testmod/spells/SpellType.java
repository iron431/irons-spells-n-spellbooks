package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.spells.cold.ConeOfColdSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.evocation.MagicMissileSpell;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum SpellType {
    NONE_SPELL(0),
    FIREBALL_SPELL(1),
    BURNING_DASH_SPELL(2),
    TEST_SPELL(3),
    TELEPORT_SPELL(4),
    MAGIC_MISSILE_SPELL(5),
    ELECTROCUTE_SPELL(6),
    CONE_OF_COLD_SPELL(7);

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
            default -> {
                return new NoneSpell(0);
            }
        }
    }

    public TranslatableComponent getDisplayName() {
        return new TranslatableComponent("spell." + TestMod.MODID + "." + this.toString().toLowerCase().replace("_spell", ""));
    }
}