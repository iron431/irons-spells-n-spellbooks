package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureHolder;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureManager;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class StatueBlockRenderer implements BlockEntityRenderer<StatueBlockEntity> {

    PlayerModel<Player> model, modelSlim;

    public StatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.model.young = false;
        this.modelSlim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.modelSlim.young = false;
    }

    @Override
    public void render(@NotNull StatueBlockEntity statueBlock, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        StatueTextureHolder statueTextureHolder = StatueTextureManager.getTexture(StatueTextureManager.TEST_UUID);
        assert statueTextureHolder != null;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        PlayerModel<Player> model = statueTextureHolder.slim() ? this.modelSlim : this.model;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(statueTextureHolder.textureLocation()));
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);

        {
            poseStack.translate(1.5, 0, 0);
            consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(StatueTextureManager.getTexture(StatueTextureManager.TEST_UUID2).textureLocation()));
            model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);
        }
        {
            poseStack.translate(1.5, 0, 0);
            consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(StatueTextureManager.getTexture(StatueTextureManager.TEST_UUID3).textureLocation()));
            model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);
        }

        poseStack.popPose();
    }

}
