package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DragonFireParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean mirrored;

    public DragonFireParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {

        super(level, xCoord, yCoord, zCoord, xd, yd, zd);


        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale(this.random.nextFloat() * 1.75f + 1f);
        this.lifetime = 10 + (int) (Math.random() * 10);
        sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.gravity = -0.015F;
        this.mirrored = this.random.nextBoolean();
    }

    @Override
    public void tick() {
        super.tick();
        this.xd += this.random.nextFloat() / 500.0F * (float) (this.random.nextBoolean() ? 1 : -1);
        this.yd += this.random.nextFloat() / 100.0F;
        this.zd += this.random.nextFloat() / 500.0F * (float) (this.random.nextBoolean() ? 1 : -1);
        this.setSpriteFromAge(this.sprites);
//        if (age > lifetime * .5f && this.random.nextFloat() <= .35f) {
//            this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
//        }
    }

    @Override
    protected float getU0() {
        return mirrored ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return mirrored ? super.getU0() : super.getU1();
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
            return new DragonFireParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }

    @Override
    public int getLightColor(float p_107564_) {
        return 240;
    }
//
//    @Override
//    public float getQuadSize(float p_107567_) {
//        float f = ((float) this.age + p_107567_) / (float) this.lifetime;
//        f = 1.0F - f;
//        f *= f;
//        f = 1.0F - f;
//        return this.quadSize * f;
//    }
}
