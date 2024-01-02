package io.redspace.ironsspellbooks.particle;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.Optional;

public interface IShockwaveParticleOptions extends ParticleOptions {

    float getScale();

    boolean isFullbright();

    Optional<ParticleOptions> trailParticle();

    String trailParticleRaw();

    Vector3f color();
}
