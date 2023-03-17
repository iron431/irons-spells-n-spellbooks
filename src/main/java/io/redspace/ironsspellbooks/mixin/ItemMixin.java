package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TieredItem.class)
public abstract class ItemMixin extends Item {

    public ItemMixin(Properties pProperties) {
        super(pProperties);
    }

    public int getUseDuration(@NotNull ItemStack itemStack) {
        IronsSpellbooks.LOGGER.debug("ItemMixin.getUseDuration");
        var spell = SpellData.getSpellData(itemStack).getSpell();
        if (spell.getSpellType() != SpellType.NONE_SPELL)
            return 7200;//return spell.getCastTime();
        else
            return super.getUseDuration(itemStack);
    }
}
