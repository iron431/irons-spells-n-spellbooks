package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractEnergySwirlLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public AbstractEnergySwirlLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (shouldRender(pLivingEntity)) {
            float f = (float) pLivingEntity.tickCount + pPartialTicks;
            EntityModel<T> entitymodel = this.model();
            entitymodel.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
            this.getParentModel().copyPropertiesTo(entitymodel);
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
            entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            entitymodel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
        }
    }

    protected abstract float xOffset(float p_116968_);

    protected abstract ResourceLocation getTextureLocation();

    protected abstract EntityModel<T> model();

    protected boolean shouldRender(T entity) {
        return true;
    }
}