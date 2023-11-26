package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.function.Consumer;

public class FogParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;

    FogParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, FogParticleOptions options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);

        float mag = .3f;
        this.xd = xd + (Math.random() * 2.0D - 1.0D) * mag;
        this.yd = yd + (Math.random() * 2.0D - 1.0D) * mag;
        this.zd = zd + (Math.random() * 2.0D - 1.0D) * mag;
        double d0 = (Math.random() + Math.random() + 1.0D) * mag * .3f;
        double d1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / d1 * d0 * mag;
        this.yd = this.yd / d1 * d0 * mag + mag * .25f;
        this.zd = this.zd / d1 * d0 * mag;

        this.quadSize = 1.5f * options.getScale();
        this.lifetime = Utils.random.nextIntBetweenInclusive(60, 120);
        this.gravity = .1f;

        float f = random.nextFloat() * 0.14F + 0.85F;
        this.rCol = options.getColor().x() * f;
        this.gCol = options.getColor().y() * f;
        this.bCol = options.getColor().z() * f;
        this.friction = 1;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * (1 + Mth.clamp((this.age + pScaleFactor) / (float) this.lifetime * 0.75F, 0.0F, 1.0F)) * Mth.clamp(age / 5f, 0, 1);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd -= 0.04D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.yd *= .85f;
            this.xd *= .94f;
            this.zd *= .94f;
        }

    }

    private float noise(float offset) {

        float f = 10 * Mth.sin(offset * .01f);
//        if (f > max) {
//            max = f;
//            IronsSpellbooks.LOGGER.debug("Min: {} | Max: {}", min, max);
//        }
//        if (f < min) {
//            min = f;
//            IronsSpellbooks.LOGGER.debug("Min: {} | Max: {}", min, max);
//        }
        return f;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialticks) {
        /*
        Copied from Shriek Particle
         */
        this.alpha = 1.0F - Mth.clamp(((float) this.age + partialticks - 20) / (float) this.lifetime, 0.2F, .7F);

//        this.renderBillboard(buffer, camera, partialticks);
        this.renderRotatedParticle(buffer, camera, partialticks, (p_234005_) -> {
            p_234005_.mul(Axis.YP.rotation(0));
            p_234005_.mul(Axis.XP.rotation(-DEGREES_90));
        });
        this.renderRotatedParticle(buffer, camera, partialticks, (p_234000_) -> {
            p_234000_.mul(Axis.YP.rotation(-(float) Math.PI));
            p_234000_.mul(Axis.XP.rotation(DEGREES_90));
        });
    }

//    private void renderBillboard(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
//        Vec3 vec3 = pRenderInfo.getPosition();
//        float f = (float) (Mth.lerp((double) pPartialTicks, this.xo, this.x) - vec3.x());
//        float f1 = (float) (Mth.lerp((double) pPartialTicks, this.yo, this.y) - vec3.y());
//        float f2 = (float) (Mth.lerp((double) pPartialTicks, this.zo, this.z) - vec3.z());
//        Quaternion quaternion;
//        if (this.roll == 0.0F) {
//            quaternion = pRenderInfo.rotation();
//        } else {
//            quaternion = new Quaternion(pRenderInfo.rotation());
//            float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
//            quaternion.mul(Axis.ZP.rotation(f3));
//        }
//
//        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
//        vector3f1.transform(quaternion);
//        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
//        float f4 = this.getQuadSize(pPartialTicks);
//
//        for (int i = 0; i < 4; ++i) {
//            Vector3f vector3f = avector3f[i];
//            vector3f.transform(quaternion);
//            vector3f.mul(f4);
//            vector3f.add(f, f1, f2);
//        }
//
//        float f7 = this.getU0();
//        float f8 = this.getU1();
//        float f5 = this.getV0();
//        float f6 = this.getV1();
//        int j = this.getLightColor(pPartialTicks);
//        float scuff = f4 * .775f;
//        pBuffer.vertex((double) avector3f[0].x(), (double) avector3f[0].y() + scuff, (double) avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
//        pBuffer.vertex((double) avector3f[1].x(), (double) avector3f[1].y() - scuff, (double) avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
//        pBuffer.vertex((double) avector3f[2].x(), (double) avector3f[2].y() - scuff, (double) avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
//        pBuffer.vertex((double) avector3f[3].x(), (double) avector3f[3].y() + scuff, (double) avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
//    }

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera camera, float partialTick, Consumer<Quaternionf> pQuaternion) {
        /*
        Copied from Shriek Particle
         */
        Vec3 vec3 = camera.getPosition();
//        Vec3 zFightHack = camera.getPosition().subtract(this.x, this.y, this.z);
//        zFightHack = zFightHack.multiply(1f, 0.75f, 1f);
//        vec3 = zFightHack.add(this.x, this.y, this.z);
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        pQuaternion.accept(quaternion);
        quaternion.transform(TRANSFORM_VECTOR);

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(partialTick);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        int j = this.getLightColor(partialTick);
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVec3f, float p_233996_, float p_233997_, int p_233998_) {
        Vec3 wiggle = new Vec3(noise((float) (age + this.x)), noise((float) (age - this.x)), noise((float) (age + this.z))).scale(.02f);
        pConsumer.vertex(pVec3f.x() + wiggle.x, pVec3f.y() + .08f + alpha * .125f, pVec3f.z() + wiggle.z).uv(p_233996_, p_233997_).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p_233998_).endVertex();
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<FogParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull FogParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            FogParticle shriekparticle = new FogParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            shriekparticle.pickSprite(this.sprite);
            shriekparticle.setAlpha(1.0F);
            return shriekparticle;
        }
    }

}
