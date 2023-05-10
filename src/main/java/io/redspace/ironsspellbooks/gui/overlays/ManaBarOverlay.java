package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import static io.redspace.ironsspellbooks.registries.AttributeRegistry.MAX_MANA;

public class ManaBarOverlay {
    public final static ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/icons.png");
    //public final static ResourceLocation EMPTY = new ResourceLocation(irons_spellbooks.MODID,"textures/gui/health_empty.png");
    //public final static ResourceLocation FULL = new ResourceLocation(irons_spellbooks.MODID,"textures/gui/health_full.png");
    static final int IMAGE_WIDTH = 98;
    static final int IMAGE_HEIGHT = 21;
    static final int HOTBAR_HEIGHT = 25;
    static final int ICON_ROW_HEIGHT = 11;
    static final int CHAR_WIDTH = 6;
    static final int HUNGER_BAR_OFFSET = 50;
    static final int TEXT_COLOR = ChatFormatting.AQUA.getColor();
    static boolean centered = false;

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        var player = Minecraft.getInstance().player;

        if (!shouldShowManaBar(player))
            return;

        int maxMana = (int) player.getAttributeValue(MAX_MANA.get());
        int mana = ClientMagicData.getPlayerMana();
        int barX, barY;
        int configOffsetY = ClientConfigs.MANA_BAR_Y_OFFSET.get();
        int configOffsetX = ClientConfigs.MANA_BAR_X_OFFSET.get();

        barX = screenWidth / 2 - IMAGE_WIDTH / 2 + (centered ? 0 : HUNGER_BAR_OFFSET) - configOffsetX;
        barY = screenHeight - HOTBAR_HEIGHT - ICON_ROW_HEIGHT * getOffsetCountFromHotbar(player) - IMAGE_HEIGHT / 2 - configOffsetY;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        gui.blit(poseStack, barX, barY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
        gui.blit(poseStack, barX, barY, 0, IMAGE_HEIGHT, (int) (IMAGE_WIDTH * Math.min((mana / (double) maxMana), 1)), IMAGE_HEIGHT);

        int textX, textY;
        String manaFraction = (mana) + "/" + maxMana;

        textX = barX + IMAGE_WIDTH / 2 - (int) ((("" + mana).length() + 0.5) * CHAR_WIDTH);
        textY = barY + ICON_ROW_HEIGHT;

        gui.getFont().drawShadow(poseStack, manaFraction, textX, textY, TEXT_COLOR);
        gui.getFont().draw(poseStack, manaFraction, textX, textY, TEXT_COLOR);


    }

    private static int getOffsetCountFromHotbar(Player player) {
        if (centered || !(player == null || player.getAirSupply() <= 0 || player.getAirSupply() >= 300))
            return 3;
        else if (!player.isCreative())
            return 2;
        else
            return 1;
    }

    private static boolean shouldShowManaBar(Player player) {
        //We show mana if they are holding an item that can cast spells or if their mana is not full
        return ClientConfigs.ALWAYS_SHOW_MANA_BAR.get() || player.isHolding((itemstack -> itemstack.getItem() instanceof SpellBook || SpellData.hasSpellData(itemstack))) || ClientMagicData.getPlayerMana() < player.getAttributeValue(MAX_MANA.get());
    }
}
