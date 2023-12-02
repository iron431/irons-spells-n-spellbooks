package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public class InvisibiltyRing extends SimpleDescriptiveCurio {
    public InvisibiltyRing() {
        super(ItemPropertiesHelper.equipment().stacksTo(1), "ring");
        this.descriptionStyle = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
    }
}
