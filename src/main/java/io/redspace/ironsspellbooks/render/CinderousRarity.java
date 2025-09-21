package io.redspace.ironsspellbooks.render;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;

import java.util.function.UnaryOperator;

public class CinderousRarity {
//    public static final EnumProxy<Rarity> CINDEROUS_RARITY_PROXY = new EnumProxy<>(Rarity.class,
//            -1,
//            "irons_spellbooks:cinderous",
//            (UnaryOperator<Style>) ((style) -> style.withColor(0xf2552e))
//    );

    public static final Rarity CINDEROUS_RARITY = Rarity.create("IRONS_SPELLBOOKS_CINDEROUS", (UnaryOperator<Style>) ((style) -> style.withColor(0xf2552e)));
}
