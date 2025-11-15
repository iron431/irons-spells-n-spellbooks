package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.*;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final RegistryObject<MobEffect> ANGEL_WINGS = MOB_EFFECT_DEFERRED_REGISTER.register("angel_wings", () -> new AngelWingsEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final RegistryObject<MobEffect> EVASION = MOB_EFFECT_DEFERRED_REGISTER.register("evasion", () -> new EvasionEffect(MobEffectCategory.BENEFICIAL, 0xf17bf4));
    public static final RegistryObject<MobEffect> HEARTSTOP = MOB_EFFECT_DEFERRED_REGISTER.register("heartstop", () -> new HeartstopEffect(MobEffectCategory.BENEFICIAL, 4393481));
    public static final RegistryObject<MobEffect> ABYSSAL_SHROUD = MOB_EFFECT_DEFERRED_REGISTER.register("abyssal_shroud", () -> new AbyssalShroudEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MobEffect> ASCENSION = MOB_EFFECT_DEFERRED_REGISTER.register("ascension", () -> new AscensionEffect(MobEffectCategory.BENEFICIAL, 0xbea925)
            .addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), IronsSpellbooks.id("mobeffect_ascension"), -.85f, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> CHARGED = MOB_EFFECT_DEFERRED_REGISTER.register("charged", () -> new ChargeEffect(MobEffectCategory.BENEFICIAL, 3311322)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, IronsSpellbooks.id("mobeffect_charged"), ChargeEffect.ATTACK_DAMAGE_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, IronsSpellbooks.id("mobeffect_charged"), ChargeEffect.SPEED_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributeRegistry.SPELL_POWER, IronsSpellbooks.id("mobeffect_charged"), ChargeEffect.SPELL_POWER_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> TRUE_INVISIBILITY = MOB_EFFECT_DEFERRED_REGISTER.register("true_invisibility", () -> new TrueInvisibilityEffect(MobEffectCategory.BENEFICIAL, 8356754));
    public static final RegistryObject<MobEffect> FORTIFY = MOB_EFFECT_DEFERRED_REGISTER.register("fortify", () -> new FortifyEffect(MobEffectCategory.BENEFICIAL, 16239960));
    public static final RegistryObject<MobEffect> REND = MOB_EFFECT_DEFERRED_REGISTER.register("rend", () -> new RendEffect(MobEffectCategory.HARMFUL, 4800826)
            .addAttributeModifier(Attributes.ARMOR, IronsSpellbooks.id("mobeffect_rend"), RendEffect.ARMOR_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> SPIDER_ASPECT = MOB_EFFECT_DEFERRED_REGISTER.register("spider_aspect", () -> new SpiderAspectEffect(MobEffectCategory.BENEFICIAL, 4800826));
    public static final RegistryObject<MobEffect> BLIGHT = MOB_EFFECT_DEFERRED_REGISTER.register("blight", () -> new BlightEffect(MobEffectCategory.HARMFUL, 0xdfff2b));
    public static final RegistryObject<MobEffect> GUIDING_BOLT = MOB_EFFECT_DEFERRED_REGISTER.register("guided", () -> new GuidingBoltEffect(MobEffectCategory.HARMFUL, 16239960));
    public static final RegistryObject<MobEffect> AIRBORNE = MOB_EFFECT_DEFERRED_REGISTER.register("airborne", () -> new AirborneEffect(MobEffectCategory.HARMFUL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> VIGOR = MOB_EFFECT_DEFERRED_REGISTER.register("vigor", () -> new MagicMobEffect(MobEffectCategory.BENEFICIAL, 0x850d0d)
            .addAttributeModifier(Attributes.MAX_HEALTH, IronsSpellbooks.id("mobeffect_vigor"), 2, AttributeModifier.Operation.ADDITION).cast());
    public static final RegistryObject<MobEffect> INSTANT_MANA = MOB_EFFECT_DEFERRED_REGISTER.register("instant_mana", () -> new InstantManaEffect(MobEffectCategory.BENEFICIAL, 0x00b7ec));
    public static final RegistryObject<MobEffect> OAKSKIN = MOB_EFFECT_DEFERRED_REGISTER.register("oakskin", () -> new OakskinEffect(MobEffectCategory.BENEFICIAL, 0xffef95)
            //fixme: constant debuff?
            /*.addAttributeModifier(Attributes.MOVEMENT_SPEED, IronsSpellbooks.id("mobeffect_oakskin"), AttributeModifier.Operation.MULTIPLY_TOTAL, level -> -OakskinEffect.SLOWNESS_MAGNITUDE)*/);
    public static final RegistryObject<MobEffect> PLANAR_SIGHT = MOB_EFFECT_DEFERRED_REGISTER.register("planar_sight", () -> new PlanarSightEffect(MobEffectCategory.BENEFICIAL, 0x6c42f5));
    public static final RegistryObject<MobEffect> ANTIGRAVITY = MOB_EFFECT_DEFERRED_REGISTER.register("antigravity", () -> new MagicMobEffect(MobEffectCategory.NEUTRAL, 0x6c42f5)
            .addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), IronsSpellbooks.id("mobeffect_antigravity"), -1.02, AttributeModifier.Operation.MULTIPLY_BASE).cast());
    public static final RegistryObject<MobEffect> HASTENED = MOB_EFFECT_DEFERRED_REGISTER.register("hastened", () -> new MagicMobEffect(MobEffectCategory.BENEFICIAL, 0xD9C043)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, IronsSpellbooks.id("mobeffect_haste"), .10, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, IronsSpellbooks.id("mobeffect_haste"), .10, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributeRegistry.MANA_REGEN, IronsSpellbooks.id("mobeffect_haste"), .10, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributeRegistry.CAST_TIME_REDUCTION, IronsSpellbooks.id("mobeffect_haste"), .10, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> SLOWED = MOB_EFFECT_DEFERRED_REGISTER.register("slowed", () -> new MagicMobEffect(MobEffectCategory.HARMFUL, 0x5A6C81)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, IronsSpellbooks.id("mobeffect_slow"), -.10, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, IronsSpellbooks.id("mobeffect_slow"), -.10, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributeRegistry.MANA_REGEN, IronsSpellbooks.id("mobeffect_slow"), -.10, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributeRegistry.CAST_TIME_REDUCTION, IronsSpellbooks.id("mobeffect_slow"), -.10, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> CHILLED = MOB_EFFECT_DEFERRED_REGISTER.register("chilled", () -> new ChilledEffect(MobEffectCategory.HARMFUL, 0xd0f9ff)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, IronsSpellbooks.id("mobeffect_chilled"), -.20, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> BURNING_DASH = MOB_EFFECT_DEFERRED_REGISTER.register("burning_dash", () -> new BurningDashEffect(MobEffectCategory.BENEFICIAL, 0xd0f9ff));
    public static final RegistryObject<MobEffect> VOLT_STRIKE = MOB_EFFECT_DEFERRED_REGISTER.register("volt_strike", () -> new VoltStrikeEffect(MobEffectCategory.BENEFICIAL, 0xd0088FF));
    public static final RegistryObject<MobEffect> GLUTTONY = MOB_EFFECT_DEFERRED_REGISTER.register("gluttony", () -> new GluttonyEffect(MobEffectCategory.BENEFICIAL, 0xd0f9ff));
    public static final RegistryObject<MobEffect> ECHOING_STRIKES = MOB_EFFECT_DEFERRED_REGISTER.register("echoing_strikes", () -> new EchoingStrikesEffect(MobEffectCategory.BENEFICIAL, 0x9f0be3));
    public static final RegistryObject<MobEffect> THUNDERSTORM = MOB_EFFECT_DEFERRED_REGISTER.register("thunderstorm", () -> new ThunderstormEffect(MobEffectCategory.BENEFICIAL, 0x9f0be3));
    public static final RegistryObject<MobEffect> FROSTBITTEN_STRIKES = MOB_EFFECT_DEFERRED_REGISTER.register("frostbite", () -> new FrostbiteEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> IMMOLATE = MOB_EFFECT_DEFERRED_REGISTER.register("immolate", () -> new ImmolateEffect(MobEffectCategory.HARMFUL, 0xFFAA00));
    public static final RegistryObject<MobEffect> FALL_DAMAGE_IMMUNITY = MOB_EFFECT_DEFERRED_REGISTER.register("fall_damage_immunity", () -> new FallDamageImmunityEffect(MobEffectCategory.BENEFICIAL, 0xDDDDFF));

//    @Deprecated(forRemoval = true)
//    public static final RegistryObject<MobEffect> VEX_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("vex_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
//    @Deprecated(forRemoval = true)
//    public static final RegistryObject<MobEffect> POLAR_BEAR_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("polar_bear_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
//    @Deprecated(forRemoval = true)
//    public static final RegistryObject<MobEffect> SUMMONED_SWORD_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("summon_swords_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
//    @Deprecated(forRemoval = true)
//    public static final RegistryObject<MobEffect> RAISE_DEAD_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("raise_dead_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
//    @Deprecated(forRemoval = true)
//    public static final RegistryObject<MobEffect> SUMMON_HORSE_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("summon_horse_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
}