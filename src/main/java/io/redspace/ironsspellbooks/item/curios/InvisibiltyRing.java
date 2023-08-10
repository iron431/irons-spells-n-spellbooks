package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;

public class InvisibiltyRing extends SimpleDescriptiveCurio {
    public InvisibiltyRing() {
        super(new Item.Properties().tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB).stacksTo(1), "ring");
        this.descriptionStyle = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
    }
}
