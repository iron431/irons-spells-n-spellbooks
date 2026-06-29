package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FallingSparkleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float rotSpeed;

    public FallingSparkleParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale((this.random.nextFloat() + 1.5f) * .35f);
        this.lifetime = 60 + (int) (Math.random() * 20);
        sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.gravity = -0.02F;
        this.rotSpeed = ((float) Math.random() - 0.5F) * 0.1F;
        this.friction = .90f - (float) Math.random() * .08f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.oRoll = this.roll;
            if (!this.onGround) {
                this.roll = this.roll + (float) Math.PI * this.rotSpeed * 2.0F;
            }

            move(xd, yd, zd);
            this.yd += this.gravity;
            this.xd *= friction;
            this.yd *= (friction + 1) / 2;
            this.zd *= friction;
            this.setSpriteFromAge(this.sprites);
//            this.scale(1.023f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new FallingSparkleParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }

    @Override
    public int getLightColor(float p_107564_) {
        return LightTexture.FULL_BRIGHT;
    }
}
