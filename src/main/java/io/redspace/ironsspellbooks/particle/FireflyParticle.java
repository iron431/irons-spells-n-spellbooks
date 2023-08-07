package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireflyParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private boolean lit;
    private int litTimer;
    private float wander;
    public FireflyParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {

        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        //this.friction = 1.0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale(2.5f);
        this.lifetime = 20 + (int) (Math.random() * 90);
        this.sprites = spriteSet;

        this.gravity = 0F;
        lit = level.random.nextBoolean();
        wander = level.random.nextFloat() * 2.5f;
        wander *= wander * wander * wander;
        this.setSprite(sprites.get(lit ? 0 : 1, 1));

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        float xj = (this.random.nextFloat() * .001f * wander * (this.random.nextBoolean() ? 1 : -1));
        float yj = (this.random.nextFloat() * .001f * wander * (this.random.nextBoolean() ? 1 : -1)) + .00025f;
        float zj = (this.random.nextFloat() * .001f * wander * (this.random.nextBoolean() ? 1 : -1));
        wander *= .98f;
        if (onGround) {
            yj = Math.abs(yj);
        }
        this.xd += xj;
        this.yd += yj;
        this.zd += zj;
        if (--litTimer <= 0) {
            lit = !lit;
            litTimer = random.nextIntBetweenInclusive(10, 50);
            this.setSprite(sprites.get(lit ? 0 : 1, 1));
        }
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return lit ? LightTexture.FULL_BRIGHT : super.getLightColor(pPartialTick);
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
            return new FireflyParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }

    }
}
