package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class StatuePoseScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/statue_pose_screen.png");
    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, -Mth.PI / 6, Mth.PI);

    final BlockPos pos;
    int leftPos, topPos;
    int imageWidth, imageHeight;

    public StatuePoseScreen(BlockPos pos) {
        super(Component.empty());
        this.pos = pos;
        this.imageWidth = 246;
        this.imageHeight = 178;
    }

    @Override
    public void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!(minecraft.level.getBlockEntity(pos) instanceof StatueBlockEntity realStatue)) return;
        StatueBlockEntity fakeEntity = StatueBlockEntity.renderable(realStatue.getStatueData());
        float scale = 4f;
        var quat = new Quaternionf().rotationXYZ(0.43633232F, -Mth.PI / 6 , Mth.PI);
        var vec = new Vector3f(1, 2.325f, 0);
        renderStatueInInventory(guiGraphics, leftPos, topPos, scale, vec, quat, null, fakeEntity);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    public static void renderStatueInInventory(
            GuiGraphics guiGraphics,
            float x,
            float y,
            float scale,
            Vector3f translate,
            Quaternionf pose,
            @Nullable Quaternionf cameraOrientation,
            StatueBlockEntity statue
    ) {
        scale *= 16;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 50.0);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translate.x, translate.y, translate.z);
        guiGraphics.pose().mulPose(pose);
        guiGraphics.pose().translate(-0.5f, 0, -0.5f);

        Lighting.setupForEntityInInventory();
//        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        BlockEntityRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
//        if (cameraOrientation != null) {
//            entityrenderdispatcher.overrideCameraOrientation(cameraOrientation.conjugate(new Quaternionf()).rotateY((float) Math.PI));
//        }

//        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
//            entityrenderdispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880)
            BlockEntityRenderer<StatueBlockEntity> blockentityrenderer = blockRenderDispatcher.getRenderer(statue);
            if (blockentityrenderer != null) {
                blockentityrenderer.render(statue, DeltaTracker.ZERO.getGameTimeDeltaTicks(), guiGraphics.pose(), guiGraphics.bufferSource(), LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY);
            }
//            blockRenderDispatcher.render(statue, DeltaTracker.ZERO.getGameTimeDeltaTicks(), guiGraphics.pose(), guiGraphics.bufferSource());
        });
        guiGraphics.flush();
//        entityrenderdispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }
}
