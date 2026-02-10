package io.redspace.ironsspellbooks.entity.spells.fireball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
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

public class FireballRenderer extends EntityRenderer<Projectile> {

    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "fireball_model"), "main");
    public static final ResourceLocation BASE_TEXTURE = IronsSpellbooks.id("textures/entity/fireball/fireball_core.png");
    public static final ResourceLocation[] FIRE_TEXTURES = {
            IronsSpellbooks.id("textures/entity/fireball/fire_0.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_1.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_2.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_3.png")/*,
            IronsSpellbooks.id("textures/entity/fireball/fire_4.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_5.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_6.png"),
            IronsSpellbooks.id("textures/entity/fireball/fire_7.png")*/
    };


    protected final ModelPart body;
    protected final ModelPart outline;

    protected final float scale;

    public FireballRenderer(Context context, float scale) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.body = modelpart.getChild("body");
        this.outline = modelpart.getChild("outline");
        this.scale = scale;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("outline", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 16.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 48, 24);
    }

    @Override
    public void render(Projectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBoundingBox().getYsize() * .5f, 0);
        poseStack.scale(scale, scale, scale);
        if (entity instanceof AbstractMagicProjectile) {
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 180));
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        } else {
            // replaced fireball renderer. these use different rules
            poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
            poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.body.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getFireTextureLocation(entity)));
        poseStack.scale(1.15f, 1.15f, 1.15f);
        this.outline.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);


        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Projectile entity) {
        return BASE_TEXTURE;
    }

    public ResourceLocation getFireTextureLocation(Projectile entity) {
        int frame = (entity.tickCount / 2) % FIRE_TEXTURES.length;
        return FIRE_TEXTURES[frame];
    }
}