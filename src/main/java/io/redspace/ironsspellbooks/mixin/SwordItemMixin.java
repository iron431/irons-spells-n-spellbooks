package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin extends Item {

    public SwordItemMixin(Properties pProperties) {
        super(pProperties);
    }

    public int getUseDuration(@NotNull ItemStack itemStack) {
        if (SpellData.getSpellData(itemStack).getSpellId() > 0)
            return 7200;
        else
            return super.getUseDuration(itemStack);
    }
}
