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
import net.minecraftforge.client.model.data.ModelData;

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
         Level level = entity.level;
//         if (blockstate != level.getBlockState(pEntity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         pMatrixStack.pushPose();
         BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
         pMatrixStack.translate(-0.5D, 0.0D, -0.5D);
         var model = this.dispatcher.getBlockModel(blockstate);
         for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(blockstate.getSeed(entity.getStartPos())), ModelData.EMPTY))
            this.dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, pMatrixStack, pBuffer.getBuffer(renderType), false, RandomSource.create(), blockstate.getSeed(entity.getStartPos()), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
         pMatrixStack.popPose();
         super.render(entity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      }
   }

   public ResourceLocation getTextureLocation(VisualFallingBlockEntity pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
