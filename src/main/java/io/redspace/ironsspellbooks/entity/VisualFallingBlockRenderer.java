package io.redspace.ironsspellbooks.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisualFallingBlockRenderer extends EntityRenderer<VisualFallingBlockEntity> {
   private final BlockRenderDispatcher dispatcher;

   public VisualFallingBlockRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.shadowRadius = 0.5F;
      this.dispatcher = pContext.getBlockRenderDispatcher();
   }

   public void render(VisualFallingBlockEntity entity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
      BlockState blockstate = entity.getBlockState();
      if (blockstate.getRenderShape() == RenderShape.MODEL) {
         Level level = entity.getLevel();
         if (blockstate != level.getBlockState(entity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            pMatrixStack.pushPose();
            pMatrixStack.translate(-0.5D, 0.0D, -0.5D);
            this.dispatcher.renderSingleBlock(entity.getBlockState(), pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
            pMatrixStack.popPose();
            super.render(entity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
         }
      }
   }

   public ResourceLocation getTextureLocation(VisualFallingBlockEntity pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
