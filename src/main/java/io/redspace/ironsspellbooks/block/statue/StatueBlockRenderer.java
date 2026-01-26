package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.statue.PlayerStatueModelType;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureHolder;
import io.redspace.ironsspellbooks.patreon.statue.StatueTextureManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StatueBlockRenderer implements BlockEntityRenderer<StatueBlockEntity> {

    StaticModel playerModel, testModel;
    private static final Map<PlayerStatueModelType, StaticModel> PLAYER_MODELS = Map.of(
            PlayerStatueModelType.WIDE, new StaticModel(IronsSpellbooks.MODID, "player"),
            PlayerStatueModelType.SLIM, new StaticModel(IronsSpellbooks.MODID, "player_slim"),
            PlayerStatueModelType.LEGACY, new StaticModel(IronsSpellbooks.MODID, "player_legacy")
    );

    public StatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.playerModel = new StaticModel(IronsSpellbooks.MODID, "player");
        this.testModel = //new StaticModel(IronsSpellbooks.MODID, "sbeve");
                new StaticModel(IronsSpellbooks.id("geo/tyros.geo.json"), IronsSpellbooks.id("textures/entity/statue/tyros.png"));
    }

    @Override
    public void render(@NotNull StatueBlockEntity statueBlock, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (statueBlock.playerUuid == null) {
            return;
        }
        StatueTextureHolder statueTextureHolder = StatueTextureManager.lookupUUID(statueBlock.playerUuid);
        if (statueTextureHolder == null || statueTextureHolder == StatueTextureManager.NULL) {
            //todo: render broken statue or other default asset
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(RotationSegment.convertToDegrees(statueBlock.getBlockState().getValue(SkullBlock.ROTATION))));

        if (false) {
            testModel.render(poseStack, RenderType::armorCutoutNoCull, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }
        RenderType rendertype = RenderType.armorCutoutNoCull(statueTextureHolder.textureLocation());
        StaticModel playerModel = PLAYER_MODELS.get(statueTextureHolder.modelType());
        playerModel.render(poseStack, rendertype, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

}
