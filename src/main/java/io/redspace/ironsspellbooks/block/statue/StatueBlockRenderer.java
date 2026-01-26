package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureHolder;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.NotNull;

public class StatueBlockRenderer implements BlockEntityRenderer<StatueBlockEntity> {

    PlayerModel<Player> model, modelSlim;

    //    StatueGeoRenderer playerRenderer;
//    StatueGeoRenderer testRenderer;
    StaticModel playerModel, testModel;

    public StatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.model.young = false;
        this.modelSlim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.modelSlim.young = false;
//        this.testRenderer = new StatueGeoRenderer(new StatueGeoRenderer.Model(IronsSpellbooks.MODID, "sbeve"));
//        this.playerRenderer = new StatueGeoRenderer(new StatueGeoRenderer.Model(IronsSpellbooks.MODID, "player"));
//        this.playerModel = new StatueGeoRenderer.Model(IronsSpellbooks.MODID, "player");
        this.playerModel = new StaticModel(IronsSpellbooks.MODID, "player");
        this.testModel = new StaticModel(IronsSpellbooks.MODID, "sbeve");
    }

    void setupVanilla(PoseStack poseStack, float blockRotDegrees) {
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(blockRotDegrees));
    }

//    void setupGeckolib(PoseStack poseStack, float blockRotDegrees) {
//        poseStack.translate(0, -0.5, 0);
//        poseStack.translate(0.5, 0, 0.5);
//        poseStack.mulPose(Axis.YP.rotationDegrees(blockRotDegrees));
//        poseStack.translate(-0.5, 0, -0.5);
//    }

    @Override
    public void render(@NotNull StatueBlockEntity statueBlock, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (statueBlock.playerUuid == null) {
            return;
        }
        StatueTextureHolder statueTextureHolder = StatueTextureManager.lookupUUID(statueBlock.playerUuid);
        if (statueTextureHolder == null || statueTextureHolder == StatueTextureManager.NULL) {
            return;
        }
        poseStack.pushPose();
//        setupVanilla(poseStack, RotationSegment.convertToDegrees(statueBlock.getBlockState().getValue(SkullBlock.ROTATION)));
//        PlayerModel<Player> model = statueTextureHolder.slim() ? this.modelSlim : this.model;
//        VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(statueTextureHolder.textureLocation()));
//        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);

        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(RotationSegment.convertToDegrees(statueBlock.getBlockState().getValue(SkullBlock.ROTATION))));
        poseStack.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + partialTick));

        if (false) {
            testModel.render(poseStack, RenderType::armorCutoutNoCull, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }
        RenderType rendertype = RenderType.armorCutoutNoCull(statueTextureHolder.textureLocation());
        playerModel.render(poseStack, rendertype, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

}
