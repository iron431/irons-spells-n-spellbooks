package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.effect.AngelWingsEffect;
import com.example.testmod.effect.BloodSlashed;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registry.MOB_EFFECT_REGISTRY, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final RegistryObject<MobEffect> BLOOD_SLASHED = MOB_EFFECT_DEFERRED_REGISTER.register("blood_slashed", () -> new BloodSlashed(MobEffectCategory.HARMFUL, 0xff4800));
    public static final RegistryObject<MobEffect> ANGEL_WINGS = MOB_EFFECT_DEFERRED_REGISTER.register("angel_wings", () -> new AngelWingsEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
}

