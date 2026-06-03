package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class ZapParticle extends TextureSheetParticle {

    Vec3 destination;

    ZapParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, ZapParticleOption options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.setSize(1, 1);
        this.quadSize = 1f;
        this.destination = options.getDestination();
        this.lifetime = Utils.random.nextIntBetweenInclusive(3, 8);
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

    public Vec3 randomOffset(RandomSource random, float scale) {
        return new Vec3(
                (2f * random.nextFloat() - 1f) * scale,
                (2f * random.nextFloat() - 1f) * scale,
                (2f * random.nextFloat() - 1f) * scale
        );
    }

    private void setRGBA(float r, float g, float b, float a) {
        this.rCol = r * a;
        this.gCol = g * a;
        this.bCol = b * a;
        this.alpha = 1;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());
        PoseStack poseStack = new PoseStack();
        poseStack.translate(f, f1, f2);
        float quadScale = this.getQuadSize(partialTick);
        poseStack.scale(quadScale, quadScale, quadScale);

        float chanceToBranch = 0.2f;
        setRGBA(1, 1, 1, 1);
        renderLightningPass(consumer, poseStack, partialTick, 0.06f, chanceToBranch);

        setRGBA(.25f, .7f, 1, .3f);
        renderLightningPass(consumer, poseStack, partialTick, 0.11f, chanceToBranch);

        setRGBA(.25f, .7f, 1, .15f);
        renderLightningPass(consumer, poseStack, partialTick, 0.25f, chanceToBranch);
    }

    private void renderLightningPass(VertexConsumer consumer, PoseStack poseStack, float partialTick, float tubeWidth, float chanceToBranch) {
        RandomSource randomSource = RandomSource.create((age + lifetime) * 3456798L);
        Vec3 start = Vec3.ZERO;
        Vec3 end = destination.subtract(this.getPos()); // local space
        double distance = end.length();
        int segments = (int) (distance / 4 + randomSource.nextIntBetweenInclusive(1, 3));
        double distancePerSegment = distance / segments;
        Vec3 direction = end.normalize();
        for (int i = 0; i < segments; i++) {
            Vec3 wiggle = randomOffset(randomSource, .2f);
            Vec3 segmentEnd = start.add(direction.scale(distancePerSegment)).add(wiggle);

            drawLightningBeam(consumer, poseStack, partialTick, start, segmentEnd, tubeWidth, chanceToBranch, randomSource);

            start = segmentEnd;
//            end = end.subtract(wiggle).add(end);
        }
    }

    private void drawLightningBeam(VertexConsumer consumer, PoseStack poseStack, float partialTick, Vec3 start, Vec3 end, float tubeWidth, float chanceToBranch, RandomSource randomSource) {
        drawTube(consumer, poseStack, partialTick, start, end, tubeWidth);

        if (randomSource.nextFloat() < chanceToBranch) {
            Vec3 branch = randomOffset(randomSource, 1f).add(end);
            drawLightningBeam(consumer, poseStack, partialTick, end, branch, tubeWidth, chanceToBranch * .5f, randomSource);
        }
    }

    private void drawTube(VertexConsumer consumer, PoseStack poseStack, float partialTick, Vec3 start, Vec3 end, float width) {
        Vec3 delta = end.subtract(start);
        float length = (float) delta.length();
        if (length <= 1e-6f) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);
        Vec2 rotation = Utils.rotationFromDirection(delta.normalize());
        poseStack.mulPose(Axis.YP.rotation(rotation.y));
        poseStack.mulPose(Axis.XP.rotation(-rotation.x));
        drawHull(Vec3.ZERO, new Vec3(0, 0, length), width, width, poseStack, consumer, partialTick);
        poseStack.popPose();
    }

    private void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack poseStack, VertexConsumer consumer, float partialTick) {
        poseStack.pushPose();
        for (int i = 0; i < 4; i++) {
            drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, poseStack.last(), consumer, partialTick);
            poseStack.mulPose(Axis.ZP.rotation(Mth.HALF_PI));
        }
        poseStack.popPose();
    }

    private void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, float partialTick) {
        Matrix4f poseMatrix = pose.pose();
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;
        int light = getLightColor(partialTick);
        consumer.addVertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU1(), getV1()).setLight(light);
        consumer.addVertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU1(), getV0()).setLight(light);
        consumer.addVertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU0(), getV0()).setLight(light);
        consumer.addVertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv(getU0(), getV1()).setLight(light);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return PARTICLE_EMISSIVE;
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return AABB.INFINITE;
    }

    public static ParticleRenderType PARTICLE_EMISSIVE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
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
