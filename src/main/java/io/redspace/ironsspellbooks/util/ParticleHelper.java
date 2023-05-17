package io.redspace.ironsspellbooks.util;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;

public class ParticleHelper {
    //public static final ParticleOptions DRAGON_FIRE = ParticleRegistry.DRAGON_FIRE_PARTICLE.get();
    public static final ParticleOptions FIRE = ParticleRegistry.FIRE_PARTICLE.get();
    public static final ParticleOptions BLOOD = ParticleRegistry.BLOOD_PARTICLE.get();
    public static final ParticleOptions WISP = ParticleRegistry.WISP_PARTICLE.get();
    public static final ParticleOptions BLOOD_GROUND = ParticleRegistry.BLOOD_GROUND_PARTICLE.get();
    public static final ParticleOptions SNOWFLAKE = ParticleRegistry.SNOWFLAKE_PARTICLE.get();
    public static final ParticleOptions ELECTRICITY = ParticleRegistry.ELECTRICITY_PARTICLE.get();
    public static final ParticleOptions UNSTABLE_ENDER = ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get();
    public static final ParticleOptions EMBERS = ParticleRegistry.EMBER_PARTICLE.get();
    public static final ParticleOptions SIPHON = ParticleRegistry.SIPHON_PARTICLE.get();
    public static final ParticleOptions ACID = ParticleRegistry.ACID_PARTICLE.get();
    public static final ParticleOptions ACID_BUBBLE = ParticleRegistry.ACID_BUBBLE_PARTICLE.get();
    public static final ParticleOptions FOG = new FogParticleOptions(new Vector3f(1, 1, 1), 1);
    public static final ParticleOptions VOID_TENTACLE_FOG = new FogParticleOptions(new Vector3f(.18f, 0.15f, .22f), 2);
    public static final ParticleOptions ROOT_FOG = new FogParticleOptions(new Vector3f(61/255f, 40/255f, 18/255f), .4f);
    public static final ParticleOptions POISON_CLOUD = new FogParticleOptions(new Vector3f(.08f, 0.64f, .16f), 1f);
}
