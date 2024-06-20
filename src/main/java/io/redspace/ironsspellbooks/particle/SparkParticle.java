package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class SparkParticle extends TextureSheetParticle {
    public SparkParticle(SparkParticleOptions options, ClientLevel level, double xCoord, double yCoord, double zCoord, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.scale(this.random.nextFloat() * .65f + .4f);
        this.lifetime = 20 + (int) (Math.random() * 45);
        this.gravity = 1.3F;
        this.friction = .985f;
        this.quadSize = .0625f;
        this.rCol = options.color.x() * (.9f + this.random.nextFloat() * 0.1f);
        this.gCol = options.color.y() * (.9f + this.random.nextFloat() * 0.1f);
        this.bCol = options.color.z() * (.9f + this.random.nextFloat() * 0.1f);
        bounciness = .6f + this.random.nextFloat() * .2f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
    }

    boolean touchedGround;
    float bounciness;

    @Override
    public void tick() {
        if (!touchedGround && lifetime < 80) {
            //try to let sparks bounce at least once
            lifetime++;
        }
        if (this.onGround) {
            touchedGround = true;
            this.yd *= -bounciness;
            bounciness *= .8f;
            quadSize *= .9f;
        }
        super.tick();
        if (new Vec3(x - xo, y - yo, z - zo).lengthSqr() < 0.001) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ZapParticle.PARTICLE_EMISSIVE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SparkParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(@NotNull SparkParticleOptions options, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            var particle = new SparkParticle(options, level, x, y, z, dx, dy, dz);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }

    @Override
    public int getLightColor(float p_107564_) {
        return LightTexture.FULL_BRIGHT;
    }
}
