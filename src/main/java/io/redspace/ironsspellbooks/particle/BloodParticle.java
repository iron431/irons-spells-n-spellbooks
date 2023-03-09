package io.redspace.ironsspellbooks.particle;

import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BloodParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public BloodParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {

        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        //this.friction = 1.0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= 1f;
        this.scale(2.5f);
        this.lifetime = 5 + (int) (Math.random() * 10);
        sprites = spriteSet;
        this.gravity = 1.0F;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.onGround) {
            this.level.addParticle(ParticleHelper.BLOOD_GROUND, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            this.remove();
        }
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
            return new BloodParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
