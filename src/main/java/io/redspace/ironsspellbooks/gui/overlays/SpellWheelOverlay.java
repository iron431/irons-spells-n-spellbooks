package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SpellWheelOverlay implements LayeredDraw.Layer {
    public static SpellWheelOverlay instance = new SpellWheelOverlay();

    public final static ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/icons.png");

    private final Vector4f lineColor = new Vector4f(1f, .85f, .7f, 1f);
    private final Vector4f radialButtonColor = new Vector4f(.04f, .03f, .01f, .6f);
    private final Vector4f highlightColor = new Vector4f(.8f, .7f, .55f, .7f);

    private final float ringInnerEdge = 20;
    private float ringOuterEdge = 80;
    private final float ringOuterEdgeMax = 80;
    private final float ringOuterEdgeMin = 65;

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

    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui || Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();
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
        //RenderSystem.enableBlend();
        //RenderSystem.defaultBlendFunc();
        //final Tesselator tesselator = Tesselator.getInstance();
        //final BufferBuilder buffer = tesselator.getBuilder();
        //buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        drawRadialBackgrounds(guiHelper, centerX, centerY, wheelSelection);
        drawDividingLines(guiHelper, centerX, centerY);

        //tesselator.end();
        //RenderSystem.disableBlend();

        //Text background
        var selectedSpell = swsm.getSpellData(wheelSelection);
        var spellLevel = selectedSpell.getSpell().getLevelFor(selectedSpell.getLevel(), player);
        var font = Minecraft.getInstance().font;
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

    private void drawTextBackground(GuiGraphics guiHelper, float centerX, float centerY, float textYOffset, int textCenterMargin, int textHeight) {
        guiHelper.fill(0, 0, (int) (centerX * 2), (int) (centerY * 2), 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //final Tesselator tesselator = Tesselator.getInstance();
        //final BufferBuilder buffer = tesselator.getBuilder();
        //buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        centerY = centerY - textYOffset - 2;
        int heightMax = textHeight / 2 + 4;
        int heightMin = 0;
        int widthMax = 70;
        int widthMin = 0;

        widthMin = -1;
        widthMax = 1;

        final VertexConsumer vertexConsumer = guiHelper.bufferSource().getBuffer(RenderType.gui());
        Matrix4f m = guiHelper.pose().last().pose();
        vertexConsumer.addVertex(m, centerX + widthMin, centerY + heightMin, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);
        vertexConsumer.addVertex(m, centerX + widthMin, centerY + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
        vertexConsumer.addVertex(m, centerX + widthMax, centerY + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
        vertexConsumer.addVertex(m, centerX + widthMax, centerY + heightMin, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);

        vertexConsumer.addVertex(m, centerX + widthMin, centerY + heightMin + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
        vertexConsumer.addVertex(m, centerX + widthMin, centerY + heightMax + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);
        vertexConsumer.addVertex(m, centerX + widthMax, centerY + heightMax + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);
        vertexConsumer.addVertex(m, centerX + widthMax, centerY + heightMin + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
        vertexConsumer.addVertex(m, centerX + widthMin, centerY + heightMin + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
        vertexConsumer.addVertex(m, centerX + widthMin, centerY + heightMax + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);
        vertexConsumer.addVertex(m, centerX + widthMax, centerY + heightMax + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);
        vertexConsumer.addVertex(m, centerX + widthMax, centerY + heightMin + heightMax, 0f).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
//
//        buffer.vertex(centerX - widthMax, centerY - heightMax, getBlitOffset()).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);
//        buffer.vertex(centerX - widthMax, centerY - heightMin, getBlitOffset()).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
//        buffer.vertex(centerX + widthMin, centerY - heightMin, getBlitOffset()).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), radialButtonColor.w());
//        buffer.vertex(centerX + widthMin, centerY - heightMax, getBlitOffset()).setColor(radialButtonColor.x(), radialButtonColor.y(), radialButtonColor.z(), 0);


        //tesselator.end();
        //FIXME: 1.21: still necessary after removal of buffer builder? still necessary post 1.20 at all?
        RenderSystem.disableBlend();
    }

    private void drawRadialBackgrounds(GuiGraphics guiGraphics, float centerX, float centerY, int selectedSpellIndex) {
        float quarterCircle = Mth.HALF_PI;
        int totalSpellsAvailable = swsm.getSpellCount();
        int segments;
        if (totalSpellsAvailable < 6) {
            segments = totalSpellsAvailable % 2 == 1 ? 15 : 12;
        } else {
            segments = totalSpellsAvailable * 2;
        }
        float radiansPerObject = 2 * Mth.PI / segments;
        float radiansPerSpell = 2 * Mth.PI / totalSpellsAvailable;
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax);
        for (int i = 0; i < segments; i++) {
            final float beginRadians = i * radiansPerObject - (quarterCircle + (radiansPerSpell / 2));
            final float endRadians = (i + 1) * radiansPerObject - (quarterCircle + (radiansPerSpell / 2));

            final float x1m1 = Mth.cos(beginRadians) * ringInnerEdge;
            final float x2m1 = Mth.cos(endRadians) * ringInnerEdge;
            final float y1m1 = Mth.sin(beginRadians) * ringInnerEdge;
            final float y2m1 = Mth.sin(endRadians) * ringInnerEdge;

            final float x1m2 = Mth.cos(beginRadians) * ringOuterEdge;
            final float x2m2 = Mth.cos(endRadians) * ringOuterEdge;
            final float y1m2 = Mth.sin(beginRadians) * ringOuterEdge;
            final float y2m2 = Mth.sin(endRadians) * ringOuterEdge;

            boolean isHighlighted = (i * totalSpellsAvailable) / segments == selectedSpellIndex;

            Vector4f color = radialButtonColor;
            if (isHighlighted) color = highlightColor;

            final VertexConsumer vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
            final Matrix4f m = guiGraphics.pose().last().pose();

            vertexConsumer.addVertex(m, centerX + x1m1, centerY + y1m1, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x2m1, centerY + y2m1, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x2m2, centerY + y2m2, 0).setColor(color.x(), color.y(), color.z(), 0);
            vertexConsumer.addVertex(m, centerX + x1m2, centerY + y1m2, 0).setColor(color.x(), color.y(), color.z(), 0);

            //Category line
            color = lineColor;
            float categoryLineWidth = 2;
            final float categoryLineOuterEdge = ringInnerEdge + categoryLineWidth;

            final float x1m3 = Mth.cos(beginRadians) * categoryLineOuterEdge;
            final float x2m3 = Mth.cos(endRadians) * categoryLineOuterEdge;
            final float y1m3 = Mth.sin(beginRadians) * categoryLineOuterEdge;
            final float y2m3 = Mth.sin(endRadians) * categoryLineOuterEdge;

            vertexConsumer.addVertex(m, centerX + x1m1, centerY + y1m1, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x2m1, centerY + y2m1, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x2m3, centerY + y2m3, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x1m3, centerY + y1m3, 0).setColor(color.x(), color.y(), color.z(), color.w());

        }
    }

    private void drawDividingLines(GuiGraphics guiHelper, float centerX, float centerY) {
        int totalSpellsAvailable = swsm.getSpellCount();

        if (totalSpellsAvailable <= 1)
            return;

        float quarterCircle = Mth.HALF_PI;
        float radiansPerSpell = 2 * Mth.PI / totalSpellsAvailable;
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax);

        for (int i = 0; i < totalSpellsAvailable; i++) {
            final float closeWidth = 8 * Mth.DEG_TO_RAD;
            final float farWidth = closeWidth / 4;
            final float beginCloseRadians = i * radiansPerSpell - (quarterCircle + (radiansPerSpell / 2)) - (closeWidth / 4);
            final float endCloseRadians = beginCloseRadians + closeWidth;
            final float beginFarRadians = i * radiansPerSpell - (quarterCircle + (radiansPerSpell / 2)) - (farWidth / 4);
            final float endFarRadians = beginCloseRadians + farWidth;

            final float x1m1 = Mth.cos(beginCloseRadians) * ringInnerEdge;
            final float x2m1 = Mth.cos(endCloseRadians) * ringInnerEdge;
            final float y1m1 = Mth.sin(beginCloseRadians) * ringInnerEdge;
            final float y2m1 = Mth.sin(endCloseRadians) * ringInnerEdge;

            final float x1m2 = Mth.cos(beginFarRadians) * ringOuterEdge * 1.4f;
            final float x2m2 = Mth.cos(endFarRadians) * ringOuterEdge * 1.4f;
            final float y1m2 = Mth.sin(beginFarRadians) * ringOuterEdge * 1.4f;
            final float y2m2 = Mth.sin(endFarRadians) * ringOuterEdge * 1.4f;

            Vector4f color = lineColor;
            final VertexConsumer vertexConsumer = guiHelper.bufferSource().getBuffer(RenderType.gui());
            Matrix4f m = guiHelper.pose().last().pose();

            vertexConsumer.addVertex(m, centerX + x1m1, centerY + y1m1, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x2m1, centerY + y2m1, 0).setColor(color.x(), color.y(), color.z(), color.w());
            vertexConsumer.addVertex(m, centerX + x2m2, centerY + y2m2, 0).setColor(color.x(), color.y(), color.z(), 0);
            vertexConsumer.addVertex(m, centerX + x1m2, centerY + y1m2, 0).setColor(color.x(), color.y(), color.z(), 0);
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
