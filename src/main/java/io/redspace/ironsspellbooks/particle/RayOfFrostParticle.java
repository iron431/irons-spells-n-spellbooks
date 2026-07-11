package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class RayOfFrostParticle extends TextureSheetParticle {
    private Vec3 destination;
    private boolean inner;

    RayOfFrostParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, RayOfFrostRayParticleOptions options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.setSize(1, 1);
        this.quadSize = 1f;
        this.destination = options.getDestination();
        this.lifetime = 8;
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
    }

    @Override
    public void tick() {
        if (age == 0 && inner) {
            Vec3 rayVector = destination.subtract(this.getPos());
            double distance = Math.min(rayVector.length(), 128);
            rayVector = rayVector.normalize();
            float gap = 1.5f;
            for (float i = 0; i < distance; i += gap) {
                Vec3 pos = getPos().add(rayVector.scale(i));
                Vec3 speed = Utils.getRandomVec3(0.043);
                level.addAlwaysVisibleParticle(ParticleHelper.SNOWFLAKE, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
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
        float baseWidth = 0.25f;
        float hullLength = 1 * baseWidth;
        double distance = Math.min(rayVector.length(), 128);
        rayVector = rayVector.normalize();
        int hulls = (int) (distance / hullLength);
        if (hulls <= 0) {
            return;
        }
        double zmargin = distance / (hulls * hullLength); // make up marginal differences caused by truncation of hull unit length
        Vec2 rotation = Utils.rotationFromDirection(rayVector);
        poseStack.mulPose(Axis.YP.rotation(rotation.y));
        poseStack.mulPose(Axis.XP.rotation(-rotation.x));
        poseStack.scale(1, 1, (float) zmargin);

        float age = this.age + partialTick;
        for (int i = 0; i < hulls; i++) {
            if (inner) {
                float coreExpansion = Mth.clampedLerp(1f, 0f, age / (lifetime - 4f));
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(age * -10));
                poseStack.scale(coreExpansion, coreExpansion, 1);
                drawHull(Vec3.ZERO, new Vec3(0, 0, hullLength), baseWidth, baseWidth, poseStack, consumer);
                poseStack.popPose();
            } else {
                float overlayExpansion = Mth.clampedLerp(1.2f, 0f, age / lifetime);
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(age * 5 + 45));
                poseStack.scale(overlayExpansion, overlayExpansion, 1);
                drawHull(Vec3.ZERO, new Vec3(0, 0, hullLength), baseWidth, baseWidth, poseStack, consumer);
                poseStack.popPose();
            }
            poseStack.translate(0, 0, hullLength);
        }
    }

    @Override
    protected float getU1() {
        return Mth.lerp(0.25f, super.getU0(), super.getU1());
    }

    @Override
    protected float getV1() {
        return Mth.lerp(0.75f, super.getV0(), super.getV1());
    }

    @Override
    protected float getV0() {
        return Mth.lerp(0.25f, super.getV0(), super.getV1());
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
        if (!inner) {
            //backface
            consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU0(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU1(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU1(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU0(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        }
        consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU0(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU1(), getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU1(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU0(), getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return PARTICLE_EMISSIVE_CULL;//inner ? PARTICLE_EMISSIVE_CULL : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static ParticleRenderType PARTICLE_EMISSIVE_CULL = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public boolean isTranslucent() {
            return true;
        }

        @Override
        public String toString() {
            return "irons_spellbooks:particle_emissive";
        }
    };

    @Override
    protected int getLightColor(float pPartialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<RayOfFrostRayParticleOptions> {
        private final SpriteSet sprite;
        private final boolean inner;

        public Provider(SpriteSet pSprite, boolean b) {
            this.sprite = pSprite;
            this.inner = b;
        }

        public Particle createParticle(@NotNull RayOfFrostRayParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            var particle = new RayOfFrostParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            particle.pickSprite(this.sprite);
            particle.setAlpha(1.0F);
            particle.inner = inner;
            return particle;
        }
    }
}
