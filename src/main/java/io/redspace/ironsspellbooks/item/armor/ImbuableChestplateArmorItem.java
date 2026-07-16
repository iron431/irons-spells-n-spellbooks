package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.skillcasting.data.ISkillContainer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public abstract class ImbuableChestplateArmorItem extends ExtendedArmorItem {

    @Deprecated
    public static Item.Properties setupImbueIfChestplate(Type type, Properties properties) {
        // fixme: surely this is stupid right
        if (type == Type.CHESTPLATE) {
            properties.component(ComponentRegistry.IMBUED_SPELL_CONTAINER, ISkillContainer.create(true, 1));
        }
        return properties;
    }

    public ImbuableChestplateArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties, AttributeContainer... attributes) {
        super(pMaterial, pType, setupImbueIfChestplate(pType, pProperties), attributes);
    }
}
