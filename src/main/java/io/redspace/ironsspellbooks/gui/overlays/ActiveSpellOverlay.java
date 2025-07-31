package io.redspace.ironsspellbooks.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ActiveSpellOverlay implements IGuiOverlay {
    public static ActiveSpellOverlay instance = new ActiveSpellOverlay();

    protected static final ResourceLocation WIDGETS_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/widgets.png");
    public final static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/gui/icons.png");

    public void render(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        ItemStack stack = player.getMainHandItem();
        AbstractSpell spell;
        if (hasRightClickCasting(stack.getItem())) {
            if (ISpellContainer.isSpellContainer(stack)) {
                spell = ISpellContainer.get(stack).getSpellAtIndex(0).getSpell();
            } else {
                spell = ClientMagicData.getSpellSelectionManager().getSelectedSpellData().getSpell();
            }
        } else {
            stack = player.getOffhandItem();
            if (hasRightClickCasting(stack.getItem())) {
                if (ISpellContainer.isSpellContainer(stack)) {
                    spell = ISpellContainer.get(stack).getSpellAtIndex(0).getSpell();
                } else {
                    spell = ClientMagicData.getSpellSelectionManager().getSelectedSpellData().getSpell();
                }
            } else {
                return;
            }
        }
        if (stack.isEmpty() || spell == SpellRegistry.none()) {
            return;
        }

        int centerX, centerY;
        //GUI:522 (offhand slot location)
        centerX = screenWidth / 2 + 91 + 9;// + 29;
        centerY = screenHeight - 23;

        //
        //  Render Spells
        //
        //Slot Border
        guiHelper.blit(WIDGETS_LOCATION, centerX, centerY, 24, 22, 29, 24);
        //Spell Icon
        guiHelper.blit(spell.getSpellIconResource(), centerX + 3, centerY + 4, 0, 0, 16, 16, 16, 16);
        //Border + Cooldowns
        float f = ClientMagicData.getCooldownPercent(spell);
        if (f > 0 && !stack.getItem().equals(ItemRegistry.SCROLL.get())) {
            //setTranslucentTexture(TEXTURE);
            int pixels = (int) (16 * f + 1f);
            guiHelper.blit(TEXTURE, centerX + 3, centerY + 20 - pixels, 47, 87, 16, pixels);
        }
    }

    private static boolean hasRightClickCasting(Item item) {
        //TODO: check configs when implemented
        return item instanceof Scroll || item instanceof CastingItem;
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
}
