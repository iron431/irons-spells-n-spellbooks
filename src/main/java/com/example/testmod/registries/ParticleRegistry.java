package com.example.testmod.registries;

import com.example.testmod.TestMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    /*
    To Create Particle:
    - textures + json
    - particle class
    - register it here
    - add it to particle helper
    - register it in client setup
     */

    public static final RegistryObject<SimpleParticleType> BLOOD_PARTICLE = PARTICLE_TYPES.register("blood", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BLOOD_GROUND_PARTICLE = PARTICLE_TYPES.register("blood_ground", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SNOWFLAKE_PARTICLE = PARTICLE_TYPES.register("snowflake", () -> new SimpleParticleType(true));
}
