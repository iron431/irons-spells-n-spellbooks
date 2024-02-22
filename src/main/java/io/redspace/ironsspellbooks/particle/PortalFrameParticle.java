package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortalFrameParticle extends TextureSheetParticle {
    float pathWidth, pathHeight, yRad, rotSpeed, rot, width;
    final Vec3 origin, forward;

    public PortalFrameParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double pathWidth, double pathHeight, double yDegrees) {
        super(level, xCoord, yCoord, zCoord, pathWidth, pathHeight, yDegrees);
        this.origin = new Vec3(xCoord, yCoord, zCoord);
        this.pathWidth = (float) pathWidth * .5f;
        this.pathHeight = (float) pathHeight * .5f;
        this.yRad = (float) (yDegrees * Mth.DEG_TO_RAD);
        this.scale(this.random.nextFloat() * 1.75f + 1f);
        this.lifetime = 40 + (int) (Math.random() * 45);
        this.gravity = 0.0F;
        this.rotSpeed = Utils.random.nextFloat() * .4f;
        this.rotSpeed *= this.rotSpeed * this.rotSpeed *(Utils.random.nextBoolean() ? -1 : 1);
        this.width = Utils.random.nextIntBetweenInclusive(20, 40) * .01f;
//https://www.desmos.com/calculator/mpx189dgxi
        this.rot = Utils.random.nextFloat() * Mth.TWO_PI;
        this.quadSize = .0625f;
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f * 0.9F;
        this.gCol = f * 0.3F;
        this.bCol = f;
        this.forward = new Vec3(Mth.sin(yRad), 0, Mth.cos(yRad));
        level.addParticle(ParticleHelper.UNSTABLE_ENDER, xCoord, yCoord, zCoord, forward.x, forward.y, forward.z);

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
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            updatePos();
            this.pathWidth *= (float) (1.0 + Utils.random.nextFloat() * .1f * rotSpeed);
            this.pathHeight *= (float) (1.0 + Utils.random.nextFloat() * .1f * rotSpeed);
            this.roll = (float) Mth.atan2(-Mth.sin(rot), Mth.cos(rot));
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
        //TODO: custom render type
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float skew = (float) new Vec3(vec3.x - x, vec3.y - y, vec3.z - z).dot(forward);
        float f = (float) (Mth.lerp((double) pPartialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp((double) pPartialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp((double) pPartialTicks, this.zo, this.z) - vec3.z());
        Quaternion quaternion;
        if (this.roll == 0.0F) {
            quaternion = pRenderInfo.rotation();
        } else {
            quaternion = new Quaternion(pRenderInfo.rotation());
            float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll) * skew;
            quaternion.mul(Vector3f.ZP.rotation(f3));
        }
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(pPartialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.mul(f4 * this.width, f4, f4);
            vector3f.transform(quaternion);
            vector3f.add(f, f1, f2);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(pPartialTicks);
        pBuffer.vertex((double) avector3f[0].x(), (double) avector3f[0].y(), (double) avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double) avector3f[1].x(), (double) avector3f[1].y(), (double) avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double) avector3f[2].x(), (double) avector3f[2].y(), (double) avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double) avector3f[3].x(), (double) avector3f[3].y(), (double) avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
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
