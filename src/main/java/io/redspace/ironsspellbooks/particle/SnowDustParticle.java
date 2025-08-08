package io.redspace.ironsspellbooks.particle;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SnowDustParticle extends SnowflakeParticle {
    float maxSize, minSize;

    public SnowDustParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, spriteSet, xd, yd, zd);
        this.minSize = this.quadSize;
        this.maxSize = Utils.random.nextFloat() * .4f + .7f;
    }

    @Override
    public void tick() {
        super.tick();
        this.quadSize = Mth.clampedLerp(minSize, maxSize, this.age / (float) this.lifetime);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            SnowDustParticle snowflakeparticle = new SnowDustParticle(pLevel, pX, pY, pZ, this.sprites, pXSpeed, pYSpeed, pZSpeed);
            snowflakeparticle.setColor(0.923F, 0.964F, 0.999F);
            snowflakeparticle.setAlpha(.5f);
            return snowflakeparticle;
        }
    }
}
