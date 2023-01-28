package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.config.CommonConfigs;
import com.example.testmod.spells.blood.BloodSlashSpell;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.evocation.FirecrackerSpell;
import com.example.testmod.spells.evocation.ShieldSpell;
import com.example.testmod.spells.evocation.SummonHorseSpell;
import com.example.testmod.spells.evocation.SummonVexSpell;
import com.example.testmod.spells.fire.*;
import com.example.testmod.spells.holy.AngelWingsSpell;
import com.example.testmod.spells.holy.HealSpell;
import com.example.testmod.spells.ice.ConeOfColdSpell;
import com.example.testmod.spells.ice.IcicleSpell;
import com.example.testmod.spells.lightning.ElectrocuteSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.LazyOptional;

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
    SUMMON_HORSE_SPELL(14),
    ANGEL_WING_SPELL(15),
    SHIELD_SPELL(16),
    WALL_OF_FIRE_SPELL(17);

    private final int value;
    private final LazyOptional<Integer> maxLevel;
    private final LazyOptional<Integer> minRarity;
    private final int maxRarity;

    SpellType(final int newValue) {
        value = newValue;
        maxLevel = LazyOptional.of(() -> (CommonConfigs.getByType(this).MAX_LEVEL));
        minRarity = LazyOptional.of(() -> (CommonConfigs.getByType(this).MIN_RARITY.getValue()));
        maxRarity = SpellRarity.LEGENDARY.getValue();

    }

    public int getValue() {
        return value;
    }

    public static SpellType getTypeFromValue(int value) {
        return SpellType.values()[value];
    }

    public CastType getCastType() {
        return switch (this) {
            case FIREBALL_SPELL -> CastType.LONG;
            case ELECTROCUTE_SPELL, CONE_OF_COLD_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL -> CastType.CONTINUOUS;
            default -> CastType.INSTANT;
        };
    }

    //private final float minLevel = 1;

    //private float mappingConstant = (maxRarity - minRarity) / (maxLevel - minLevel);

    public static float getRarityMapped(float levelMin, float levelMax, float rarityMin, float rarityMax, float levelToMap) {
        return rarityMin + ((levelToMap - levelMin) * (rarityMax - rarityMin)) / (levelMax - levelMin);
    }

    public SpellRarity getRarity(int level) {
        //float adjustedRarity = getRarityMapped(minLevel, maxLevel, minRarity, maxRarity, level);
        int maxLevel = this.maxLevel.resolve().get();
        int minRarity = this.minRarity.resolve().get();
        //https://www.desmos.com/calculator/fumipfwdfr
        float rarityPercent = level / (float) maxLevel;
        float scaledRarity = Mth.clamp(lerp(minRarity, maxRarity, rarityPercent * rarityPercent), minRarity, maxRarity);
        return SpellRarity.values()[(int) scaledRarity];
        //return SpellRarity.getRarityFromPercent(adjustedRarity);
    }

    public AbstractSpell getSpellForRarity(SpellRarity rarity) {
        int minRarity = this.minRarity.resolve().get();
        float maxLevel = this.maxLevel.resolve().get();
        var x = rarity.getValue();
        var a = minRarity;
        var b = maxRarity;
        int level = (int) (maxLevel * Math.sqrt((x - a) / (double) (b - a)));

        return getSpellForType(level);
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public SchoolType getSchoolType() {
        if (quickSearch(FIRE_SPELLS, this))
            return SchoolType.FIRE;
        if (quickSearch(ICE_SPELLS, this))
            return SchoolType.ICE;
        if (quickSearch(LIGHTNING_SPELLS, this))
            return SchoolType.LIGHTNING;
        if (quickSearch(HOLY_SPELLS, this))
            return SchoolType.HOLY;
        if (quickSearch(ENDER_SPELLS, this))
            return SchoolType.ENDER;
        if (quickSearch(BLOOD_SPELLS, this))
            return SchoolType.BLOOD;
        //if (quickSearch(EVOCATION_SPELLS, this))
        return SchoolType.EVOCATION;
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
            case ANGEL_WING_SPELL -> {
                return new AngelWingsSpell(level);
            }
            case SHIELD_SPELL -> {
                return new ShieldSpell(level);
            }
            case WALL_OF_FIRE_SPELL -> {
                return new WallOfFireSpell(level);
            }
            default -> {
                return new NoneSpell(0);
            }
        }
    }

    public static SpellType[] getSpellsFromSchool(SchoolType school) {
        if (school.equals(SchoolType.FIRE))
            return FIRE_SPELLS;
        else if (school.equals(SchoolType.ICE))
            return ICE_SPELLS;
        else if (school.equals(SchoolType.LIGHTNING))
            return LIGHTNING_SPELLS;
        else if (school.equals(SchoolType.HOLY))
            return HOLY_SPELLS;
        else if (school.equals(SchoolType.ENDER))
            return ENDER_SPELLS;
        else if (school.equals(SchoolType.BLOOD))
            return BLOOD_SPELLS;
        //else if (school.equals(SchoolType.EVOCATION))
        return EVOCATION_SPELLS;
    }

    private static final SpellType[] FIRE_SPELLS =
            {FIREBALL_SPELL, BURNING_DASH_SPELL, FIREBOLT_SPELL, FIRE_BREATH_SPELL, WALL_OF_FIRE_SPELL};
    private static final SpellType[] ICE_SPELLS =
            {CONE_OF_COLD_SPELL, ICICLE_SPELL};
    private static final SpellType[] LIGHTNING_SPELLS =
            {ELECTROCUTE_SPELL};
    private static final SpellType[] HOLY_SPELLS =
            {HEAL_SPELL, ANGEL_WING_SPELL};
    private static final SpellType[] ENDER_SPELLS =
            {TELEPORT_SPELL, MAGIC_MISSILE_SPELL};
    private static final SpellType[] BLOOD_SPELLS =
            {BLOOD_SLASH_SPELL};
    private static final SpellType[] EVOCATION_SPELLS =
            {SUMMON_VEX_SPELL, FIRECRACKER_SPELL, SUMMON_HORSE_SPELL, SHIELD_SPELL};

    public MutableComponent getDisplayName() {
        return Component.translatable("spell." + TestMod.MODID + "." + this.getId());
    }

    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(TestMod.MODID, "textures/gui/spell_icons/" + this.getId() + ".png");
    }

    public String getId() {
        return this.toString().toLowerCase().replace("_spell", "");
    }

    private boolean quickSearch(SpellType[] array, SpellType query) {
        for (SpellType spellType : array)
            if (spellType.equals(query))
                return true;
        return false;
    }
}