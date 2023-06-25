package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector4f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.gui.overlays.network.ServerboundSetSpellBookActiveIndex;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SpellWheelOverlay extends GuiComponent {
    public static SpellWheelOverlay instance = new SpellWheelOverlay();

    public final static ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/icons.png");
    public final static ResourceLocation WHEEL = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/spell_wheel.png");

    private final Vector4f lineColor = new Vector4f(1f, .85f, .7f, 1f);
    private final Vector4f radialButtonColor = new Vector4f(.04f, .03f, .01f, .6f);
    private final Vector4f highlightColor = new Vector4f(.8f, .7f, .55f, .7f);
//    private final Vector4f selectedColor = new Vector4f(0f, .5f, 1f, .5f);
//    private final Vector4f highlightSelectedColor = new Vector4f(0.2f, .7f, 1f, .7f);

    private final double ringInnerEdge = 20;
    private double ringOuterEdge = 80;
    private final double ringOuterEdgeMax = 80;
    private final double ringOuterEdgeMin = 65;
    private final double categoryLineWidth = 2;

    public boolean active;
    private int selection;
    private int selectedSpellIndex;
    private SpellBookData spellBookData;

    public void open() {
        active = true;
        selection = -1;
        selectedSpellIndex = -1;
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    public void close() {
        active = false;
        if (selectedSpellIndex >= 0) {
            Messages.sendToServer(new ServerboundSetSpellBookActiveIndex(selectedSpellIndex));
        }
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!active)
            return;

        var minecraft = Minecraft.getInstance();

        if ((minecraft.player == null || minecraft.screen != null || minecraft.mouseHandler.isMouseGrabbed() || !Utils.isPlayerHoldingSpellBook(minecraft.player))) {
            close();
            return;
        }

        poseStack.pushPose();

        Player player = minecraft.player;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        ItemStack spellBookStack = player.getMainHandItem().getItem() instanceof SpellBook ? player.getMainHandItem() : player.getOffhandItem();
        spellBookData = SpellBookData.getSpellBookData(spellBookStack);
        List<AbstractSpell> spells = spellBookData.getActiveInscribedSpells();
        int spellCount = spells.size();
        if (spellCount == 0) {
            close();
            return;
        }

        Vec2 screenCenter = new Vec2(minecraft.getWindow().getScreenWidth() * .5f, minecraft.getWindow().getScreenHeight() * .5f);
        Vec2 mousePos = new Vec2((float) minecraft.mouseHandler.xpos(), (float) minecraft.mouseHandler.ypos());
        double radiansPerSpell = Math.toRadians(360 / (float) spellCount);

        float mouseRotation = (Utils.getAngle(mousePos, screenCenter) + 1.570f + (float) radiansPerSpell * .5f) % 6.283f;

        selection = (int) Mth.clamp(mouseRotation / radiansPerSpell, 0, spellCount - 1);
        if (mousePos.distanceToSqr(screenCenter) < ringOuterEdgeMin * ringOuterEdgeMin)
            selection = Math.max(0, spellBookData.getActiveSpellIndex());
        var currentSpell = spells.get(selection);
        selectedSpellIndex = ArrayUtils.indexOf(spellBookData.getInscribedSpells(), currentSpell);

//        int mouseXX = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
//        int mouseYY = (int) (minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());
//        final double mouseXCenter = mouseXX - centerX;
//        final double mouseYCenter = mouseYY - centerY;
//        double mouseRadians = Math.atan2(mouseYCenter, mouseXCenter);

        fill(poseStack, 0, 0, screenWidth, screenHeight, 0);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder buffer = tesselator.getBuilder();
        final double quarterCircle = Math.PI / 2; //Original

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawRadialBackgrounds(buffer, centerX, centerY, selection, spells);
        drawDividingLines(buffer, centerX, centerY, spells);

        float scale = Mth.clamp(1 + (15 - spellCount) / 15f, 1, 2) * .65f;
        double radius = 3 / scale * (ringInnerEdge + ringInnerEdge) * .5 * (.85f + .15f * (spells.size() / 15f));

        Vec2[] locations = new Vec2[spellCount];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = new Vec2((float) (Math.sin(radiansPerSpell * i) * radius), (float) (-Math.cos(radiansPerSpell * i) * radius));
        }

        //var locations = drawRadialBackgrounds(buffer, centerX, centerY, mouseXCenter, mouseYCenter, mouseRadians, quarterCircle, spells);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();


        //Spell Icons
        for (int i = 0; i < locations.length; i++) {
            var spell = spells.get(i);
            if (spell != null) {
                setOpaqueTexture(spells.get(i).getSpellType().getResourceLocation());
                poseStack.pushPose();
                poseStack.translate(centerX, centerY, 0);

                /*
                Icon
                 */
                //scale = 1 + i * .1f;
                poseStack.scale(scale, scale, scale);

                int iconWidth = 16 / 2;
                int borderWidth = 32 / 2;
                int cdWidth = 16 / 2;
                //blit(poseStack, centerX + (int) locations[i].x + 3, centerY + (int) locations[i].y + 3, 0, 0, 16, 16, 16, 16);
                blit(poseStack, (int) locations[i].x - iconWidth, (int) locations[i].y - iconWidth, 0, 0, 16, 16, 16, 16);
                /*
                Border
                 */
                setTranslucentTexture(TEXTURE);
                blit(poseStack, (int) locations[i].x - borderWidth, (int) locations[i].y - borderWidth, selection == i ? 32 : 0, 106, 32, 32);
                /*
                Cooldown
                 */
                float f = spells.get(i) == null ? 0 : ClientMagicData.getCooldownPercent(spells.get(i).getSpellType());
                if (f > 0) {
                    int pixels = (int) (16 * f + 1f);
//                    gui.blit(poseStack, centerX + (int) locations[i].x + 3, centerY + (int) locations[i].y + 19 - pixels, 47, 87, 16, pixels);
                    gui.blit(poseStack, (int) locations[i].x - cdWidth, (int) locations[i].y + cdWidth - pixels, 47, 87, 16, pixels);
                }
                poseStack.popPose();

            }
        }

        //Text
        var selectedSpell = spells.get(selection);
        if (selectedSpell != null) {

            var font = gui.getFont();
            var title = currentSpell.getSpellType().getDisplayName().withStyle(Style.EMPTY.withUnderlined(true));
            var level = Component.translatable("ui.irons_spellbooks.level", TooltipsUtils.getLevelComponenet(selectedSpell, player)).withStyle(selectedSpell.getSpellType().getRarity(selectedSpell.getLevel(null)).getDisplayName().getStyle());
            var mana = Component.translatable("ui.irons_spellbooks.mana_cost", selectedSpell.getManaCost()).withStyle(ChatFormatting.AQUA);
//            selectedSpell.getUniqueInfo(minecraft.player).forEach((line) -> lines.add(line.withStyle(ChatFormatting.DARK_GREEN)));
            int height = 2 * font.lineHeight + 5;

            font.drawShadow(poseStack, title, (float) (centerX - font.width(title) / 2), (float) (centerY - (ringOuterEdge + height)), 0xFFFFFF);
            font.drawShadow(poseStack, level, (float) (centerX - font.width(level) - 5), (float) (centerY - (ringOuterEdge + height) + font.lineHeight + 5), 0xFFFFFF);
            font.drawShadow(poseStack, mana, (float) (centerX + 5), (float) (centerY - (ringOuterEdge + height) + font.lineHeight + 5), 0xFFFFFF);

            var info = selectedSpell.getUniqueInfo(minecraft.player);
            for (int i = 0; i < info.size(); i++) {
                var line = info.get(i);
                font.drawShadow(poseStack, line.withStyle(ChatFormatting.GREEN), centerX - font.width(line) / 2f, (float) (centerY + ringOuterEdgeMax + font.lineHeight * i), 0xFFFFFF);

            }

//                setTranslucentTexture(TEXTURE);
//                gui.blit(poseStack, centerX + (int) locations[i].x, centerY + (int) locations[i].y, 176, 84, 22, 22);
        }
        poseStack.popPose();

    }

    private void drawRadialBackgrounds(BufferBuilder buffer, double centerX, double centerY, int selectedSpellIndex, List<AbstractSpell> spells) {

//        final int spellCount = spells.size();
//        final int spellSegments = 15/*Math.max(3, spells.size())*/;
        double quarterCircle = Math.PI / 2;
        int segments;
        if (spells.size() < 6)
            segments = spells.size() % 2 == 1 ? 15 : 12;
        else
            segments = spells.size() * 2;
        //int segments = Math.max((spells.size() % 2 == 1 ? 15 : 12), `spells.size() * 2);
        double radiansPerObject = 2 * Math.PI / segments;
        double radiansPerSpell = 2 * Math.PI / spells.size();
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax);
        final double fragment = /*Math.PI * 0.005*/0; //gap between buttons in radians at inner edge
        final double fragment2 = /*Math.PI * 0.0025*/0; //gap between buttons in radians at outer edge
//        final double radiansPerObject = 2.0 * Math.PI / spellSegments;
//        Vec2[] locations = new Vec2[spellCount];

        //irons_spellbooks.LOGGER.debug("centerX:{}, centerY:{}, mouseX:{}, mouseY:{}, mouseRad:{}, ringOuter: {}", centerX, centerY, mouseXCenter, mouseYCenter, mouseRadians, ringOuterEdge);

        for (int i = 0; i < segments; i++) {
            var spell = i > spells.size() - 1 ? null : spells.get(i);

            final double beginRadians = i * radiansPerObject - (quarterCircle + (radiansPerSpell / 2));
            final double endRadians = (i + 1) * radiansPerObject - (quarterCircle + (radiansPerSpell / 2));

            final double x1m1 = Math.cos(beginRadians + fragment) * ringInnerEdge;
            final double x2m1 = Math.cos(endRadians - fragment) * ringInnerEdge;
            final double y1m1 = Math.sin(beginRadians + fragment) * ringInnerEdge;
            final double y2m1 = Math.sin(endRadians - fragment) * ringInnerEdge;

            final double x1m2 = Math.cos(beginRadians + fragment2) * ringOuterEdge;
            final double x2m2 = Math.cos(endRadians - fragment2) * ringOuterEdge;
            final double y1m2 = Math.sin(beginRadians + fragment2) * ringOuterEdge;
            final double y2m2 = Math.sin(endRadians - fragment2) * ringOuterEdge;

            final boolean isSelected = spell != null && spellBookData.getActiveSpell().getID() == spell.getID();

            final int extendBy = 10;
//            final boolean isHighlighted = inTriangle(x1m1, y1m1, x2m2 * extendBy, y2m2 * extendBy, x2m1, y2m1, mouseXCenter, mouseYCenter)
//                    || inTriangle(x1m1, y1m1, x1m2 * extendBy, y1m2 * extendBy, x2m2 * extendBy, y2m2 * extendBy, mouseXCenter, mouseYCenter);
            boolean isHighlighted = (i * spells.size()) / segments == selectedSpellIndex;
            //final boolean isHighlighted = beginRadians <= mouseRadians && mouseRadians <= endRadians && isMouseInQuad;

            //irons_spellbooks.LOGGER.debug("i:{}, begin: {}, end: {}, rpo: {}, highlight:{}", i, beginRadians, endRadians, radiansPerObject, isHighlighted);

            Vector4f color = radialButtonColor;
            //if (isSelected) color = selectedColor;
            if (isHighlighted) color = highlightColor;
            //if (isSelected && isHighlighted) color = highlightSelectedColor;

            buffer.vertex(centerX + x1m1, centerY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m2, centerY + y2m2, getBlitOffset()).color(color.x(), color.y(), color.z(), 0).endVertex();
            buffer.vertex(centerX + x1m2, centerY + y1m2, getBlitOffset()).color(color.x(), color.y(), color.z(), 0).endVertex();

            //Category line
            color = /*new Vector4f(0.12f, 0.03f, 0.47f, .5f)*/lineColor;
            final double categoryLineOuterEdge = ringInnerEdge + categoryLineWidth;

            final double x1m3 = Math.cos(beginRadians + fragment) * categoryLineOuterEdge;
            final double x2m3 = Math.cos(endRadians - fragment) * categoryLineOuterEdge;
            final double y1m3 = Math.sin(beginRadians + fragment) * categoryLineOuterEdge;
            final double y2m3 = Math.sin(endRadians - fragment) * categoryLineOuterEdge;

            buffer.vertex(centerX + x1m1, centerY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m3, centerY + y2m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x1m3, centerY + y1m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();


//            if (spell != null) {
//                var radius = Math.cos((endRadians - beginRadians) / 2) * (ringOuterEdge - ringInnerEdge);
//                locations[i] = new Vec2((float) (Math.sin(radiansPerObject * i) * radius), (float) (-Math.cos(radiansPerObject * i) * radius)).add(-11);
//            }
        }

        //irons_spellbooks.LOGGER.debug("");

//        return locations;
    }

    private void drawDividingLines(BufferBuilder buffer, double centerX, double centerY, List<AbstractSpell> spells) {

        if (spells.size() <= 1)
            return;

        double quarterCircle = Math.PI / 2;
        double radiansPerSpell = 2 * Math.PI / spells.size();
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax);

        for (int i = 0; i < spells.size(); i++) {
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
            buffer.vertex(centerX + x1m1, centerY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m2, centerY + y2m2, getBlitOffset()).color(color.x(), color.y(), color.z(), 0).endVertex();
            buffer.vertex(centerX + x1m2, centerY + y1m2, getBlitOffset()).color(color.x(), color.y(), color.z(), 0).endVertex();
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
