package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class TintedSplashParticle extends WaterDropParticle {
    private final double surfaceY;

    protected TintedSplashParticle(ClientLevel level, double x, double y, double z, Vector3f tint) {
        super(level, x, y, z);
        this.gravity = 0.04F;
        this.surfaceY = y;
        this.setColor(tint.x(), tint.y(), tint.z());
    }

    @Override
    public void tick() {
        super.tick();
        // the cauldron's liquid is not a fluid block, so vanilla's fluid check never removes the drop
        if (this.yd < 0 && this.y < surfaceY) {
            this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<CauldronParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(
                @NotNull CauldronParticleOptions options,
                @NotNull ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            TintedSplashParticle particle = new TintedSplashParticle(level, x, y, z, options.tint(level));
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
