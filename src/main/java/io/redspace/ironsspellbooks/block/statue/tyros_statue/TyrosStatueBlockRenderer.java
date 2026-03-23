package io.redspace.ironsspellbooks.block.statue.tyros_statue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironspatreonlib.game.block.statue.AbstractStatueBlock;
import io.redspace.ironspatreonlib.game.block.statue.client.StaticModel;
import io.redspace.ironspatreonlib.game.block.statue.decorative.DecorativeStatueBlockEntity;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;

public class TyrosStatueBlockRenderer implements BlockEntityRenderer<DecorativeStatueBlockEntity> {

    StaticModel model;

    public TyrosStatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new StaticModel(IronsSpellbooks.id("geo/statue/tyros_statue.geo.json"), IronsSpellbooks.id("textures/entity/statue/tyros.png")) {
            @Override
            protected void renderBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay) {
                super.renderBone(poseStack, bone, buffer, packedLight, packedOverlay);
                if (bone.getName().equals("right_arm")) {
                    poseStack.pushPose();
                    poseStack.translate(0.25f, 1.0f, 0.2f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(180));
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(70));
                    Minecraft.getInstance().getItemRenderer().render(
                            ItemRegistry.HELLRAZOR.get().getDefaultInstance(),
                            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                            false,
                            poseStack,
                            Minecraft.getInstance().renderBuffers().bufferSource(), packedLight, packedOverlay,
                            Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(IronsSpellbooks.id("item/stone_scythe")))
                    );
                    poseStack.popPose();
                }
            }
        };

    }

    @Override
    public void render(@NotNull DecorativeStatueBlockEntity statueBlock, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!statueBlock.isPrimary()) {
            return;
        }
        if (!(statueBlock.getBlockState().getBlock() instanceof AbstractStatueBlock statue)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(statue.xSize * .5f, 0, statue.zSize * 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(statueBlock.getBlockState().getValue(SkullBlock.ROTATION))));
        poseStack.scale(1.7f, 1.7f, 1.7f);
        this.model.render(poseStack, RenderType::entityCutoutNoCull, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }


    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull DecorativeStatueBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
