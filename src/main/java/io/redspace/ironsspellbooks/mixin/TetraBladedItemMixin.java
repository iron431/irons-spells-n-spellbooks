package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;

@Mixin(ModularBladedItem.class)
public abstract class TetraBladedItemMixin extends ItemModularHandheld {

    public TetraBladedItemMixin(Properties properties) {
        super(properties);
    }

    public int getUseDuration(@NotNull ItemStack itemStack) {
        IronsSpellbooks.LOGGER.debug("TetraBladedItemMixin.getUseDuration");
        if (SpellData.getSpellData(itemStack).getSpellId() > 0)
            return 7200;
        else
            return super.getUseDuration(itemStack);
    }
}
