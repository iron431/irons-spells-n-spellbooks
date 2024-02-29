package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public abstract class ImbuableChestplateArmorItem extends ExtendedArmorItem implements IPresetSpellContainer {
    public ImbuableChestplateArmorItem(IronsExtendedArmorMaterial material, EquipmentSlot slot, Properties settings) {
        super(material, slot, settings);
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getSlot() == EquipmentSlot.CHEST) {
            if (!ISpellContainer.isSpellContainer(itemStack)) {
                var spellContainer = ISpellContainer.create(1, true, true);
                spellContainer.save(itemStack);
            }
        }
    }
}
