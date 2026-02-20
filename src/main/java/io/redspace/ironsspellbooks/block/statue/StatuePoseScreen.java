package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.patreon.statue.PlayerStatuePose;
import io.redspace.ironsspellbooks.patreon.statue.StatueData;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogHolder;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogItemData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class StatuePoseScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/statue_pose_screen.png");
    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, -Mth.PI / 6, Mth.PI);

    private static final int PREVIEW_X = 8;
    private static final int PREVIEW_Y = 8;
    private static final int PREVIEW_WIDTH = 108;
    private static final int PREVIEW_HEIGHT = 162;

    private static final int OPTIONS_WINDOW_X = 122;
    private static final int OPTIONS_WINDOW_Y = 8;
    private static final int OPTIONS_WINDOW_WIDTH = 108;
    private static final int OPTIONS_WINDOW_HEIGHT = 162;

    private static final int POSE_OPTION_WIDTH = OPTIONS_WINDOW_WIDTH / 3;
    private static final int POSE_OPTION_HEIGHT = OPTIONS_WINDOW_HEIGHT / 3;

    private final BlockPos pos;
    private int leftPos, topPos;
    private final int imageWidth, imageHeight;

    private int scrollOffset;
    private boolean isScrollbarHeld;

    private List<PoseOption> poseOptions;

    private int getMaxScroll() {
        int optionsPerRow = 3;
        int rowsRequired = (int) Math.ceil(poseOptions.size() / (double) optionsPerRow);
        return Math.max(0, rowsRequired - 3); // can fit 3 rows without scrolling
    }

    private void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        int optionsPerRow = 3;
        int minIndex = scrollOffset * optionsPerRow;
        int optionsPerColumn = 3;
        int maxIndex = minIndex + optionsPerRow * optionsPerColumn;
        for (int i = 0; i < poseOptions.size(); i++) {
            PoseOption option = poseOptions.get(i);
            option.setY(option.originalY - scrollOffset * POSE_OPTION_HEIGHT);
            if (i < minIndex || i >= maxIndex) {
                option.active = false;
                option.visible = false;
            } else {
                option.active = true;
                option.visible = true;
            }
        }
    }

    private int getScrollBarX() {
        return leftPos + OPTIONS_WINDOW_X + OPTIONS_WINDOW_WIDTH + 2;
    }

    private int getScrollBarY() {
        return topPos + OPTIONS_WINDOW_Y + (int) ((scrollOffset / (float) getMaxScroll()) * (OPTIONS_WINDOW_HEIGHT - 27));
    }

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
        float scale = PREVIEW_WIDTH / 16f * 0.55f;
        var quat = new Quaternionf().rotationXYZ(0.43633232F, -Mth.PI / 6, Mth.PI);
        var vec = new Vector3f(0, 0, 0);
        renderStatueInInventory(guiGraphics, leftPos + PREVIEW_X + PREVIEW_WIDTH * 0.5f, topPos + PREVIEW_Y + PREVIEW_HEIGHT * 0.5f, scale, vec, quat, fakeEntity);
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
            StatueBlockEntity statue
    ) {
        scale *= 16;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 50.0);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translate.x, translate.y + 1, translate.z);
        guiGraphics.pose().mulPose(pose);
        guiGraphics.pose().translate(-0.5f, 0, -0.5f);

        Lighting.setupForEntityInInventory();
        BlockEntityRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        RenderSystem.runAsFancy(() -> {
            BlockEntityRenderer<StatueBlockEntity> blockentityrenderer = blockRenderDispatcher.getRenderer(statue);
            if (blockentityrenderer != null) {
                blockentityrenderer.render(statue, DeltaTracker.ZERO.getGameTimeDeltaTicks(), guiGraphics.pose(), guiGraphics.bufferSource(), LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY);
            }
        });
        guiGraphics.flush();
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    class PoseOption extends Button {
        final int index;
        final int originalY, originalX;
        final StatueBlockEntity entity;

        private static ItemStack createStack(Item item, TransmogHolder holder) {
            var stack = new ItemStack(item);
            TransmogItemData.set(stack, new TransmogItemData(holder));
            return stack;
        }

        PoseOption(Builder builder, int index, StatueData statueData, PlayerStatuePose pose) {
            super(builder);
            this.index = index;
            this.originalY = this.getY();
            this.originalX = this.getX();
            this.entity = StatueBlockEntity.renderable(new StatueData(statueData.uuid(), pose));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            boolean selected = false;//menu.selectedTransmogIndex == this.index;
            ResourceLocation frameSprite = IronsSpellbooks.id("transmog_table/transmog_option");
            if (selected) {
                frameSprite = frameSprite.withSuffix("_selected");
            } else if (hovered) {
                frameSprite = frameSprite.withSuffix("_highlighted");
            }
            if (!frameSprite.getPath().endsWith("option")) {
                frameSprite = frameSprite.withPrefix("gui/sprites/");
            }
            guiGraphics.blitSprite(frameSprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        protected void renderArmorPreview(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, LivingEntity armorStand, @Nullable EquipmentSlot equipmentSlot) {
            float scale = this.getWidth() / 16f * 9.5f;
            guiGraphics.pose().pushPose();

            InventoryScreen.renderEntityInInventory(guiGraphics, this.getX() + this.getWidth() / 2f, this.getY() + this.getHeight() / 2f, scale,
                    new Vector3f(0f, 0.97f, 0f),
                    ARMOR_STAND_ANGLE, null, armorStand);
            guiGraphics.pose().popPose();
            render(guiGraphics, mouseX, mouseY, partialTick);
        }

        public List<Component> getTooltip(LocalPlayer player) {
            List<Component> list = new ArrayList<>();
            //todo:tooltip?
            return list;
        }

    }
}
