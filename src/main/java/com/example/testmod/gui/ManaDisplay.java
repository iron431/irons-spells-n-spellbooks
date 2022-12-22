package com.example.testmod.gui;

import com.example.testmod.TestMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ManaDisplay extends GuiComponent {
    public final static ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/gui/icons.png");
    //public final static ResourceLocation EMPTY = new ResourceLocation(TestMod.MODID,"textures/gui/health_empty.png");
    //public final static ResourceLocation FULL = new ResourceLocation(TestMod.MODID,"textures/gui/health_full.png");
    static final int IMAGE_WIDTH = 98;
    static final int IMAGE_HEIGHT = 21;
    static final int HOTBAR_HEIGHT = 25;
    static final int ICON_ROW_HEIGHT = 11;
    static final int CHAR_WIDTH = 6;
    static final int HUNGER_BAR_OFFSET = 50;
    static int screenHeight;
    static int screenWidth;
    static int savedMaxMana = 100;
    static float mana = 50f;
    static char key = ' ';
    static boolean centered = true;
    static int colorIndex = 0;
    static ChatFormatting[] colors = {ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.GOLD, ChatFormatting.DARK_AQUA, ChatFormatting.WHITE, ChatFormatting.YELLOW, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY, ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE};

    @SubscribeEvent
    public static void onPostRender(RenderGameOverlayEvent.Text e) {
        /*
            extensions change when its drawn, as far as i understand:
            POST/PostLayer (idk the difference): After Chat
            PRE/PreLayer(^): Intertwined with Chat
            TEXT: Before Chat
            BOSSINFO: only when a bossbar is up
            CHAT: before chat
            ALl of them seem to render above the hotbar however; idk how to use ElementLayers or if they would help

            This must be rendered before Gui.render() in order to appear below the hotbar elements, the only way to do that might be with a mixin
         */
        //System.out.println("success");
        var player = Minecraft.getInstance().player;
        if (player.getAttribute(MAX_MANA.get()) == null) {
            TestMod.LOGGER.info("null");
            return;
        }
        Gui GUI = Minecraft.getInstance().gui;
        PoseStack stack = e.getMatrixStack();
        int maxMana = (int)player.getAttributeValue(MAX_MANA.get());
        savedMaxMana = maxMana; //just because we still handle mana in this class
        screenWidth = e.getWindow().getGuiScaledWidth();
        screenHeight = e.getWindow().getGuiScaledHeight();

        int barX, barY;
        barX = screenWidth / 2 - IMAGE_WIDTH / 2 + (centered ? 0 : HUNGER_BAR_OFFSET);
        barY = screenHeight - HOTBAR_HEIGHT - ICON_ROW_HEIGHT * getOffsetCountFromHotbar(player) - IMAGE_HEIGHT / 2;

        //if(key=='T')
        //    x=0;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        GUI.blit(stack, barX, barY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        GUI.blit(stack, barX, barY, 0, IMAGE_HEIGHT, (int) (IMAGE_WIDTH * getPercentMana()), IMAGE_HEIGHT);

        int textX, textY;
        var textColor = colors[colorIndex];
        String manaFraction = (int)(mana) + "/" + maxMana;

        textX = barX + IMAGE_WIDTH / 2 - (int) ((("" + (int)mana).length() + 0.5) * CHAR_WIDTH);
        textY = barY + ICON_ROW_HEIGHT;

        GUI.getFont().drawShadow(stack, manaFraction, textX, textY, textColor.getColor());
        GUI.getFont().draw(stack, manaFraction, textX, textY, textColor.getColor());

        addPercentMana(0.01f * Minecraft.getInstance().getDeltaFrameTime() / 20f);

    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent e) {
        key = (char) e.getKey();
        if (e.getKey() == (int) 'H' && e.getAction() == 1) {
            addMana(5);
        }
        if (e.getKey() == (int) 'G' && e.getAction() == 1) {
            removeMana(5);
        }
        if (e.getKey() == (int) 'J' && e.getAction() == 1) {
            //System.out.println(screenWidth+"x"+screenHeight);
            centered = !centered;

            System.out.println(Minecraft.getInstance().getDeltaFrameTime()); // in ticks per frame
        }
        if (e.getKey() == (int) 'C' && e.getAction() == 1) {
            colorIndex++;
            if (colorIndex >= colors.length)
                colorIndex = 0;
        }
        if (e.getKey() == (int) 'Y' && e.getAction() == 1) {
            Player player = Minecraft.getInstance().player;
            player.sendMessage(new TextComponent("Launching "+player.getDisplayName().getString()), player.getUUID());
            player.push(0,1,0);
            //player.move(MoverType.SELF, new Vec3(1,10,1));

        }
    }

    private static void addMana(float amount) {
        mana += amount;
        clampMana();
    }

    private static void removeMana(float amount) {
        addMana(-amount);
    }

    private static void addPercentMana(float percent) {
        //idk why it ticks so fast
        addMana(percent * savedMaxMana);
    }

    private static void clampMana() {
        //manamanamanmanamanamana
        mana = mana < 0 || mana > savedMaxMana ? mana < 0 ? 0 : savedMaxMana : mana;
    }

    private static float getPercentMana() {
        return mana / (float) savedMaxMana;
    }

    private static int getOffsetCountFromHotbar(Player player) {
        if (centered || !(player == null || player.getAirSupply() <= 0 || player.getAirSupply() >= 300))
            return 3;
        else if (!player.isCreative())
            return 2;
        else
            return 1;
    }

}
