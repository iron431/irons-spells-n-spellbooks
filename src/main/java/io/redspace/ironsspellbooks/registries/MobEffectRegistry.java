package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.*;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.caelus.api.CaelusApi;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    //public static final RegistryObject<MobEffect> BLOOD_SLASHED = MOB_EFFECT_DEFERRED_REGISTER.register("blood_slashed", () -> new BloodSlashed(MobEffectCategory.HARMFUL, 0xff4800));
    public static final RegistryObject<MobEffect> ANGEL_WINGS = MOB_EFFECT_DEFERRED_REGISTER.register("angel_wings", () -> new AngelWingsEffect(MobEffectCategory.BENEFICIAL, 0xbea925).addAttributeModifier(CaelusApi.getInstance().getFlightAttribute(), "748D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 1, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> EVASION = MOB_EFFECT_DEFERRED_REGISTER.register("evasion", () -> new EvasionEffect(MobEffectCategory.BENEFICIAL, 0x9f0be3));
    public static final RegistryObject<MobEffect> HEARTSTOP = MOB_EFFECT_DEFERRED_REGISTER.register("heartstop", () -> new HeartstopEffect(MobEffectCategory.BENEFICIAL, 4393481));
    //public static final RegistryObject<MobEffect> SUMMON_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("summon_timer", () -> new SummonTimer(MobEffectCategory.NEUTRAL, 0xbea925));
    public static final RegistryObject<SummonTimer> VEX_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("vex_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final RegistryObject<SummonTimer> POLAR_BEAR_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("polar_bear_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final RegistryObject<SummonTimer> RAISE_DEAD_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("raise_dead_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final RegistryObject<SummonTimer> SUMMON_HORSE_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("summon_horse_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final RegistryObject<MobEffect> ABYSSAL_SHROUD = MOB_EFFECT_DEFERRED_REGISTER.register("abyssal_shroud", () -> new AbyssalShroudEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MobEffect> ASCENSION = MOB_EFFECT_DEFERRED_REGISTER.register("ascension", () -> new AscensionEffect(MobEffectCategory.BENEFICIAL, 0xbea925).addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), "467D7064-6A45-4F59-8ABE-C2C93A6DD7A9", -.85f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> CHARGED = MOB_EFFECT_DEFERRED_REGISTER.register("charged", () -> new ChargeEffect(MobEffectCategory.BENEFICIAL, 3311322).addAttributeModifier(Attributes.ATTACK_DAMAGE, "87733c95-909c-4fc3-9780-e35a89565666", ChargeEffect.ATTACK_DAMAGE_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.MOVEMENT_SPEED, "87733c95-909c-4fc3-9780-e35a89565666", ChargeEffect.SPEED_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), "87733c95-909c-4fc3-9780-e35a89565666", ChargeEffect.SPELL_POWER_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> TRUE_INVISIBILITY = MOB_EFFECT_DEFERRED_REGISTER.register("true_invisibility", () -> new TrueInvisibilityEffect(MobEffectCategory.BENEFICIAL, 8356754));
    public static final RegistryObject<MobEffect> FORTIFY = MOB_EFFECT_DEFERRED_REGISTER.register("fortify", () -> new FortifyEffect(MobEffectCategory.BENEFICIAL, 16239960));
    public static final RegistryObject<MobEffect> REND = MOB_EFFECT_DEFERRED_REGISTER.register("rend", () -> new RendEffect(MobEffectCategory.HARMFUL, 4800826).addAttributeModifier(Attributes.ARMOR, "01efe86c-d40e-4199-b635-1782f9fcbe03", RendEffect.ARMOR_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> SPIDER_ASPECT = MOB_EFFECT_DEFERRED_REGISTER.register("spider_aspect", () -> new SpiderAspectEffect(MobEffectCategory.BENEFICIAL, 4800826));
    public static final RegistryObject<MobEffect> BLIGHT = MOB_EFFECT_DEFERRED_REGISTER.register("blight", () -> new BlightEffect(MobEffectCategory.HARMFUL, 0xdfff2b));
    public static final RegistryObject<MobEffect> GUIDING_BOLT = MOB_EFFECT_DEFERRED_REGISTER.register("guided", () -> new GuidingBoltEffect(MobEffectCategory.HARMFUL, 16239960));
    public static final RegistryObject<MobEffect> AIRBORNE = MOB_EFFECT_DEFERRED_REGISTER.register("airborne", () -> new AirborneEffect(MobEffectCategory.HARMFUL, 0xFFFFFF));
    public static final RegistryObject<MobEffect> VIGOR = MOB_EFFECT_DEFERRED_REGISTER.register("vigor", () -> new MagicMobEffect(MobEffectCategory.BENEFICIAL, 0x850d0d).addAttributeModifier(Attributes.MAX_HEALTH, "c2b7228b-3ded-412e-940b-8f9f1e2cf882", 2, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> INSTANT_MANA = MOB_EFFECT_DEFERRED_REGISTER.register("instant_mana", () -> new InstantManaEffect(MobEffectCategory.BENEFICIAL, 0x00b7ec) );
    public static final RegistryObject<MobEffect> OAKSKIN = MOB_EFFECT_DEFERRED_REGISTER.register("oakskin", () -> new OakskinEffect(MobEffectCategory.BENEFICIAL, 0xffef95) );
    public static final RegistryObject<MobEffect> PLANAR_SIGHT = MOB_EFFECT_DEFERRED_REGISTER.register("planar_sight", () -> new PlanarSightEffect(MobEffectCategory.BENEFICIAL, 0x6c42f5));
    public static final RegistryObject<MobEffect> ANTIGRAVITY = MOB_EFFECT_DEFERRED_REGISTER.register("antigravity", () -> new MagicMobEffect(MobEffectCategory.NEUTRAL, 0x6c42f5).addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), "d2b7228b-3ded-412e-940b-8f9f1e2cf882", -1.02, AttributeModifier.Operation.MULTIPLY_BASE));
    public static final RegistryObject<MobEffect> HASTENED = MOB_EFFECT_DEFERRED_REGISTER.register("hastened", () -> new MagicMobEffect(MobEffectCategory.BENEFICIAL, 0xD9C043).addAttributeModifier(Attributes.MOVEMENT_SPEED, "e2b7228b-3ded-412e-940b-8f9f1e2cf882", .10, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.ATTACK_SPEED, "e2b7228b-3ded-412e-940b-8f9f1e2cf882", .10, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(AttributeRegistry.MANA_REGEN.get(), "e2b7228b-3ded-412e-940b-8f9f1e2cf882", .10, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(AttributeRegistry.CAST_TIME_REDUCTION.get(), "e2b7228b-3ded-412e-940b-8f9f1e2cf882", .10, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> SLOWED = MOB_EFFECT_DEFERRED_REGISTER.register("slowed", () -> new MagicMobEffect(MobEffectCategory.HARMFUL, 0x5A6C81).addAttributeModifier(Attributes.MOVEMENT_SPEED, "f2b7228b-3ded-412e-940b-8f9f1e2cf882", -.10, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(Attributes.ATTACK_SPEED, "f2b7228b-3ded-412e-940b-8f9f1e2cf882", -.10, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(AttributeRegistry.MANA_REGEN.get(), "f2b7228b-3ded-412e-940b-8f9f1e2cf882", -.10, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(AttributeRegistry.CAST_TIME_REDUCTION.get(), "f2b7228b-3ded-412e-940b-8f9f1e2cf882", -.10, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> CHILLED = MOB_EFFECT_DEFERRED_REGISTER.register("chilled", () -> new MagicMobEffect(MobEffectCategory.HARMFUL, 0xd0f9ff).addAttributeModifier(Attributes.MOVEMENT_SPEED, "d2b7229b-3ded-412e-940b-8f9f1e2cf882", -.20, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> BURNING_DASH = MOB_EFFECT_DEFERRED_REGISTER.register("burning_dash", () -> new BurningDashEffect(MobEffectCategory.BENEFICIAL, 0xd0f9ff));
    public static final RegistryObject<MobEffect> GLUTTONY = MOB_EFFECT_DEFERRED_REGISTER.register("gluttony", () -> new GluttonyEffect(MobEffectCategory.BENEFICIAL, 0xd0f9ff));
    public static final RegistryObject<MobEffect> ECHOING_STRIKES = MOB_EFFECT_DEFERRED_REGISTER.register("echoing_strikes", () -> new EchoingStrikesEffect(MobEffectCategory.BENEFICIAL, 0x9f0be3));
    public static final RegistryObject<MobEffect> THUNDERSTORM = MOB_EFFECT_DEFERRED_REGISTER.register("thunderstorm", () -> new ThunderstormEffect(MobEffectCategory.BENEFICIAL, 0x9f0be3));
    //public static final RegistryObject<MobEffect> ENCHANTED_WARD = MOB_EFFECT_DEFERRED_REGISTER.register("enchanted_ward", () -> new EnchantedWardEffect(MobEffectCategory.HARMFUL, 3311322));
}