package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AcidBubbleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public AcidBubbleParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {

        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        //this.friction = 1.0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= 1f;
        this.scale(3f);
        this.lifetime = 10 + (int) (Math.random() * 5);
        sprites = spriteSet;
        this.gravity = 0.35F;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
            return new AcidBubbleParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
