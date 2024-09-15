package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CleanseParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private static final int BASE_LIFETIME = 8;

    protected CleanseParticle(
        ClientLevel pLevel,
        double pX,
        double pY,
        double pZ,
        double pXSpeed,
        double pYSpeed,
        double pZSpeed,
        float pSizeMultiplier,
        SpriteSet pSprites
    ) {
        super(pLevel, pX, pY, pZ, 0.0, 0.0, 0.0);
        this.sprites = pSprites;
        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd *= 0.0;
        this.yd *= 0.9;
        this.zd *= 0.0;
        this.xd += pXSpeed;
        this.yd += pYSpeed;
        this.zd += pZSpeed;
        this.quadSize *= 0.75F * pSizeMultiplier;
        this.lifetime = (int)(8.0F / Mth.randomBetween(this.random, 0.5F, 1.0F) * pSizeMultiplier);
        this.lifetime = Math.max(this.lifetime, 1);
        this.setSpriteFromAge(pSprites);
        this.hasPhysics = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return 240;
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_Y;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(
            SimpleParticleType pType,
            ClientLevel pLevel,
            double pX,
            double pY,
            double pZ,
            double pXSpeed,
            double pYSpeed,
            double pZSpeed
        ) {
            return new CleanseParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, 1.5F, this.sprites);
        }
    }
}
