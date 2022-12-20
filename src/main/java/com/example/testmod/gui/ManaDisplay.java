package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.util.internal.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ManaDisplay extends GuiComponent{
    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID,"textures/gui/icons.png");
    public final static ResourceLocation EMPTY = new ResourceLocation(TestMod.MODID,"textures/gui/health_empty.png");
    public final static ResourceLocation FULL = new ResourceLocation(TestMod.MODID,"textures/gui/health_full.png");
    static final int IMAGE_WIDTH = 98;
    static final int IMAGE_HEIGHT = 21;
    static final int HOTBAR_HEIGHT = 25;
    static final int ICON_ROW_HEIGHT = 11;
    static int screenHeight;
    static int screenWidth;
    static int maxMana = 100;
    static float mana = 50f;
    static char key=' ';
    static boolean centered = true;
    static int colorIndex = 0;
    static ChatFormatting[] colors = {ChatFormatting.AQUA,ChatFormatting.BLUE, ChatFormatting.GOLD,ChatFormatting.DARK_AQUA,ChatFormatting.WHITE,ChatFormatting.YELLOW,ChatFormatting.GRAY,ChatFormatting.DARK_GRAY,ChatFormatting.LIGHT_PURPLE,ChatFormatting.DARK_PURPLE};

    @SubscribeEvent
    public static void onPostRender(RenderGameOverlayEvent.PreLayer e){
        //System.out.println("success");
        var GUI = Minecraft.getInstance().gui;
        var stack = e.getMatrixStack();
        screenWidth = e.getWindow().getGuiScaledWidth();
        screenHeight = e.getWindow().getGuiScaledHeight();

        int barX,barY;
        if(centered){
            barX = screenWidth/2 - IMAGE_WIDTH/2;
            barY = screenHeight - HOTBAR_HEIGHT - ICON_ROW_HEIGHT*3 - IMAGE_HEIGHT/2;
        }else{
            int hotbarWidth = 200;
            barX = screenWidth/2 - IMAGE_WIDTH/2 + hotbarWidth/4;
            barY = screenHeight - HOTBAR_HEIGHT - ICON_ROW_HEIGHT*2 - IMAGE_HEIGHT/2;
        }

        //if(key=='T')
        //    x=0;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f,1f,1f,1f);

        RenderSystem.setShaderTexture(0,TEXTURE);
        //RenderSystem.setShaderTexture(0,EMPTY);
        GUI.blit(stack,barX,barY,0,0,IMAGE_WIDTH,IMAGE_HEIGHT,256,256);

        //RenderSystem.setShaderTexture(0,FULL);
        GUI.blit(stack,barX,barY,0,IMAGE_HEIGHT, (int) (IMAGE_WIDTH*getPercentMana()),IMAGE_HEIGHT);

        int textX,textY;
        int charWidth = 6;
        var textColor = colors[colorIndex];
        String manaFraction =(int)(mana) +"/"+maxMana;
        textX = barX+IMAGE_WIDTH/2-((""+(int)mana).length()+1)*charWidth;
        textY = (int) (screenHeight-HOTBAR_HEIGHT-ICON_ROW_HEIGHT*2.75);
        GUI.getFont().drawShadow(stack,manaFraction,textX,textY, textColor.getColor());
        GUI.getFont().draw(stack,manaFraction,textX,textY, textColor.getColor());
        //addPercentMana(.005*Minecraft.getInstance().getDeltaFrameTime());
        //e.getOverlay().render((ForgeIngameGui) GUI,stack,1,screenWidth,screenHeight);
        addPercentMana(0.01f*Minecraft.getInstance().getDeltaFrameTime());

    }
    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent e){
        key = (char)e.getKey();
        if (e.getKey()==(int)'H'&&e.getAction()==1){
            addMana(5);
        }
        if (e.getKey()==(int)'G'&&e.getAction()==1){
            removeMana(5);
        }
        if (e.getKey()==(int)'J'&&e.getAction()==1){
            //System.out.println(screenWidth+"x"+screenHeight);
            centered = !centered;
            System.out.println(Minecraft.getInstance().getDeltaFrameTime());
        }
        if(e.getKey()==(int)'C'&&e.getAction()==1){
            colorIndex++;
            if (colorIndex>=colors.length)
                colorIndex=0;

        }
    }
    private static void addMana(float amount){
        mana+=amount;
        clampMana();
    }
    private static void removeMana(float amount){
        addMana(-amount);
    }
    private static void addPercentMana(float percent){
        //idk why it ticks so fast
        addMana(percent*maxMana/400f);
    }
    private static void clampMana(){
        //manamanamanmanamanamana
        mana = mana<0||mana>maxMana?mana<0?0:maxMana:mana;
    }
    private static float getPercentMana(){
        return  mana/(float)maxMana;
    }

}
