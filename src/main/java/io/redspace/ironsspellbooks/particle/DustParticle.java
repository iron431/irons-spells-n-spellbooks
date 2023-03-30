package io.redspace.ironsspellbooks.particle;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DustParticle extends TextureSheetParticle {
    protected DustParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd, float r, float g, float b) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.quadSize *= 3F;
        this.lifetime = 30 + this.random.nextInt(20);
        this.hasPhysics = true;
        this.pickSprite(spriteSet);
        this.setColor(r, g, b);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.xd = this.xd * 0.98F;
        this.yd = this.yd * 0.88F;
        this.zd = this.zd * 0.98F;
        fadeOut();
    }

    private void fadeOut() {
        int delay = 20;
        this.alpha = 1 - (((age - delay) / (1 - (delay / (float)lifetime)) + delay) / lifetime);
//        this.alpha = (-(1 / (float) lifetime) * age + 1);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        float r, g, b;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
            this.r = 1;
            this.g = 1;
            this.b = 1;
        }

        public static Provider ProviderBlack(SpriteSet spriteSet) {
            Provider provider = new Provider(spriteSet);
            provider.r = .08f;
            provider.g = .08f;
            provider.b = .12f;
            return provider;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new DustParticle(level, x, y, z, this.sprites, dx, dy, dz, r, g, b);
        }
    }
}