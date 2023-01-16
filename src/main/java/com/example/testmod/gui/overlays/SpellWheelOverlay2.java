package com.example.testmod.gui.overlays;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.SpellBookData;
import com.example.testmod.gui.network.PacketChangeSelectedSpell;
import com.example.testmod.item.SpellBook;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SpellWheelOverlay2 extends GuiComponent {
    public static SpellWheelOverlay2 instance = new SpellWheelOverlay2();

    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/icons.png");
    public final static ResourceLocation WHEEL = new ResourceLocation(TestMod.MODID, "textures/gui/spell_wheel.png");

    private final Vector4f radialButtonColor = new Vector4f(0f, 0f, 0f, .5f);
    private final Vector4f sideButtonColor = new Vector4f(.5f, .5f, .5f, .5f);
    private final Vector4f highlightColor = new Vector4f(.6f, .8f, 1f, .6f);
    private final Vector4f selectedColor = new Vector4f(0f, .5f, 1f, .5f);
    private final Vector4f highlightSelectedColor = new Vector4f(0.2f, .7f, 1f, .7f);

    private final int whiteTextColor = 0xffffffff;
    private final int watermarkTextColor = 0x88888888;
    private final int descriptionTextColor = 0xdd888888;
    private final int optionTextColor = 0xeeeeeeff;

    private final double ringInnerEdge = 20;
    private double ringOuterEdge = 90;
    private final double ringOuterEdgeMax = 90;
    private final double ringOuterEdgeMin = 65;
    private final double categoryLineWidth = 1;
    private final double textDistance = 75;
    private final double buttonDistance = 105;
    private final float fadeSpeed = 0.3f;
    private final int descriptionHeight = 100;
    private float visibility = 0f;

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
            Messages.sendToServer(new PacketChangeSelectedSpell(selectedSpellIndex));
        }
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!active)
            return;

        var minecraft = Minecraft.getInstance();

        if ((minecraft.player == null || minecraft.screen != null || minecraft.mouseHandler.isMouseGrabbed() || !Utils.isPlayerHoldingSpellBook(minecraft.player))) {
            active = false;
            return;
        }

        poseStack.pushPose();

        Player player = minecraft.player;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        ItemStack spellBookStack = player.getMainHandItem().getItem() instanceof SpellBook ? player.getMainHandItem() : player.getOffhandItem();
        spellBookData = ((SpellBook) spellBookStack.getItem()).getSpellBookData(spellBookStack);
        List<AbstractSpell> spells = spellBookData.getActiveInscribedSpells();
        int spellCount = spells.size();

        float scale = Mth.clamp(1 + 3 * (spellCount / 15f), 1, 4);
        Vec2 screenCenter = new Vec2(minecraft.getWindow().getScreenWidth() * .5f, minecraft.getWindow().getScreenHeight() * .5f);
        Vec2 mousePos = new Vec2((float) minecraft.mouseHandler.xpos(), (float) minecraft.mouseHandler.ypos());
        double radiansPerSpell = Math.toRadians(360 / (float) spellCount);

        float mouseRotation = (Utils.getAngle(mousePos, screenCenter) + 1.570f + (float) radiansPerSpell * .5f) % 6.283f;

        selection = (int) Mth.clamp(mouseRotation / radiansPerSpell, 0, spellCount - 1);
        selectedSpellIndex = ArrayUtils.indexOf(spellBookData.getInscribedSpells(), spells.get(selection));

        int mouseXX = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
        int mouseYY = (int) (minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());
        final double mouseXCenter = mouseXX - centerX;
        final double mouseYCenter = mouseYY - centerY;
        double mouseRadians = Math.atan2(mouseYCenter, mouseXCenter);

        fill(poseStack, 0, 0, screenWidth, screenHeight, 0);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder buffer = tesselator.getBuilder();
        final double quarterCircle = Math.PI / 2; //Original
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var locations = drawRadialBackgrounds(buffer, centerX, centerY, mouseXCenter, mouseYCenter, mouseRadians, quarterCircle, spells);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        //Spell Icon Borders
        setTranslucentTexture(TEXTURE);
        for (int i = 0; i < locations.length; i++) {
            var spell = spells.get(i);
            if (spell != null) {
                gui.blit(poseStack, centerX + (int) locations[i].x, centerY + (int) locations[i].y, 176, 84, 22, 22);
            }
        }

        //Spell Icons
        for (int i = 0; i < locations.length; i++) {
            var spell = spells.get(i);
            if (spell != null) {
                setOpaqueTexture(spells.get(i).getSpellType().getResourceLocation());
                blit(poseStack, centerX + (int) locations[i].x + 3, centerY + (int) locations[i].y + 3, 0, 0, 16, 16, 16, 16);

                float f = spells.get(i) == null ? 0 : ClientMagicData.getCooldownPercent(spells.get(i).getSpellType());
                if (f > 0) {
                    setTranslucentTexture(TEXTURE);
                    int pixels = (int) (16 * f + 1f);
                    gui.blit(poseStack, centerX + (int) locations[i].x + 3, centerY + (int) locations[i].y + 19 - pixels, 47, 87, 16, pixels);
                }
            }
        }

        poseStack.popPose();
    }

    private Vec2[] drawRadialBackgrounds(BufferBuilder buffer, double centerX, double centerY,
                                         double mouseXCenter, double mouseYCenter, double mouseRadians, double quarterCircle, List<AbstractSpell> spells) {

        final int spellCount = spells.size();
        final int spellSegments = Math.max(3, spells.size());
        ringOuterEdge = Math.max(ringOuterEdgeMin, ringOuterEdgeMax * (spellSegments / 15d));
        final double fragment = Math.PI * 0.005; //gap between buttons in radians at inner edge
        final double fragment2 = Math.PI * 0.0025; //gap between buttons in radians at outer edge
        final double radiansPerObject = 2.0 * Math.PI / spellSegments;
        Vec2[] locations = new Vec2[spellCount];

        //TestMod.LOGGER.debug("centerX:{}, centerY:{}, mouseX:{}, mouseY:{}, mouseRad:{}, ringOuter: {}", centerX, centerY, mouseXCenter, mouseYCenter, mouseRadians, ringOuterEdge);

        for (int i = 0; i < spellSegments; i++) {
            var spell = i > spells.size() - 1 ? null : spells.get(i);

            final double beginRadians = i * radiansPerObject - (quarterCircle + (radiansPerObject / 2));
            final double endRadians = (i + 1) * radiansPerObject - (quarterCircle + (radiansPerObject / 2));

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
            final boolean isHighlighted = inTriangle(x1m1, y1m1, x2m2 * extendBy, y2m2 * extendBy, x2m1, y2m1, mouseXCenter, mouseYCenter)
                    || inTriangle(x1m1, y1m1, x1m2 * extendBy, y1m2 * extendBy, x2m2 * extendBy, y2m2 * extendBy, mouseXCenter, mouseYCenter);

            //final boolean isHighlighted = beginRadians <= mouseRadians && mouseRadians <= endRadians && isMouseInQuad;

            //TestMod.LOGGER.debug("i:{}, begin: {}, end: {}, rpo: {}, highlight:{}", i, beginRadians, endRadians, radiansPerObject, isHighlighted);

            Vector4f color = radialButtonColor;
            if (isSelected) color = selectedColor;
            if (isHighlighted) color = highlightColor;
            if (isSelected && isHighlighted) color = highlightSelectedColor;

            buffer.vertex(centerX + x1m1, centerY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m2, centerY + y2m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x1m2, centerY + y1m2, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();

            //Category line
            color = new Vector4f(0.12f, 0.03f, 0.47f, .5f);
            final double categoryLineOuterEdge = ringInnerEdge + categoryLineWidth;

            final double x1m3 = Math.cos(beginRadians + fragment) * categoryLineOuterEdge;
            final double x2m3 = Math.cos(endRadians - fragment) * categoryLineOuterEdge;
            final double y1m3 = Math.sin(beginRadians + fragment) * categoryLineOuterEdge;
            final double y2m3 = Math.sin(endRadians - fragment) * categoryLineOuterEdge;

            buffer.vertex(centerX + x1m1, centerY + y1m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m1, centerY + y2m1, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x2m3, centerY + y2m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();
            buffer.vertex(centerX + x1m3, centerY + y1m3, getBlitOffset()).color(color.x(), color.y(), color.z(), color.w()).endVertex();

            if (spell != null) {
                var radius = Math.cos((endRadians - beginRadians) / 2) * (ringOuterEdge - ringInnerEdge);
                locations[i] = new Vec2((float) (Math.sin(radiansPerObject * i) * radius), (float) (-Math.cos(radiansPerObject * i) * radius)).add(-11);
            }
        }
        //TestMod.LOGGER.debug("");

        return locations;
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
