package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Vector4f;

public class SpellWheelOverlay implements IGuiOverlay {
    public static SpellWheelOverlay instance = new SpellWheelOverlay();

    public final static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/icons.png");

    private final Vector4f lineColor = new Vector4f(1f, .85f, .7f, 1f);
    private final Vector4f radialButtonColor = new Vector4f(.04f, .03f, .01f, .6f);
    private final Vector4f highlightColor = new Vector4f(.8f, .7f, .55f, .7f);

    private final double ringInnerEdge = 20;
    private double ringOuterEdge = 80;
    private final double ringOuterEdgeMax = 80;
    private final double ringOuterEdgeMin = 65;

    public boolean active;
    private int wheelSelection;
    private SpellSelectionManager swsm;

    public void open() {
        active = true;
        wheelSelection = -1;
        Minecraft.getInstance().mouseHandler.releaseMouse();

    }

    public void close() {
        active = false;

        if (wheelSelection >= 0) {
            swsm.makeSelection(wheelSelection);
        }

        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    public void render(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight) {
        if (!active)
            return;

        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.screen != null || minecraft.mouseHandler.isMouseGrabbed()) {
            close();
            return;
        }

        swsm = ClientMagicData.getSpellSelectionManager();
        int totalSpellsAvailable = swsm.getSpellCount();

        if (totalSpellsAvailable <= 0) {
            close();
            return;
        }
        PoseStack poseStack = guiHelper.pose();
        poseStack.pushPose();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        Vec2 screenCenter = new Vec2(minecraft.getWindow().getScreenWidth() * .5f, minecraft.getWindow().getScreenHeight() * .5f);
        Vec2 mousePos = new Vec2((float) minecraft.mouseHandler.xpos(), (float) minecraft.mouseHandler.ypos());
        double radiansPerSpell = Math.toRadians(360 / (float) totalSpellsAvailable);

        float mouseRotation = (Utils.getAngle(mousePos, screenCenter) + 1.570f + (float) radiansPerSpell * .5f) % 6.283f;

        wheelSelection = (int) Mth.clamp(mouseRotation / radiansPerSpell, 0, totalSpellsAvailable - 1);
        if (mousePos.distanceToSqr(screenCenter) < ringOuterEdgeMin * ringOuterEdgeMin) {
            wheelSelection = Math.max(0, swsm.getSelectionIndex());
        }

        guiHelper.fill(0, 0, screenWidth, screenHeight, 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        drawRadialBackgrounds(buffer, centerX, centerY, wheelSelection);
        drawDividingLines(buffer, centerX, centerY);

        tesselator.end();
        RenderSystem.disableBlend();

        //Text background
        var selectedSpell = swsm.getSpellData(wheelSelection);
        var spellLevel = selectedSpell.getSpell().getLevelFor(selectedSpell.getLevel(), player);
        var font = gui.getFont();
        var info = selectedSpell.getSpell().getUniqueInfo(spellLevel, minecraft.player);
        int textHeight = Math.max(2, info.size()) * font.lineHeight + 5;
        int textCenterMargin = 5;
        int textTitleMargin = 5;
        var title = selectedSpell.getSpell().getDisplayName(minecraft.player).withStyle(Style.EMPTY.withUnderlined(true));
        var level = Component.translatable("ui.irons_spellbooks.level", TooltipsUtils.getLevelComponenet(selectedSpell, player).withStyle(selectedSpell.getSpell().getRarity(spellLevel).getDisplayName().getStyle()));
        var mana = Component.translatable("ui.irons_spellbooks.mana_cost", selectedSpell.getSpell().getManaCost(spellLevel)).withStyle(ChatFormatting.AQUA);
//            selectedSpell.getUniqueInfo(minecraft.player).forEach((line) -> lines.add(line.withStyle(ChatFormatting.DARK_GREEN)));

        drawTextBackground(guiHelper, centerX, centerY, ringOuterEdge + textHeight - textTitleMargin - font.lineHeight, textCenterMargin, Math.max(2, info.size()) * font.lineHeight);
        guiHelper.drawString(font, title, (int) (centerX - font.width(title) / 2), (int) (centerY - (ringOuterEdge + textHeight)), 0xFFFFFF, true);
        guiHelper.drawString(font, level, (int) (centerX - font.width(level) - textCenterMargin), (int) (centerY - (ringOuterEdge + textHeight) + font.lineHeight + textTitleMargin), 0xFFFFFF, true);
        guiHelper.drawString(font, mana, (int) (centerX - font.width(mana) - textCenterMargin), (int) (centerY - (ringOuterEdge + textHeight) + font.lineHeight * 2 + textTitleMargin), 0xFFFFFF, true);

        for (int i = 0; i < info.size(); i++) {
            var line = info.get(i);
            guiHelper.drawString(font, line, (int) (centerX + textCenterMargin), (int) (centerY - (ringOuterEdgeMax + textHeight) + font.lineHeight * (i + 1) + textTitleMargin), 0x3be33b, true);
        }

        //Spell Icons
        float scale = Mth.lerp(totalSpellsAvailable / 15f, 2, 1.25f) * .65f;
        double radius = 3 / scale * (ringInnerEdge + ringInnerEdge) * .5 * (.85f + .25f * (totalSpellsAvailable / 15f));
        Vec2[] locations = new Vec2[totalSpellsAvailable];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = new Vec2((float) (Math.sin(radiansPerSpell * i) * radius), (float) (-Math.cos(radiansPerSpell * i) * radius));
        }
        for (int i = 0; i < locations.length; i++) {
            var spell = swsm.getSpellData(i);
            if (spell != null) {
                var texture = spell.getSpell().getSpellIconResource();
                poseStack.pushPose();
                poseStack.translate(centerX, centerY, 0);
                poseStack.scale(scale, scale, scale);

                //Icon
                int iconWidth = 16 / 2;
                int borderWidth = 32 / 2;
                int cdWidth = 16 / 2;
                //blit(poseStack, centerX + (int) locations[i].x + 3, centerY + (int) locations[i].y + 3, 0, 0, 16, 16, 16, 16);
                guiHelper.blit(texture, (int) locations[i].x - iconWidth, (int) locations[i].y - iconWidth, 0, 0, 16, 16, 16, 16);
                /*
                Border
                 */
                guiHelper.blit(TEXTURE, (int) locations[i].x - borderWidth, (int) locations[i].y - borderWidth, swsm.getSelectionIndex() == i ? 32 : 0, 106, 32, 32);
                /*
                Cooldown
                 */
                float f = ClientMagicData.getCooldownPercent(spell.getSpell());
                if (f > 0) {
                    RenderSystem.enableBlend();
                    int pixels = (int) (16 * f + 1f);
//                    gui.blit(poseStack, centerX + (int) locations[i].x + 3, centerY + (int) locations[i].y + 19 - pixels, 47, 87, 16, pixels);
                    guiHelper.blit(TEXTURE, (int) locations[i].x - cdWidth, (int) locations[i].y + cdWidth - pixels, 47, 87, 16, pixels);
                }
                poseStack.popPose();
            }
        }


        poseStack.popPose();
    }

    private void drawTextBackground(GuiGraphics guiHelper, double centerX, double centerY, double textYOffset, int textCenterMargin, int textHeight) {
        guiHelper.fill(0, 0, (int) (centerX * 2), (int) (centerY * 2), 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        centerY = centerY - textYOffset - 2;
        int heightMax = textHeight / 2 + 4;
        int heightMin = 0;
        int widthMax = 70;
        int widthMin = 0;

        widthMin = -1;
        widthMax = 1;
        buffer.vertex(centerX + widthMin, centerY + heightMin, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();
        buffer.vertex(centerX + widthMin, centerY + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
        buffer.vertex(centerX + widthMax, centerY + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
        buffer.vertex(centerX + widthMax, centerY + heightMin, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();

        buffer.vertex(centerX + widthMin, centerY + heightMin + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
        buffer.vertex(centerX + widthMin, centerY + heightMax + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();
        buffer.vertex(centerX + widthMax, centerY + heightMax + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();
        buffer.vertex(centerX + widthMax, centerY + heightMin + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
        buffer.vertex(centerX + widthMin, centerY + heightMin + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
        buffer.vertex(centerX + widthMin, centerY + heightMax + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();
        buffer.vertex(centerX + widthMax, centerY + heightMax + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();
        buffer.vertex(centerX + widthMax, centerY + heightMin + heightMax, 0).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
//
//        buffer.vertex(centerX - widthMax, centerY - heightMax, getBlitOffset()).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();
//        buffer.vertex(centerX - widthMax, centerY - heightMin, getBlitOffset()).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
//        buffer.vertex(centerX + widthMin, centerY - heightMin, getBlitOffset()).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w()).endVertex();
//        buffer.vertex(centerX + widthMin, centerY - heightMax, getBlitOffset()).color(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0).endVertex();


        tesselator.end();
        RenderSystem.disableBlend();
    }

    private void drawRadialBackgrounds(BufferBuilder buffer, double centerX, double centerY, int selectedSpellIndex) {
        double quarterCircle = Math.PI / 2;
        int totalSpellsAvailable = swsm.getSpellCount();
        int segments;
        if (totalSpellsAvailable < 6) {
            segments = totalSpellsAvailable % 2 == 1 ? 15 : 12;
        } else {
            segments = totalSpellsAvailable * 2;
        }
        double radiansPerObject = 2 * Math.PI / segments;
        double radiansPerSpell = 2 * Math.PI / totalSpellsAvailable;
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax);
        for (int i = 0; i < segments; i++) {
            final double beginRadians = i * radiansPerObject - (quarterCircle + (radiansPerSpell / 2));
            final double endRadians = (i + 1) * radiansPerObject - (quarterCircle + (radiansPerSpell / 2));

            final double x1m1 = Math.cos(beginRadians) * ringInnerEdge;
            final double x2m1 = Math.cos(endRadians) * ringInnerEdge;
            final double y1m1 = Math.sin(beginRadians) * ringInnerEdge;
            final double y2m1 = Math.sin(endRadians) * ringInnerEdge;

            final double x1m2 = Math.cos(beginRadians) * ringOuterEdge;
            final double x2m2 = Math.cos(endRadians) * ringOuterEdge;
            final double y1m2 = Math.sin(beginRadians) * ringOuterEdge;
            final double y2m2 = Math.sin(endRadians) * ringOuterEdge;

            boolean isHighlighted = (i * totalSpellsAvailable) / segments == selectedSpellIndex;

            Vector4f color = radialButtonColor;
            if (isHighlighted) color = highlightColor;

            buffer.vertex(centerX + x1m1, centerY + y1m1, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m2, centerY + y2m2, 0).color(color.x(), color.y(), color.z(), 0).endVertex();
            buffer.vertex(centerX + x1m2, centerY + y1m2, 0).color(color.x(), color.y(), color.z(), 0).endVertex();

            //Category line
            color = lineColor;
            double categoryLineWidth = 2;
            final double categoryLineOuterEdge = ringInnerEdge + categoryLineWidth;

            final double x1m3 = Math.cos(beginRadians) * categoryLineOuterEdge;
            final double x2m3 = Math.cos(endRadians) * categoryLineOuterEdge;
            final double y1m3 = Math.sin(beginRadians) * categoryLineOuterEdge;
            final double y2m3 = Math.sin(endRadians) * categoryLineOuterEdge;

            buffer.vertex(centerX + x1m1, centerY + y1m1, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m3, centerY + y2m3, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x1m3, centerY + y1m3, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();

        }
    }

    private void drawDividingLines(BufferBuilder buffer, double centerX, double centerY) {
        int totalSpellsAvailable = swsm.getSpellCount();

        if (totalSpellsAvailable <= 1)
            return;

        double quarterCircle = Math.PI / 2;
        double radiansPerSpell = 2 * Math.PI / totalSpellsAvailable;
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax);

        for (int i = 0; i < totalSpellsAvailable; i++) {
            final double closeWidth = 8 * Mth.DEG_TO_RAD;
            final double farWidth = closeWidth / 4;
            final double beginCloseRadians = i * radiansPerSpell - (quarterCircle + (radiansPerSpell / 2)) - (closeWidth / 4);
            final double endCloseRadians = beginCloseRadians + closeWidth;
            final double beginFarRadians = i * radiansPerSpell - (quarterCircle + (radiansPerSpell / 2)) - (farWidth / 4);
            final double endFarRadians = beginCloseRadians + farWidth;

            final double x1m1 = Math.cos(beginCloseRadians) * ringInnerEdge;
            final double x2m1 = Math.cos(endCloseRadians) * ringInnerEdge;
            final double y1m1 = Math.sin(beginCloseRadians) * ringInnerEdge;
            final double y2m1 = Math.sin(endCloseRadians) * ringInnerEdge;

            final double x1m2 = Math.cos(beginFarRadians) * ringOuterEdge * 1.4;
            final double x2m2 = Math.cos(endFarRadians) * ringOuterEdge * 1.4;
            final double y1m2 = Math.sin(beginFarRadians) * ringOuterEdge * 1.4;
            final double y2m2 = Math.sin(endFarRadians) * ringOuterEdge * 1.4;

            Vector4f color = lineColor;
            buffer.vertex(centerX + x1m1, centerY + y1m1, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, 0).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m2, centerY + y2m2, 0).color(color.x(), color.y(), color.z(), 0).endVertex();
            buffer.vertex(centerX + x1m2, centerY + y1m2, 0).color(color.x(), color.y(), color.z(), 0).endVertex();
        }

    }

    private void setOpaqueTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }

    private void setTranslucentTexture(ResourceLocation texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }

    private boolean inTriangle(final double x1, final double y1, final double x2, final double y2,
                               final double x3, final double y3, final double x, final double y) {
        final double ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        final double bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        final double ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return sign(ab) == sign(bc) && sign(bc) == sign(ca);
    }

    private int sign(final double n) {
        return n > 0 ? 1 : -1;
    }
}
