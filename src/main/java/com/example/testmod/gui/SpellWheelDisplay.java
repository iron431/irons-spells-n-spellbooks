package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.data.SpellBookData;
import com.example.testmod.item.SpellBook;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.util.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sun.jna.platform.win32.Wdm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SpellWheelDisplay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/icons.png");
    public final static ResourceLocation WHEEL = new ResourceLocation(TestMod.MODID, "textures/gui/spell_wheel.png");

    private static int screenHeight;
    private static int screenWidth;
    private static boolean active;
    private static int selection;

    public static void open() {
        active = true;
        selection = -1;
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    public static void close() {
        active = false;
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    @SubscribeEvent
    public static void onPostRender(RenderGameOverlayEvent.Text e) {

        var minecraft = Minecraft.getInstance();

        if (active && (minecraft.screen != null || minecraft.mouseHandler.isMouseGrabbed()))
            active = false;

        if (!active)
            return;

        Player player = minecraft.player;
        Gui gui = Minecraft.getInstance().gui;
        PoseStack stack = e.getMatrixStack();
        screenWidth = e.getWindow().getGuiScaledWidth();
        screenHeight = e.getWindow().getGuiScaledHeight();

        int centerX, centerY;
        centerX = screenWidth / 2;
        centerY = screenHeight / 2;

        //
        //  Render Spells
        //

        ItemStack spellbook = player.getMainHandItem().getItem() instanceof SpellBook ? player.getMainHandItem() : player.getOffhandItem();

        var spellBookData = ((SpellBook) spellbook.getItem()).getSpellBookData(spellbook);
        var spells = spellBookData.getInscribedSpells();
        int spellCount = spellBookData.getSpellCount();
        float scale = Mth.clamp(1 + 3 * (spellCount / 15f), 1, 4);
        var locations = generateWheelPositions(spellBookData, scale);

        if (selection < 0)
            selection = spellBookData.getActiveSpellIndex();

        Vec2 screenCenter = new Vec2(e.getWindow().getScreenWidth() * .5f, e.getWindow().getScreenHeight() * .5f);
        Vec2 mousePos = new Vec2((float) minecraft.mouseHandler.xpos(), (float) minecraft.mouseHandler.ypos());
        double radiansPerSpell = Math.toRadians(360 / (float) spellCount);

        float mouseRotation = (Utils.getAngle(
                mousePos,
                screenCenter) + 1.570f + (float) radiansPerSpell * .5f) % 6.283f;

        selection = (int) Mth.clamp(mouseRotation / radiansPerSpell, 0, spellCount);

        //gui.getFont().draw(stack, screenCenter.x + ", " + screenCenter.y + "\n" + mousePos.x + ", " + mousePos.y + "\n" + Math.toDegrees(mouseRotation) , centerX, centerY, 0xFFFFFF);

        setTranslucentTexture(WHEEL);
        stack.scale(scale, scale, scale);
        gui.blit(stack, (int) (centerX / scale - 32), (int) (centerY / scale - 32), 0, 0, 64, 64, 64, 64);
        stack.scale(1 / scale, 1 / scale, 1 / scale);

        //Slot Border, icon, selected frame
        setTranslucentTexture(TEXTURE);
        for (int i = 0; i < locations.size(); i++) {
            gui.blit(stack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 66, 84, 22, 22);
            gui.blit(stack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 22, 84, 22, 22);
            if (selection == i)
                gui.blit(stack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 88, 84, 22, 22);
        }
        //Spell Icons, cooldowns
        for (int i = 0; i < locations.size(); i++) {
            if (spells[i] != null) {
                setOpaqueTexture(spells[i].getSpellType().getResourceLocation());
                gui.blit(stack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 3, 0, 0, 16, 16, 16, 16);

                float f = spells[i] == null ? 0 : ClientMagicData.getCooldownPercent(spells[i].getSpellType());
                if (f > 0) {
                    setTranslucentTexture(TEXTURE);
                    int pixels = (int) (16 * f + 1f);
                    gui.blit(stack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 19 - pixels, 47, 87, 16, pixels);
                }
            }
        }
        //gui.getFont().draw(stack, Math.toDegrees(mouseRotation) + " ", centerX, centerY, 0xFFFFFF);
    }

    private static void drawWithScale(Gui gui, PoseStack stack, int x, int y, int width, int height, int spriteX, int spriteY, int spriteWidth, int spriteHeight, float scale) {
        gui.blit(stack, (int) (x / scale) - width / 2, (int) (y / scale) - height / 2, spriteX, spriteY, spriteWidth, spriteHeight);
    }

    private static void drawWithScale(Gui gui, PoseStack stack, int x, int y, int width, int height, int spriteX, int spriteY, int spriteWidth, int spriteHeight, int spriteMapWidth, int spriteMapHeight, float scale) {
        gui.blit(stack, (int) (x / scale) - width / 2, (int) (y / scale) - height / 2, spriteX, spriteY, spriteWidth, spriteHeight, spriteMapWidth, spriteMapHeight);
    }

    private static void setOpaqueTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }

    private static void setTranslucentTexture(ResourceLocation texture) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, texture);
    }

    private static List<Vec2> generateWheelPositions(SpellBookData spellBookData, float scale) {
        List<Vec2> locations = Lists.newArrayList();
        int spellCount = spellBookData.getSpellCount();
        if (spellCount > 0) {
            double radiansPerSpell = Math.toRadians(360 / (float) spellCount);
            int radius = (int) (128 * .75 * (scale / 4));
            for (int i = 0; i < spellCount; i++) {
                locations.add(new Vec2((float) Math.sin(radiansPerSpell * i) * radius, (float) (-Math.cos(radiansPerSpell * i) * radius)).add(-11));
            }
        }

        return locations;
    }
}
