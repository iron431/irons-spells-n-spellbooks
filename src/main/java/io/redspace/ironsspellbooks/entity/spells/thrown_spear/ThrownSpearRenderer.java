package io.redspace.ironsspellbooks.entity.spells.thrown_spear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownSpearRenderer extends EntityRenderer<ThrownSpear> {

    public ThrownSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(ThrownSpear entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var item = entity.getWeaponItem();
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 10));

        poseStack.translate(0, -1.5, 0);
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level, 0);
        if (entity.isChanneled()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90-10));
            LightningLanceRenderer.renderModel(poseStack, buffer, entity.tickCount);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(ThrownSpear entity) {
        return ThrownTridentRenderer.TRIDENT_LOCATION;
    }
}
