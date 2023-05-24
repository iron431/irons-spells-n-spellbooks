package io.redspace.ironsspellbooks.entity.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class FireballRenderer extends EntityRenderer<Projectile> {

    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(IronsSpellbooks.MODID, "acid_orb_model"), "main");
    private static ResourceLocation ORB_TEXTURE = IronsSpellbooks.id("textures/entity/fireball/fireball.png");
    private static ResourceLocation FIRE_TEXTURES[] = {
            IronsSpellbooks.id("textures/entity/fireball/fire_0.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_1.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_2.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_3.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_4.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_5.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_6.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_7.png")
    };


    private final ModelPart orb;
    private final ModelPart swirl;

    public FireballRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.orb = modelpart.getChild("orb");
        this.swirl = modelpart.getChild("swirl");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("swirl", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    @Override
    public void render(Projectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBoundingBox().getYsize() * .5f, 0);
        float size = (float) entity.getBoundingBox().getXsize();
        poseStack.scale(size, size, size);
        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.orb.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        float f = entity.tickCount + partialTicks;
//        float swirlX = Mth.cos(.08f * f) * 180;
//        float swirlY = Mth.sin(.08f * f) * 180;
//        float swirlZ = Mth.cos(.08f * f + 5464) * 180;
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(swirlX));
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(swirlY));
//        poseStack.mulPose(Vector3f.ZP.rotationDegrees(swirlZ));
//        int frameCount = 32;
//        float uv = 1f / frameCount;
//        int frame = (int) ((f) % frameCount);
        consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getFireTextureLocation(entity)));
        poseStack.scale(1.15f, 1.15f, 1.15f);
        this.swirl.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);


        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Projectile entity) {
        return ORB_TEXTURE;
    }

    private ResourceLocation getFireTextureLocation(Projectile entity) {
        int frame = (entity.tickCount) % FIRE_TEXTURES.length;
        return FIRE_TEXTURES[frame];
    }
}