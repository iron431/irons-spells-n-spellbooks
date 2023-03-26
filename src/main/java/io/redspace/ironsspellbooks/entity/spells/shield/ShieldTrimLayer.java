package io.redspace.ironsspellbooks.entity.spells.shield;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ShieldTrimLayer extends RenderLayer<ShieldEntity, ShieldModel> {
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/shield/shield_trim.png");
    private final ShieldTrimModel model;

    public ShieldTrimLayer(RenderLayerParent<ShieldEntity, ShieldModel> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.model = new ShieldTrimModel(context.bakeLayer(ShieldTrimModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ShieldEntity entity, float pLimbSwing, float pLimbSwingAmount, float partialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        var offset = ShieldRenderer.getEnergySwirlOffset(entity, partialTicks, 3456);
        //VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(getTextureLocation(entity), offset.x, offset.y));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(TEXTURE,0,0));
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, .45f);
    }
}
