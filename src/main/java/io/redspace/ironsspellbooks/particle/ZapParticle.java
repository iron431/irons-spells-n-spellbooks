package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class ZapParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;

    Vec3 destination;

    ZapParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, ZapParticleOption options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.setSize(1, 1);
        IronsSpellbooks.LOGGER.debug("quad size: {}", this.quadSize);
        this.quadSize = 1f;
        this.destination = options.getDestination().getPosition(pLevel).orElse(new Vec3(pX, pY, pZ));
        this.lifetime = pLevel.random.nextIntBetweenInclusive(60, 120);
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());

        Quaternion quaternion = new Quaternion(ROTATION_VECTOR, 0.0F, true);

        float width = .15f;
        float h = width * .5f;

        Vector3f start = Vector3f.ZERO;
        Vector3f end = new Vector3f((float) (destination.x - this.x), (float) (destination.y - this.y), (float) (destination.z - this.z));

        //normal is Counterclockwise
        Vector3f[] avector3f = new Vector3f[]{
                new Vector3f(0, -h, -h),
                new Vector3f(0, h, -h),
                new Vector3f(0, h, h),
                new Vector3f(0, -h, h)
        };
        Vector3f[] left = new Vector3f[]{
                new Vector3f(h + start.x(), -h + start.y(), start.z()),
                new Vector3f(h + start.x(), h + start.y(), start.z()),
                new Vector3f(h + end.x(), h + end.y(), end.z()),
                new Vector3f(h + end.x(), -h + end.y(), end.z())
        };
        Vector3f[] right = new Vector3f[]{
                new Vector3f(-h + end.x(), -h + end.y(), end.z()),
                new Vector3f(-h + end.x(), h + end.y(), end.z()),
                new Vector3f(-h + start.x(), h + start.y(), start.z()),
                new Vector3f(-h + start.x(), -h + start.y(), start.z())
        };
        Vector3f[] top = new Vector3f[]{
                new Vector3f(h + start.x(), h + start.y(), start.z()),
                new Vector3f(-h + start.x(), h + start.y(), start.z()),
                new Vector3f(-h + end.x(), h + end.y(), end.z()),
                new Vector3f(h + end.x(), h + end.y(), end.z())
        };
        Vector3f[] bottom = new Vector3f[]{
                new Vector3f(h + end.x(), -h + end.y(), end.z()),
                new Vector3f(-h + end.x(), -h + end.y(), end.z()),
                new Vector3f(-h + start.x(), -h + start.y(), start.z()),
                new Vector3f(h + start.x(), -h + start.y(), start.z())
        };

        quad(consumer, partialTick, f, f1, f2, quaternion, left);
        quad(consumer, partialTick, f, f1, f2, quaternion, right);
        quad(consumer, partialTick, f, f1, f2, quaternion, top);
        quad(consumer, partialTick, f, f1, f2, quaternion, bottom);
        //pQuaternion.accept(quaternion);
        //TRANSFORM_VECTOR.transform(quaternion);
//        Vector3f[] top = new Vector3f[]{
//                new Vector3f(start.x() - h, h, start.z()),
//                new Vector3f(end.x() - h, h, end.z()),
//                new Vector3f(end.x() + h, h, end.z()),
//                new Vector3f(start.x() + h, h, start.z())
//        };
//        Vector3f[] bottom = new Vector3f[]{
//                new Vector3f(start.x() + h, -h, start.z()),
//                new Vector3f(end.x() + h, -h, end.z()),
//                new Vector3f(end.x() - h, -h, end.z()),
//                new Vector3f(start.x() - h, -h, start.z())
//        };
//        Vector3f[] right = new Vector3f[]{
//                new Vector3f(start.x() + h, h, start.z()),
//                new Vector3f(end.x() + h, h, end.z()),
//                new Vector3f(end.x() + h, -h, end.z()),
//                new Vector3f(start.x() + h, -h, start.z())
//        };
//        Vector3f[] left = new Vector3f[]{
//                new Vector3f(start.x() - h, -h, start.z()),
//                new Vector3f(end.x() - h, -h, end.z()),
//                new Vector3f(end.x() - h, h, end.z()),
//                new Vector3f(start.x() - h, h, start.z())
//        };
//        quad(consumer, partialTick, f, f1, f2, quaternion, top);
//        quad(consumer, partialTick, f, f1, f2, quaternion, bottom);
//        quad(consumer, partialTick, f, f1, f2, quaternion, right);
//        quad(consumer, partialTick, f, f1, f2, quaternion, left);
    }

    private void renderRotatedParticle(VertexConsumer consumer, Camera camera, float partialTick, Vector3f start, Vector3f end, float width) {

    }

    public void drawHull(VertexConsumer consumer, Vec3 from, Vec3 to, float width) {

        //        //Bottom
//        drawQuad(consumer, from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, r, g, b, a);
//        //Top
//        drawQuad(consumer, from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, r, g, b, a);
//        //Left
//        drawQuad(consumer, from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, r, g, b, a);
//        //Right
//        drawQuad(consumer, from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, r, g, b, a);
    }

    public void drawQuad(VertexConsumer consumer, Vec3 from, Vec3 to, float width, float height, int r, int g, int b, int a) {
        //to = new Vec3(1, 0, 10);
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;
        //float height = (float) (Math.random() * .25f) + .25f;
        //consumer.vertex((float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).endVertex();
        //consumer.vertex((float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).endVertex();
        //consumer.vertex((float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).endVertex();
        //consumer.vertex((float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).endVertex();
        this.makeCornerVertex(consumer, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z, this.getU1(), this.getV1());
        this.makeCornerVertex(consumer, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z, this.getU1(), this.getV0());
        this.makeCornerVertex(consumer, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z, this.getU0(), this.getV0());
        this.makeCornerVertex(consumer, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z, this.getU0(), this.getV1());
    }

    private void makeCornerVertex(VertexConsumer pConsumer, double x, double y, double z, float p_233996_, float p_233997_) {
        pConsumer.vertex(x, y, z).uv(p_233996_, p_233997_).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVec3f, float p_233996_, float p_233997_, int p_233998_) {
        pConsumer.vertex((double) pVec3f.x(), (double) pVec3f.y(), (double) pVec3f.z()).uv(p_233996_, p_233997_).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p_233998_).endVertex();
    }

    private void quad(VertexConsumer pConsumer, float partialTick, float f, float f1, float f2, Quaternion quaternion, Vector3f[] avector3f) {
        float f3 = this.getQuadSize(partialTick);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            //vector3f.transform(quaternion);
            vector3f.mul(f3);
            //vector3f.mul(8);
            vector3f.add(f, f1, f2);
        }

        int j = this.getLightColor(partialTick);
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        //TODO: custom render type
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ZapParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull ZapParticleOption options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            var particle = new ZapParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            particle.pickSprite(this.sprite);
            particle.setAlpha(1.0F);
            return particle;
        }
    }

}
