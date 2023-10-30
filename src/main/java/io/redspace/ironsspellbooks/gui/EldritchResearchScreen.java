package io.redspace.ironsspellbooks.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector4f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.network.ServerboundLearnSpell;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class EldritchResearchScreen extends Screen {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/eldritch_research_screen/window.png");
    private static final ResourceLocation FRAME_LOCATION = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/eldritch_research_screen/spell_frame.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;

    int leftPos, topPos;
    InteractionHand activeHand;

    public EldritchResearchScreen(Component pTitle, InteractionHand activeHand) {
        super(pTitle);
        this.activeHand = activeHand;
    }

    List<AbstractSpell> learnableSpells;
    List<SpellNode> nodes;
    SyncedSpellData playerData;
    Vec2 viewportOffset;

    protected void init() {
        learnableSpells = SpellRegistry.getSpellsForSchool(SchoolRegistry.ELDRITCH.get());
        if (this.minecraft != null) {
            playerData = ClientMagicData.getSyncedSpellData(minecraft.player);
        }
        viewportOffset = Vec2.ZERO;
        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = (this.height - WINDOW_HEIGHT) / 2;
        nodes = new ArrayList<>();
        float f = 6.282f / learnableSpells.size();
        RandomSource randomSource = RandomSource.create(431L);
        int gridsize = 64;

        for (int i = 0; i < learnableSpells.size(); i++) {
            float r = 35;
            int x = leftPos + WINDOW_WIDTH / 2 - 8 + (int) (r * Mth.cos(f * i));
            int y = topPos + WINDOW_HEIGHT / 2 - 8 + (int) (r * Mth.sin(f * i));
            nodes.add(new SpellNode(learnableSpells.get(i), x, y));
        }
        for (int i = 0; i < nodes.size(); i++) {

        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        drawBackdrop(leftPos, topPos);
        setTranslucentTexture(WINDOW_LOCATION);
        this.blit(poseStack, leftPos, topPos, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        handleConnections(poseStack, partialTick);
        List<FormattedCharSequence> tooltip = null;
        for (int i = 0; i < nodes.size(); i++) {
            int x = nodes.get(i).x + (int) viewportOffset.x;
            int y = nodes.get(i).y + (int) viewportOffset.y;
            setTexture(nodes.get(i).spell.getSpellIconResource());
            blit(poseStack, x, y, 0, 0, 16, 16, 16, 16);
            setTexture(FRAME_LOCATION);
            blit(poseStack, x - 8, y - 8, 32, 32, nodes.get(i).spell.isLearned(player) ? 32 : 0, 0, 32, 32, 64, 32);
            if (isHovering(x - 2, y - 2, 16 + 4, 16 + 4, mouseX, mouseY)) {
                tooltip = buildTooltip(nodes.get(i).spell, font);
            }
        }
        if (tooltip != null) {
            renderTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }

    private static final Component ALREADY_LEARNED = Component.translatable("ui.irons_spellbooks.research_already_learned").withStyle(ChatFormatting.DARK_AQUA);
    private static final Component UNLEARNED = Component.translatable("ui.irons_spellbooks.research_warning").withStyle(ChatFormatting.RED);

    public static List<FormattedCharSequence> buildTooltip(AbstractSpell spell, Font font) {
        boolean learned = spell.isLearned(Minecraft.getInstance().player);
        var name = spell.getDisplayName(null).withStyle(learned ? ChatFormatting.DARK_AQUA : ChatFormatting.RED);
        var description = font.split(Component.translatable(String.format("%s.guide", spell.getComponentId())).withStyle(ChatFormatting.GRAY), 180);
        var hoverText = new ArrayList<FormattedCharSequence>();
        hoverText.add(FormattedCharSequence.forward(name.getString(), name.getStyle().withUnderlined(true)));
        hoverText.addAll(description);
        hoverText.add(FormattedCharSequence.EMPTY);
        hoverText.add((learned ? ALREADY_LEARNED : UNLEARNED).getVisualOrderText());
        return hoverText;
    }

    private void handleConnections(PoseStack poseStack, float partialTick) {
        fill(poseStack, 0, 0, this.width, this.height, 0);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < nodes.size() - 1; i++) {
            Vec2 a = new Vec2(nodes.get(i).x, nodes.get(i).y);
            Vec2 b = new Vec2(nodes.get(i + 1).x, nodes.get(i + 1).y);
            Vec2 org = new Vec2(-(b.y - a.y), b.x - a.x).normalized().scale(1.5f);

            final double x1m1 = a.x + org.x + 8;
            final double x2m1 = b.x + org.x + 8;
            final double y1m1 = a.y + org.y + 8;
            final double y2m1 = b.y + org.y + 8;

            final double x1m2 = a.x - org.x + 8;
            final double x2m2 = b.x - org.x + 8;
            final double y1m2 = a.y - org.y + 8;
            final double y2m2 = b.y - org.y + 8;

            float f = Mth.sin((Minecraft.getInstance().player.tickCount + partialTick) * .1f);
            float glowIntensity = f * f;
            var color = new Vector4f(135 / 255f, 154 / 255f, 174 / 255f, 0.5f);
            var glowcolor = new Vector4f(244 / 255f, 65 / 255f, 255 / 255f, 0.5f);
            var color1 = lerpColor(color, glowcolor, glowIntensity * (nodes.get(i).spell.isLearned(Minecraft.getInstance().player) ? 1 : 0));
            var color2 = lerpColor(color, glowcolor, glowIntensity * (nodes.get(i + 1).spell.isLearned(Minecraft.getInstance().player) ? 1 : 0));
            buffer.vertex(x1m1 + viewportOffset.x, y1m1 + viewportOffset.y, getBlitOffset()).color(color1.x(), color1.y(), color1.z(), color1.w()).endVertex();
            buffer.vertex(x2m1 + viewportOffset.x, y2m1 + viewportOffset.y, getBlitOffset()).color(color2.x(), color2.y(), color2.z(), color2.w()).endVertex();
            buffer.vertex(x2m2 + viewportOffset.x, y2m2 + viewportOffset.y, getBlitOffset()).color(color2.x(), color2.y(), color2.z(), color2.w()).endVertex();
            buffer.vertex(x1m2 + viewportOffset.x, y1m2 + viewportOffset.y, getBlitOffset()).color(color1.x(), color1.y(), color1.z(), color1.w()).endVertex();
        }

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private int colorFromRGBA(Vector4f rgba) {
        var r = (int) (rgba.x() * 255) & 0xFF;
        var g = (int) (rgba.y() * 255) & 0xFF;
        var b = (int) (rgba.z() * 255) & 0xFF;
        var a = (int) (rgba.w() * 255) & 0xFF;

        return (r << 24) + (g << 16) + (b << 8) + (a);
    }

    private void drawBackdrop(int left, int top) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRendertypeEndPortalShader);
        RenderSystem.setShaderTexture(0, TheEndPortalRenderer.END_PORTAL_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex((float) left, (float) top + EldritchResearchScreen.WINDOW_HEIGHT, 0.0F).color(1, 1, 1, 1).endVertex();
        bufferbuilder.vertex((float) left + EldritchResearchScreen.WINDOW_WIDTH, (float) top + EldritchResearchScreen.WINDOW_HEIGHT, 0.0F).color(1, 1, 1, 1).endVertex();
        bufferbuilder.vertex((float) left + EldritchResearchScreen.WINDOW_WIDTH, (float) top, 0.0F).color(1, 1, 1, 1).endVertex();
        bufferbuilder.vertex((float) left, (float) top, 0.0F).color(1, 1, 1, 1).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    private static Vector4f lerpColor(Vector4f a, Vector4f b, float pDelta) {
        float f = 1.0F - pDelta;
        var x = a.x() * f + b.x() * pDelta;
        var y = a.y() * f + b.y() * pDelta;
        var z = a.z() * f + b.z() * pDelta;
        var w = a.w() * f + b.w() * pDelta;
        return new Vector4f(x, y, z, w);
    }

    boolean isMouseHoldingSpell, isMouseDragging;

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int mouseX = (int) pMouseX;
        int mouseY = (int) pMouseY;
        for (SpellNode node : nodes) {
            if (isHoveringNode(node, mouseX, mouseY)) {
                isMouseHoldingSpell = true;
                Messages.sendToServer(new ServerboundLearnSpell(this.activeHand, node.spell.getSpellId()));
                break;
            }
        }
        if (!isMouseHoldingSpell) {
            if (isHovering(leftPos + WINDOW_INSIDE_X, topPos + WINDOW_INSIDE_Y, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT, mouseX, mouseY)) {
                isMouseDragging = true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean isHoveringNode(SpellNode node, int mouseX, int mouseY) {
        return isHovering(node.x - 2 + (int) viewportOffset.x, node.y - 2 + (int) viewportOffset.y, 16 + 4, 16 + 4, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        isMouseHoldingSpell = false;
        isMouseDragging = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isMouseDragging) {
            this.viewportOffset = this.viewportOffset.add(new Vec2((float) pDragX, (float) pDragY));
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }


    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    record SpellNode(AbstractSpell spell, int x, int y) {
    }

    record NodeConnection(SpellNode node1, SpellNode node2) {
    }

    private static void setTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }

    private static void setTranslucentTexture(ResourceLocation texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }
}
