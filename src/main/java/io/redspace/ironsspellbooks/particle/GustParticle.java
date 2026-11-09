package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

// The particle's velocity carries the gust's direction; the particle itself does not move.
public class GustParticle extends Particle {
    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(IronsSpellbooks.id("gust_model"), "main");
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/trident_riptide.png");

    private final ModelPart body;
    private final Vec3 direction;
    private final float yRot;
    private final float xRot;

    GustParticle(ClientLevel level, double x, double y, double z, Vec3 direction) {
        super(level, x, y, z);
        this.direction = direction.normalize();
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.gravity = 0;
        this.hasPhysics = false;
        this.lifetime = 8;
        this.yRot = (float) (Mth.atan2(-this.direction.x, this.direction.z) * Mth.RAD_TO_DEG);
        this.xRot = (float) (Math.asin(-this.direction.y) * Mth.RAD_TO_DEG);
        this.body = Minecraft.getInstance().getEntityModels().bakeLayer(MODEL_LAYER_LOCATION).getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        meshdefinition.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8, -16, -8, 16, 32, 16), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void tick() {
        if (++age > lifetime) {
            remove();
            return;
        }
        if (age <= 2) {
            spawnPoof();
        }
    }

    private void spawnPoof() {
        Vec3 pos = new Vec3(x, y, z).add(direction.scale(1.6));
        double speed = random.nextDouble() * .4 + .45;
        for (int i = 0; i < 5; i++) {
            double offset = .25;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;
            double angularness = .8;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = (direction.scale(3).add(randomVec)).normalize().scale(speed);
            level.addParticle(ParticleTypes.POOF, pos.x + ox, pos.y + oy, pos.z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    public void render(VertexConsumer ignored, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(x - cameraPos.x, y - cameraPos.y + .5, z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot - 90));
        poseStack.scale(.25f, .25f, .25f);

        float f = age + partialTick;
        float scale = Mth.lerp(Mth.clamp(f / 6f, 0, 1), 1, 2.3f);
        float alpha = 1f - f / 10f;
        int light = getLightColor(partialTick);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        for (int i = 0; i < 3; i++) {
            poseStack.mulPose(Axis.YP.rotationDegrees(f * 10));
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0, scale - 1, 0);
            this.body.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.color((int) (alpha * 255), 255, 255, 255));
        }
        bufferSource.endBatch();
    }

    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return new AABB(x, y, z, x, y, z).inflate(4);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new GustParticle(level, x, y, z, new Vec3(xd, yd, zd));
        }
    }
}
