package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.spells.blood.BloodSlashSpell;
import com.example.testmod.spells.evocation.FirecrackerSpell;
import com.example.testmod.spells.evocation.SummonHorseSpell;
import com.example.testmod.spells.ice.ConeOfColdSpell;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.evocation.SummonVexSpell;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireBreathSpell;
import com.example.testmod.spells.fire.FireballSpell;
import com.example.testmod.spells.fire.FireboltSpell;
import com.example.testmod.spells.holy.HealSpell;
import com.example.testmod.spells.ice.IcicleSpell;
import com.example.testmod.spells.lightning.ElectrocuteSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public enum SpellType {
    /*
    When adding spell, add:
        Spell Type
        SpellType.Cast Type
        SpellType.School Type
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
    CONE_OF_COLD_SPELL(6),
    HEAL_SPELL(7),
    BLOOD_SLASH_SPELL(8),
    SUMMON_VEX_SPELL(9),
    FIREBOLT_SPELL(10),
    FIRE_BREATH_SPELL(11),
    ICICLE_SPELL(12),
    FIRECRACKER_SPELL(13),
    SUMMON_HORSE_SPELL(14);

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
            case HEAL_SPELL -> {
                return new HealSpell(level);
            }
            case BLOOD_SLASH_SPELL -> {
                return new BloodSlashSpell(level);
            }
            case SUMMON_VEX_SPELL -> {
                return new SummonVexSpell(level);
            }
            case FIREBOLT_SPELL -> {
                return new FireboltSpell(level);
            }
            case FIRE_BREATH_SPELL -> {
                return new FireBreathSpell(level);
            }
            case ICICLE_SPELL -> {
                return new IcicleSpell(level);
            }
            case FIRECRACKER_SPELL -> {
                return new FirecrackerSpell(level);
            }
            case SUMMON_HORSE_SPELL -> {
                return new SummonHorseSpell(level);
            }
            default -> {
                return new NoneSpell(0);
            }
        }
    }

    public CastType getCastType() {
        return switch (this) {
            case FIREBALL_SPELL -> CastType.LONG;
            case ELECTROCUTE_SPELL, CONE_OF_COLD_SPELL, FIRE_BREATH_SPELL -> CastType.CONTINUOUS;
            default -> CastType.INSTANT;
        };
    }

    public SchoolType getSchoolType() {
        //TODO: not this
        return switch (this) {
            case FIREBALL_SPELL, BURNING_DASH_SPELL, FIREBOLT_SPELL, FIRE_BREATH_SPELL -> SchoolType.FIRE;
            case CONE_OF_COLD_SPELL, ICICLE_SPELL -> SchoolType.ICE;
            case ELECTROCUTE_SPELL -> SchoolType.LIGHTNING;
            case HEAL_SPELL -> SchoolType.HOLY;
            case TELEPORT_SPELL, MAGIC_MISSILE_SPELL -> SchoolType.ENDER;
            case BLOOD_SLASH_SPELL -> SchoolType.BLOOD;
            case NONE_SPELL, SUMMON_VEX_SPELL, FIRECRACKER_SPELL, SUMMON_HORSE_SPELL -> SchoolType.EVOCATION;
        };
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("spell." + TestMod.MODID + "." + this.getId());
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(TestMod.MODID, "textures/gui/spell_icons/" + this.getId() + ".png");
    }

    private String getId() {
        return this.toString().toLowerCase().replace("_spell", "");
    }
}