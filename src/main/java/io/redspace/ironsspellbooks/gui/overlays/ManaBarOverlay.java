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
import net.minecraftforge.client.gui.ForgeIngameGui;


import static io.redspace.ironsspellbooks.config.ClientConfigs.MANA_BAR_TEXT_VISIBLE;
import static io.redspace.ironsspellbooks.registries.AttributeRegistry.MAX_MANA;

public class ManaBarOverlay {
    public final static ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/gui/icons.png");

    //public final static ResourceLocation EMPTY = new ResourceLocation(irons_spellbooks.MODID,"textures/gui/health_empty.png");
    //public final static ResourceLocation FULL = new ResourceLocation(irons_spellbooks.MODID,"textures/gui/health_full.png");
    public enum Anchor {
        Hunger,
        XP,
        Center,
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight
    }

    public enum Display {
        Never,
        Always,
        Contextual
    }

    static final int DEFAULT_IMAGE_WIDTH = 98;
    static final int XP_IMAGE_WIDTH = 188;
    static final int IMAGE_HEIGHT = 21;
    static final int HOTBAR_HEIGHT = 25;
    static final int ICON_ROW_HEIGHT = 11;
    static final int CHAR_WIDTH = 6;
    static final int HUNGER_BAR_OFFSET = 50;
    static final int SCREEN_BORDER_MARGIN = 20;
    static final int TEXT_COLOR = ChatFormatting.AQUA.getColor();

    public static void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        var player = Minecraft.getInstance().player;

        if (!shouldShowManaBar(player))
            return;

        int maxMana = (int) player.getAttributeValue(MAX_MANA.get());
        int mana = ClientMagicData.getPlayerMana();
        int barX, barY;
        //TODO: cache these?
        int configOffsetY = ClientConfigs.MANA_BAR_Y_OFFSET.get();
        int configOffsetX = ClientConfigs.MANA_BAR_X_OFFSET.get();
        Anchor anchor = ClientConfigs.MANA_BAR_ANCHOR.get();
        if (anchor == Anchor.XP && player.getJumpRidingScale() > 0) //Hide XP Mana bar when actively jumping on a horse
            return;
        barX = getBarX(anchor, screenWidth, player) + configOffsetX;
        barY = getBarY(anchor, screenHeight, player) - configOffsetY;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int imageWidth = anchor == Anchor.XP ? XP_IMAGE_WIDTH : DEFAULT_IMAGE_WIDTH;
        int spriteX = anchor == Anchor.XP ? 68 : 0;
        int spriteY = anchor == Anchor.XP ? 40 : 0;
        gui.blit(poseStack, barX, barY, spriteX, spriteY, imageWidth, IMAGE_HEIGHT, 256, 256);
        gui.blit(poseStack, barX, barY, spriteX, spriteY + IMAGE_HEIGHT, (int) (imageWidth * Math.min((mana / (double) maxMana), 1)), IMAGE_HEIGHT);

        int textX, textY;
        String manaFraction = (mana) + "/" + maxMana;

        textX = barX + imageWidth / 2 - (int) ((("" + mana).length() + 0.5) * CHAR_WIDTH);
        textY = barY + (anchor == Anchor.XP ? ICON_ROW_HEIGHT / 3 : ICON_ROW_HEIGHT);

        if (ClientConfigs.MANA_BAR_TEXT_VISIBLE.get()) {
            gui.getFont().drawShadow(poseStack, manaFraction, textX, textY, TEXT_COLOR);
            //gui.getFont().draw(poseStack, manaFraction, textX, textY, TEXT_COLOR);
        }
    }

    private static int getOffsetCountFromHotbar(Player player) {
        if (!(player == null || player.getAirSupply() <= 0 || player.getAirSupply() >= 300))
            return 3;
        else if (!player.isCreative())
            return 2;
        else
            return 1;
    }

    public static boolean shouldShowManaBar(Player player) {
        //We show mana if they are holding an item that can cast spells or if their mana is not full
        var display = ClientConfigs.MANA_BAR_DISPLAY.get();
        return !player.isSpectator() && display != Display.Never && (display == Display.Always || player.isHolding((itemstack -> itemstack.getItem() instanceof SpellBook || SpellData.hasSpellData(itemstack))) || ClientMagicData.getPlayerMana() < player.getAttributeValue(MAX_MANA.get()));
    }

    private static int getBarX(Anchor anchor, int screenWidth, Player player) {
        if (anchor == Anchor.XP)
            return screenWidth / 2 - 91 - 3; //Vanilla's Pos - 3
        if (anchor == Anchor.Hunger || anchor == Anchor.Center)
            return screenWidth / 2 - DEFAULT_IMAGE_WIDTH / 2 + (anchor == Anchor.Center ? 0 : HUNGER_BAR_OFFSET);
        else if (anchor == Anchor.TopLeft || anchor == Anchor.BottomLeft)
            return SCREEN_BORDER_MARGIN;
        else return screenWidth - SCREEN_BORDER_MARGIN - DEFAULT_IMAGE_WIDTH;

    }

    private static int getBarY(Anchor anchor, int screenHeight, Player player) {
        if (anchor == Anchor.XP)
            return screenHeight - 32 + 3 - 8; //Vanilla's Pos - 8
        if (anchor == Anchor.Hunger || anchor == Anchor.Center)
            return screenHeight - HOTBAR_HEIGHT - (int) (ICON_ROW_HEIGHT * (anchor == Anchor.Center ? 2.5f : getOffsetCountFromHotbar(player))) - IMAGE_HEIGHT / 2;
        else if (anchor == Anchor.TopLeft || anchor == Anchor.TopRight)
            return SCREEN_BORDER_MARGIN;
        else return screenHeight - SCREEN_BORDER_MARGIN - IMAGE_HEIGHT;

    }
}
