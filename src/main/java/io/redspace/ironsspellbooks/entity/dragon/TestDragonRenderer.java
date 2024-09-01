package io.redspace.ironsspellbooks.entity.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class TestDragonRenderer extends LivingEntityRenderer<DragonEntity, TestDragonModel> {

    ResourceLocation resourceLocation = IronsSpellbooks.id("textures/entity/dragon/dragon.png");
    TestDragonModel model;

    public TestDragonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new TestDragonModel(pContext.bakeLayer(TestDragonModel.LAYER_LOCATION)), 2f);
        this.model = getModel();
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEntity pEntity) {
        return resourceLocation;
    }

//    @Override
//    protected void scale(DragonEntity pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
//        super.scale(pLivingEntity, pMatrixStack, pPartialTickTime);
//        float scale = 1.6f;
//        pMatrixStack.scale(scale, scale, scale);
//    }

    @Override
    public void render(DragonEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if(true)
            return;
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        //float f6 = Mth.lerp(pPartialTick, pEntity.xRotO, pEntity.getXRot());
        //this.model.setupAnim(pEntity, 0, 0, 0, 0, f6);
        //VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(pEntity)));
        //this.model.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(DragonEntity pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
        ResourceLocation resourcelocation = this.getTextureLocation(pLivingEntity);
        if (pTranslucent) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (pBodyVisible) {
            return RenderType.armorCutoutNoCull(getTextureLocation(pLivingEntity));
        } else {
            return pGlowing ? RenderType.outline(resourcelocation) : null;
        }
    }
}
