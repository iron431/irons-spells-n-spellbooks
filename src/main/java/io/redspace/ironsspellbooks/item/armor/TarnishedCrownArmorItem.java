package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class TarnishedCrownArmorItem extends ExtendedArmorItem implements IPresetSpellContainer {
    public TarnishedCrownArmorItem(EquipmentSlot slot, Properties settings) {
        super(ExtendedArmorMaterials.TARNISHED, slot, settings);
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            var spellContainer = ISpellContainer.create(1, true, true);
            spellContainer.save(itemStack);
        }
    }
}
