package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ScrollSpellIconDecorator implements IItemDecorator {
    private static final int ITEM_SIZE = 16;
    private static final float ICON_SCALE = 0.5f;
    private static final int ICON_SIZE = (int) (ITEM_SIZE * ICON_SCALE);
    private static final int BUFFER = 0;

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int xOffset, int yOffset) {
        if (!ISpellContainer.isSpellContainer(stack)) {
            return false;
        }
        var spell = ISpellContainer.get(stack).getSpellAtIndex(0).getSpell();
        if (spell == SpellRegistry.none()) {
            return false;
        }
        ResourceLocation icon = spell.getSpellIconResource();
        int x = xOffset + ITEM_SIZE - ICON_SIZE - BUFFER;
        int y = yOffset + BUFFER;
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 250);
        pose.scale(ICON_SCALE, ICON_SCALE, 1f);
        guiGraphics.blit(icon, 0, 0, 0, 0, ITEM_SIZE, ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
        pose.popPose();
        return false;
    }
}
