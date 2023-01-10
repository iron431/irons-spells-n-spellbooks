package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.example.testmod.item.SpellBook;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.spells.CastType;
import com.example.testmod.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;
import org.checkerframework.checker.units.qual.C;

import java.util.List;

import static com.ibm.icu.text.PluralRules.Operand.i;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SpellBarDisplay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/icons.png");
    static final int IMAGE_HEIGHT = 21;
    static final int IMAGE_WIDTH = 21;
    static final int boxSize = 20;
    static int screenHeight;
    static int screenWidth;

    private static ItemStack lastSpellBook = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onPostRender(RenderGuiOverlayEvent.Post e) {
        Player player = Minecraft.getInstance().player;

        if (!Utils.isPlayerHoldingSpellBook(player))
            return;
        //System.out.println("SpellBarDisplay: Holding Spellbook");
        Gui GUI = Minecraft.getInstance().gui;
        PoseStack stack = e.getPoseStack();
        screenWidth = e.getWindow().getGuiScaledWidth();
        screenHeight = e.getWindow().getGuiScaledHeight();

        int centerX, centerY;
        centerX = screenWidth / 2 - Math.max(110, screenWidth / 4);
        centerY = screenHeight - Math.max(55, screenHeight / 8);


        //
        //  Render Spells
        //
        ItemStack spellbook = player.getMainHandItem().getItem() instanceof SpellBook ? player.getMainHandItem() : player.getOffhandItem();
        var spellBookData = ((SpellBook) spellbook.getItem()).getSpellBookData(spellbook);
        if (spellbook != lastSpellBook) {
            lastSpellBook = spellbook;
            ClientMagicData.generateRelativeLocations(spellBookData, 20, 22);
        }

        var spells = spellBookData.getInscribedSpells();
        var locations = ClientMagicData.relativeSpellBarSlotLocations;
        //Slot Border
        setTranslucentTexture(TEXTURE);
        for (Vec2 location : locations) {
            GUI.blit(stack, centerX + (int) location.x, centerY + (int) location.y, 66, 84, 22, 22);
        }
        //Spell Icons
        for (int i = 0; i < locations.size(); i++) {
            if (spells[i] != null) {
                setOpaqueTexture(spells[i].getSpellType().getResourceLocation());
                GUI.blit(stack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 3, 0, 0, 16, 16, 16, 16);
            }
        }
        //Border + Cooldowns
        for (int i = 0; i < locations.size(); i++) {
            setTranslucentTexture(TEXTURE);
            if (i != spellBookData.getActiveSpellIndex())
                GUI.blit(stack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 22, 84, 22, 22);

            float f = spells[i] == null ? 0 : ClientMagicData.getCooldownPercent(spells[i].getSpellType());
            if (f > 0) {
                int pixels = (int) (16 * f + 1f);
                GUI.blit(stack, centerX + (int) locations.get(i).x + 3, centerY + (int) locations.get(i).y + 19 - pixels, 47, 87, 16, pixels);
            }
        }
        //Selected Outline
        for (int i = 0; i < locations.size(); i++) {
            setTranslucentTexture(TEXTURE);
            if (i == spellBookData.getActiveSpellIndex())
                GUI.blit(stack, centerX + (int) locations.get(i).x, centerY + (int) locations.get(i).y, 0, 84, 22, 22);
        }
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


}
