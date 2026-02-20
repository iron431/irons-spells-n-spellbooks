package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.network.gui.SelectStatuePosePacket;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import io.redspace.ironsspellbooks.patreon.statue.PlayerStatuePose;
import io.redspace.ironsspellbooks.patreon.statue.StatueData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StatuePoseScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/statue_pose_screen.png");
    private static final Quaternionf PREVIEW_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, -Mth.PI / 6, Mth.PI);
    private static final Quaternionf OPTION_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, Mth.PI / 6, Mth.PI);

    private static final int PREVIEW_X = 8;
    private static final int PREVIEW_Y = 24;
    private static final int PREVIEW_WIDTH = 108;
    private static final int PREVIEW_HEIGHT = 162;

    private static final int OPTIONS_WINDOW_X = 122;
    private static final int OPTIONS_WINDOW_Y = 24;
    private static final int OPTIONS_WINDOW_WIDTH = 108;
    private static final int OPTIONS_WINDOW_HEIGHT = 162;

    private static final int TITLE_X = 67;
    private static final int TITLE_Y = 8;
    private static final int TITLE_WIDTH = 103;
    private static final int TITLE_HEIGHT = 10;

    private static final int POSE_OPTION_WIDTH = OPTIONS_WINDOW_WIDTH / 3;
    private static final int POSE_OPTION_HEIGHT = OPTIONS_WINDOW_HEIGHT / 3;

    private final BlockPos pos;
    private int leftPos, topPos;
    private final int imageWidth, imageHeight;

    private int scrollOffset;
    private boolean isScrollbarHeld;
    private boolean isDraggingPreview;
    private float previewRotationDegrees;

    private List<PoseOption> poseOptions;

    StatueBlockEntity previewStatue;

    StatueData statueData;
    Button flipPoseButton;

    /* -------------------------------
     * Setup
     * ------------------------------- */
    public StatuePoseScreen(BlockPos pos, StatueData statueData) {
        super(Component.empty());
        this.pos = pos;
        this.imageWidth = 246;
        this.imageHeight = 196;
        this.statueData = statueData;
        this.setupPreviewStatue();
    }

    @Override
    public void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.poseOptions = new ArrayList<>();
        PlayerStatuePose[] poses = PlayerStatuePose.values();
        if (statueData == null) {
            return;
        }
        int selectedPose = -1;
        for (int i = 0; i < poses.length; i++) {
            PlayerStatuePose pose = poses[i];
            int optionsPerRow = 3;
            int x = leftPos + OPTIONS_WINDOW_X + (i % optionsPerRow) * POSE_OPTION_WIDTH;
            int y = topPos + OPTIONS_WINDOW_Y + (i / optionsPerRow) * POSE_OPTION_HEIGHT;
            poseOptions.add(new PoseOption(Button.builder(Component.empty(), button -> {
            }).bounds(x, y, POSE_OPTION_WIDTH, POSE_OPTION_HEIGHT), i, statueData, pose));
            if (this.statueData.pose() == pose) {
                selectedPose = i;
            }
        }
        if (selectedPose >= 0) {
            // attempt to scroll to selected pose
            setScrollOffset(Math.min(selectedPose / 3, getMaxScroll()));
        } else {
            setScrollOffset(scrollOffset);
        }
        flipPoseButton = new Button(Button.builder(Component.empty(), button -> updatePose(statueData.pose(), !statueData.flipped()))
                .bounds(leftPos + PREVIEW_X + PREVIEW_WIDTH - 22, topPos + PREVIEW_Y + PREVIEW_HEIGHT - 22, 22, 22)){
            static final WidgetSprites SPRITES = new WidgetSprites(
                    IronsSpellbooks.id("statue_pose_screen/flip_pose"),
                    IronsSpellbooks.id("statue_pose_screen/flip_pose"),
                    IronsSpellbooks.id("statue_pose_screen/flip_pose_highlighted")
            );
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
        };
    }

    /* -------------------------------
     * Rendering
     * ------------------------------- */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.previewStatue != null) {
            float scale = PREVIEW_WIDTH / 16f * 0.55f;
            Quaternionf rotation = new Quaternionf(PREVIEW_ANGLE).rotateAxis(previewRotationDegrees * Mth.DEG_TO_RAD, new Vector3f(0, 1, 0))/*.rotationY()*/;
            renderStatueInInventory(guiGraphics, leftPos + PREVIEW_X + PREVIEW_WIDTH * 0.5f, topPos + PREVIEW_Y + PREVIEW_HEIGHT * 0.5f, scale, new Vector3f(), rotation, this.previewStatue);
        }
        this.flipPoseButton.render(guiGraphics, mouseX, mouseY, partialTick);
        for (PoseOption option : poseOptions) {
            if (option.visible) {
                option.render(guiGraphics, mouseX, mouseY, partialTick);
                if (option.isHovered()) {
                    setTooltipForNextRenderPass(option.getTooltipList());
                }
            }
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        guiGraphics.blitSprite(IronsSpellbooks.id("transmog_table/scroller"), getScrollBarX(), getScrollBarY(), 6, 27);
        renderPlayerName(guiGraphics);
    }

    private void renderPlayerName(@NotNull GuiGraphics guiGraphics) {
        Component title;
        if (statueData == null) {
            title = Component.translatable("block.irons_spellbooks.player_statue.unknown_player");
        } else {
            title = Component.literal(PatreonHandler.profileFromUUID(statueData.uuid()).username());
        }
        int width = font.width(title.getString());
        float scale = Math.clamp(TITLE_WIDTH * 0.8f / width, 0, 1f);
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.translate(leftPos + TITLE_X + TITLE_WIDTH * 0.5f, topPos + TITLE_Y + TITLE_HEIGHT, 0);
        if (scale < 1) {
            stack.scale(scale, scale, scale);
        }
        guiGraphics.drawString(font, title, -width / 2, -font.lineHeight, -1);
        stack.popPose();
    }

    /* -------------------------------
     * UX Interaction
     * ------------------------------- */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isScrollbarHeld = false;
        isDraggingPreview = false;
        if (flipPoseButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (PoseOption option : poseOptions) {
            if (option.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (mouseX >= getScrollBarX() && mouseX < getScrollBarX() + 6 && mouseY >= getScrollBarY() && mouseY < getScrollBarY() + 27) {
            isScrollbarHeld = true;
            return true;
        }
        if (mouseX >= leftPos + PREVIEW_X && mouseX < leftPos + PREVIEW_X + PREVIEW_WIDTH &&
                mouseY >= topPos + PREVIEW_Y && mouseY < topPos + PREVIEW_Y + PREVIEW_HEIGHT) {
            isDraggingPreview = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        int maxScroll = getMaxScroll();
        int newScroll = Math.clamp(scrollOffset - (int) pScrollY, 0, maxScroll);
        if (newScroll != scrollOffset) {
            setScrollOffset(newScroll);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int max = getMaxScroll();
        if (this.isScrollbarHeld) {
            int scrollZoneMin = topPos + OPTIONS_WINDOW_Y;
            int scrollZoneMax = scrollZoneMin + OPTIONS_WINDOW_HEIGHT;
            var scrollOffs = ((float) pMouseY - (float) scrollZoneMin - 7.5F) / ((float) (scrollZoneMax - scrollZoneMin) - 15.0F);
            scrollOffs = Mth.clamp(scrollOffs, 0.0F, 1.0F);
            int i = Math.max((int) ((double) (scrollOffs * (float) max) + 0.5D), 0);
            if (i != this.scrollOffset) {
                setScrollOffset(i);
            }
            return true;
        }
        if (isDraggingPreview) {
            // dragging from left to right of panel should rotate preview by 180 degrees
            previewRotationDegrees += (float) (pDragX / PREVIEW_WIDTH * 180);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /* -------------------------------
     * Helper
     * ------------------------------- */
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

    private void updatePose(PlayerStatuePose pose, boolean flipped) {
        StatueData current = this.statueData;
        StatueData updated = current.updatePose(pose).updateFlipped(flipped);
        if (current.equals(updated)) {
            return;
        }
        this.statueData = updated;
        setupPreviewStatue();
        PacketDistributor.sendToServer(new SelectStatuePosePacket(this.pos, this.statueData.pose(), this.statueData.flipped()));
    }

    private void setupPreviewStatue() {
        previewStatue = StatueBlockEntity.renderable(this.statueData);
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

    /* -------------------------------
     * Button Subclass
     * ------------------------------- */
    class PoseOption extends Button {
        final int index;
        final int originalY, originalX;
        final StatueBlockEntity entity;
        final PlayerStatuePose pose;

        PoseOption(Builder builder, int index, StatueData statueData, PlayerStatuePose pose) {
            super(builder);
            this.index = index;
            this.originalY = this.getY();
            this.originalX = this.getX();
            this.entity = StatueBlockEntity.renderable(new StatueData(statueData.uuid(), pose, false));
            this.pose = pose;
        }

        @Override
        public void onPress() {
            StatuePoseScreen.this.updatePose(this.pose, StatuePoseScreen.this.statueData.flipped());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            boolean selected = pose == statueData.pose();
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
            float scale = POSE_OPTION_WIDTH / 16f * 0.55f;
            renderStatueInInventory(guiGraphics, getX() + POSE_OPTION_WIDTH * 0.5f, getY() + POSE_OPTION_HEIGHT * 0.5f, scale, new Vector3f(), OPTION_ANGLE, this.entity);
        }

        public List<FormattedCharSequence> getTooltipList() {
            return Stream.of(
                    Component.translatable(pose.descriptionId()),
                    Component.translatable("block.irons_spellbooks.player_statue.pose_guide").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY)
            ).flatMap(component -> Tooltip.splitTooltip(minecraft, component).stream()).toList();
        }

    }
}
