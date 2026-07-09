package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public abstract class ImbuableChestplateArmorItem extends ExtendedArmorItem {

    public static Item.Properties setupImbueIfChestplate(Type type, Properties properties) {
        if (type == Type.CHESTPLATE) {
            properties.component(SkillcastingDataComponents.SKILL_CONTAINER, ISkillContainer.create(true, 1));
        }
        return properties;
    }

    public ImbuableChestplateArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties, AttributeContainer... attributes) {
        super(pMaterial, pType, setupImbueIfChestplate(pType, pProperties), attributes);
    }

//    @Override
//    public void initializeSpellContainer(ItemStack itemStack) {
//        if (itemStack == null) {
//            return;
//        }
//
//        if (itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getType() == Type.CHESTPLATE) {
//            if (!ISpellContainer.isSpellContainer(itemStack)) {
//                var spellContainer = ISpellContainer.create(1, true, true);
//                ISpellContainer.set(itemStack, spellContainer);
//            }
//        }
//    }
}
