package io.redspace.ironsspellbooks.particle;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortalFrameParticle extends TextureSheetParticle {
    float pathWidth, pathHeight, yRad, rotSpeed, rot;
    final Vec3 origin;

    public PortalFrameParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double pathWidth, double pathHeight, double yDegrees) {
        super(level, xCoord, yCoord, zCoord, pathWidth, pathHeight, yDegrees);
        this.origin = new Vec3(xCoord, yCoord, zCoord);
        this.pathWidth = (float) pathWidth * .5f;
        this.pathHeight = (float) pathHeight * .5f;
        this.yRad = (float) (yDegrees * Mth.DEG_TO_RAD);
        this.scale(this.random.nextFloat() * 1.75f + 1f);
        this.lifetime = 40 + (int) (Math.random() * 45);
        this.gravity = 0.0F;
        this.rotSpeed = Utils.random.nextIntBetweenInclusive(5, 10) * .04f;
        this.rotSpeed *= this.rotSpeed * (Utils.random.nextBoolean() ? -1 : 1);
//https://www.desmos.com/calculator/mpx189dgxi
        this.rot = Utils.random.nextFloat() * Mth.TWO_PI;
        this.quadSize = .0625f;
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f * 0.9F;
        this.gCol = f * 0.3F;
        this.bCol = f;

        updatePos();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            updatePos();
            this.pathWidth *= (float) (1.0 + Utils.random.nextFloat() * .075f * rotSpeed);
            this.pathHeight *= (float) (1.0 + Utils.random.nextFloat() * .075f * rotSpeed);
            this.quadSize -= rotSpeed * rotSpeed * .02f;
        }
    }

    private void updatePos() {
        this.x = origin.x + Mth.cos(rot) * pathWidth * Mth.cos(yRad);
        this.y = origin.y + Mth.sin(rot) * pathHeight;
        this.z = origin.z + Mth.cos(rot) * pathWidth * Mth.sin(yRad);
        rot = (rotSpeed + rot) % Mth.TWO_PI;
        var speed = new Vec3(x - xo, y - yo, z - zo);
        this.setBoundingBox(this.getBoundingBox().move(speed.x, speed.y, speed.z));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ZapParticle.PARTICLE_EMISSIVE;
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
            var particle = new PortalFrameParticle(level, x, y, z, dx, dy, dz);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }

    @Override
    public int getLightColor(float p_107564_) {
        return LightTexture.FULL_BRIGHT;
    }
}
