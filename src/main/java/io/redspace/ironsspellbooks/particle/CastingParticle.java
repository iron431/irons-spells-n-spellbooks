package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class CastingParticle extends TextureSheetParticle {

    public CastingParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double xd, double yd, double zd, CastingParticleOptions options) {

        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        //this.friction = 1.0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= .85f;
        this.scale(2.5f * options.getScale());
        float darkness = level.getRandom().nextFloat() * .3f + .7f;
        this.rCol = options.getColor().x() * darkness;
        this.gCol = options.getColor().y() * darkness;
        this.bCol = options.getColor().z() * darkness;
        this.lifetime = 6 + (int) (Math.random() * 10);
        this.gravity = 0F;
    }

    @Override
    public void tick() {
        super.tick();
        this.quadSize *= .95f;
        this.yd += this.random.nextFloat() / 200.0F;

    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<CastingParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull CastingParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            CastingParticle particle = new CastingParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            particle.pickSprite(this.sprite);
            particle.setAlpha(1.0F);
            return particle;
        }
    }
}
