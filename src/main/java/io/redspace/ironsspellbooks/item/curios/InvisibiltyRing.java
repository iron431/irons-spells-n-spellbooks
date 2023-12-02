package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.compat.Curios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;

public class InvisibiltyRing extends SimpleDescriptiveCurio {
    public InvisibiltyRing() {
        super(new Item.Properties().stacksTo(1), Curios.RING_SLOT);
        this.descriptionStyle = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
    }
}
