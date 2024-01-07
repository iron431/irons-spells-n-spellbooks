package io.redspace.ironsspellbooks.particle;

import net.minecraft.core.particles.ParticleOptions;
import org.joml.Vector3f;

import java.util.Optional;

public interface IShockwaveParticleOptions extends ParticleOptions {

    float getScale();

    boolean isFullbright();

    Optional<ParticleOptions> trailParticle();

    String trailParticleRaw();

    Vector3f color();
}
