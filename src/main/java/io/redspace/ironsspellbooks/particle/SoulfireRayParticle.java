package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SoulfireRayParticle extends TextureSheetParticle {

    Vec3 destination;
    boolean firsttick = false;

    SoulfireRayParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, SoulfireRayParticleOptions options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.setSize(1, 1);
        this.quadSize = 1f;
        this.destination = options.getDestination();
        this.lifetime = 15;
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
    }

    @Override
    public void tick() {
        if (!firsttick) {
            firsttick = true;
            var particle = ParticleTypes.SOUL_FIRE_FLAME; //ParticleHelper.FIRE;
            for (int i = 0; i < 25; i++) {
                Vec3 random = Utils.getRandomVec3(0.6);
                level.addParticle(particle, destination.x, destination.y, destination.z, random.x, random.y, random.z);
            }
            Vec3 delta = destination.subtract(this.getPos());
            double length = delta.length();
            float volume = 0.4f;
            delta = delta.normalize();
            for (float i = 0; i < length; i += volume) {
                Vec3 random = Utils.getRandomVec3(0.03);
                level.addParticle(particle, this.x + delta.x * i, this.y + delta.y * i, this.z + delta.z * i, random.x, random.y, random.z);
            }
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return AABB.INFINITE;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());
        PoseStack poseStack = new PoseStack();
        poseStack.translate(f, f1, f2);
        Vec3 rayVector = destination.subtract(this.getPos());
        float baseWidth = 1f;
        float hullLength = 3 * baseWidth;
        double distance = rayVector.length();
        int hulls = (int) (distance / hullLength);
        double zmargin = distance / (hulls * hullLength); // make up marginal differences caused by truncation of hull unit length
        rayVector = rayVector.normalize();
        Vec2 rotation = Utils.rotationFromDirection(rayVector);
        poseStack.mulPose(Axis.YP.rotation(rotation.y));
        poseStack.mulPose(Axis.XP.rotation(-rotation.x));
        poseStack.mulPose(Axis.ZP.rotationDegrees((age + partialTick) * 360 / lifetime));
        poseStack.scale(1, 1, (float) zmargin);
        float t = Math.clamp((age + partialTick) / (float) lifetime, 0, 1);
        float width = Mth.lerp(t, 0.1f, baseWidth);
        this.alpha = Mth.lerp(t, 1, 0);

        for (int i = 0; i < hulls; i++) {
            drawHull(Vec3.ZERO, new Vec3(0, 0, hullLength), width, width, poseStack, consumer);
            drawHull(Vec3.ZERO, new Vec3(0, 0, hullLength), width * .5f, width * .5f, poseStack, consumer);
            poseStack.translate(0, 0, hullLength);
        }
    }

    public void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack poseStack, VertexConsumer consumer) {
        poseStack.pushPose();
        for (int i = 0; i < 4; i++) {
            drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, poseStack.last(), consumer);
            poseStack.mulPose(Axis.ZP.rotation(Mth.HALF_PI));
        }
        poseStack.popPose();
    }

    public void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer) {
        Matrix4f poseMatrix = pose.pose();
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;
        int light = getLightColor(0);
        consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU0(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU1(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU1(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU0(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        //backface
        consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU0(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU1(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU1(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(this.rCol, this.bCol, this.gCol, this.alpha).setUv(getU0(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    @Override
    protected int getLightColor(float pPartialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SoulfireRayParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull SoulfireRayParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            var particle = new SoulfireRayParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            particle.pickSprite(this.sprite);
            particle.setAlpha(1.0F);
            return particle;
        }
    }

}
