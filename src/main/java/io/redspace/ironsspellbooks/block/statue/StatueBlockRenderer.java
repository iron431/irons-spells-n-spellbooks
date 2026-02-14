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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class StatueBlockRenderer implements BlockEntityRenderer<StatueBlockEntity> {

    StaticModel testModel, nullModel, statueBaseModel;
    private static final Map<PlayerStatueModelType, StaticModel> PLAYER_MODELS = Map.of(
            PlayerStatueModelType.WIDE, new StaticModel(IronsSpellbooks.MODID, "player"),
            PlayerStatueModelType.SLIM, new StaticModel(IronsSpellbooks.MODID, "player_slim"),
            PlayerStatueModelType.LEGACY, new StaticModel(IronsSpellbooks.MODID, "player_legacy")
    );

    public StatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.testModel = //new StaticModel(IronsSpellbooks.MODID, "sbeve");
                new StaticModel(IronsSpellbooks.id("geo/tyros.geo.json"), IronsSpellbooks.id("textures/entity/statue/tyros.png"));
        this.nullModel = new StaticModel(IronsSpellbooks.MODID, "sbeve");
        this.statueBaseModel = new StaticModel(IronsSpellbooks.MODID, "statue_base");

    }

    @Override
    public void render(@NotNull StatueBlockEntity statueBlock, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(!statueBlock.isPrimary()) return;
        if(!(statueBlock.getBlockState().getBlock() instanceof StatueBlock statue)) return;
        poseStack.pushPose();
        poseStack.translate(statue.xSize * .5f, 0, statue.zSize * 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(statueBlock.getBlockState().getValue(SkullBlock.ROTATION))));
        statueBaseModel.render(poseStack, RenderType::entityCutout, bufferSource, packedLight, packedOverlay);
        float statueBaseHeight = 2 / 16f;
        poseStack.translate(0, statueBaseHeight, 0);

        if (false) {
            testModel.render(poseStack, RenderType::entityCutout, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }
        StatueTextureHolder statueTextureHolder = resolvePlayerStatue(statueBlock.getPlayerUuid());
        if (statueTextureHolder == StatueTextureManager.NULL) {
            nullModel.render(poseStack, RenderType::entityCutout, bufferSource, packedLight, packedOverlay);
        } else {
            var playermodel = PLAYER_MODELS.get(statueTextureHolder.modelType());
            RenderType rendertype = RenderType.entityCutout(statueTextureHolder.textureLocation());
            playermodel.render(poseStack, rendertype, bufferSource, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private @NotNull StatueTextureHolder resolvePlayerStatue(@Nullable UUID playerUuid) {
        if (playerUuid == null) {
            return StatueTextureManager.NULL;
        }
        return StatueTextureManager.lookupUUID(playerUuid);
    }

}
