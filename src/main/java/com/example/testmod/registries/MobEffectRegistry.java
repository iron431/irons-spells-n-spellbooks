package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.effect.*;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.caelus.api.CaelusApi;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registry.MOB_EFFECT_REGISTRY, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final RegistryObject<MobEffect> BLOOD_SLASHED = MOB_EFFECT_DEFERRED_REGISTER.register("blood_slashed", () -> new BloodSlashed(MobEffectCategory.HARMFUL, 0xff4800));
    public static final RegistryObject<MobEffect> ANGEL_WINGS = MOB_EFFECT_DEFERRED_REGISTER.register("angel_wings", () -> new AngelWingsEffect(MobEffectCategory.BENEFICIAL, 0xbea925).addAttributeModifier(CaelusApi.getInstance().getFlightAttribute(), "748D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 1, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> EVASION = MOB_EFFECT_DEFERRED_REGISTER.register("evasion", () -> new EvasionEffect(MobEffectCategory.BENEFICIAL, 0xff4800));
    public static final RegistryObject<MobEffect> ABYSSAL_SHROUD = MOB_EFFECT_DEFERRED_REGISTER.register("abyssal_shroud", () -> new AbyssalShroudEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MobEffect> HEARTSTOP = MOB_EFFECT_DEFERRED_REGISTER.register("heartstop", () -> new HeartstopEffect(MobEffectCategory.BENEFICIAL, 4393481));
    public static final RegistryObject<MobEffect> SUMMON_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("summon_timer", () -> new SummonTimer(MobEffectCategory.NEUTRAL, 0xbea925));
    public static final RegistryObject<MobEffect> VEX_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("vex_timer", () -> new SummonTimer(MobEffectCategory.NEUTRAL, 0xbea925));
    public static final RegistryObject<MobEffect> RAISE_DEAD_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("raise_dead_timer", () -> new SummonTimer(MobEffectCategory.NEUTRAL, 0xbea925));
    public static final RegistryObject<MobEffect> SUMMON_HORSE_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("summon_horse_timer", () -> new SummonTimer(MobEffectCategory.NEUTRAL, 0xbea925));
}

