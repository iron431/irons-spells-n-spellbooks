package com.example.testmod.gui.overlays;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.SpellBookData;
import com.example.testmod.gui.inscription_table.network.PacketChangeSelectedSpell;
import com.example.testmod.item.SpellBook;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.util.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class SpellWheelOverlay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/icons.png");
    public final static ResourceLocation WHEEL = new ResourceLocation(TestMod.MODID, "textures/gui/spell_wheel.png");

    public static boolean active;
    private static int selection;
    private static int selectedSpellIndex;

    public static void open() {
        active = true;
        selection = -1;
        selectedSpellIndex = -1;
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    public static void close() {
        active = false;
        if (selectedSpellIndex >= 0) {
            Messages.sendToServer(new PacketChangeSelectedSpell(selectedSpellIndex));
        }
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (!active)
            return;

        var minecraft = Minecraft.getInstance();

        if ((minecraft.player == null || minecraft.screen != null || minecraft.mouseHandler.isMouseGrabbed() || !Utils.isPlayerHoldingSpellBook(minecraft.player))) {
            active = false;
            return;
        }



        Player player = minecraft.player;

        int centerX, centerY;
        centerX = screenWidth / 2;
        centerY = screenHeight / 2;

        //
        //  Render Spells
        //
        ItemStack spellbook = player.getMainHandItem().getItem() instanceof SpellBook ? player.getMainHandItem() : player.getOffhandItem();

        var spellBookData = ((SpellBook) spellbook.getItem()).getSpellBookData(spellbook);
        //var spells = spellBookData.getInscribedSpells();
        int spellCount = spellBookData.getSpellCount();
        float scale = Mth.clamp(1 + 3 * (spellCount / 15f), 1, 4);
        var locations = generateWheelPositions(spellBookData, scale);

        List<AbstractSpell> spells = new ArrayList<>();
        for (AbstractSpell spell : spellBookData.getInscribedSpells()) {
            if (spell != null)
                spells.add(spell);
        }
//        TestMod.LOGGER.debug("SpellBarDisplay.spellcount: {}", spellCount);
//        TestMod.LOGGER.debug("SpellBarDisplay.non-null spells.length: {}", spells.size());
//        if (spellCount > spells.size())
//            return;
        Vec2 screenCenter = new Vec2(minecraft.getWindow().getScreenWidth() * .5f, minecraft.getWindow().getScreenHeight() * .5f);
        Vec2 mousePos = new Vec2((float) minecraft.mouseHandler.xpos(), (float) minecraft.mouseHandler.ypos());
        double radiansPerSpell = Math.toRadians(360 / (float) spellCount);

        float mouseRotation = (Utils.getAngle(
                mousePos,
                screenCenter) + 1.570f + (float) radiansPerSpell * .5f) % 6.283f;

        selection = (int) Mth.clamp(mouseRotation / radiansPerSpell, 0, spellCount - 1);
        selectedSpellIndex = ArrayUtils.indexOf(spellBookData.getInscribedSpells(), spells.get(selection));

        //gui.getFont().draw(stack, screenCenter.x + ", " + screenCenter.y + "\n" + mousePos.x + ", " + mousePos.y + "\n" + Math.toDegrees(mouseRotation) , centerX, centerY, 0xFFFFFF);

        setTranslucentTexture(WHEEL);
        poseStack.scale(scale, scale, scale);
        gui.blit(poseStack, (int) (centerX / scale - 32), (int) (centerY / scale - 32), 0, 0, 64, 64, 64, 64);
        poseStack.scale(1 / scale, 1 / scale, 1 / scale);

        //Slot Border, icon, selected frame
        setTranslucentTexture(TEXTURE);
        for (int i = 0; i < locations.size(); i++) {
            gui.blit(poseStack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 66, 84, 22, 22);
            gui.blit(poseStack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 22, 84, 22, 22);
            if (selection == i)
                gui.blit(poseStack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 88, 84, 22, 22);
        }
        //Spell Icons, cooldowns
        for (int i = 0; i < locations.size(); i++) {
            if (spells.get(i) != null) {
                setOpaqueTexture(spells.get(i).getSpellType().getResourceLocation());
                gui.blit(poseStack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 3, 0, 0, 16, 16, 16, 16);

                float f = spells.get(i) == null ? 0 : ClientMagicData.getCooldownPercent(spells.get(i).getSpellType());
                if (f > 0) {
                    setTranslucentTexture(TEXTURE);
                    int pixels = (int) (16 * f + 1f);
                    gui.blit(poseStack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 19 - pixels, 47, 87, 16, pixels);
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
        RenderSystem.defaultBlendFunc();
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
