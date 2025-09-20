package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.TarnishedCrownModel;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class TarnishedCrownArmorItem extends ExtendedArmorItem implements IPresetSpellContainer {
    public TarnishedCrownArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.TARNISHED, slot, settings,
                new AttributeContainer(AttributeRegistry.MAX_MANA, 150, AttributeModifier.Operation.ADDITION),
                new AttributeContainer(AttributeRegistry.MANA_REGEN, 0.25, AttributeModifier.Operation.MULTIPLY_BASE),
                new AttributeContainer(()->Attributes.ATTACK_DAMAGE, -0.15, AttributeModifier.Operation.MULTIPLY_BASE)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new TarnishedCrownModel());
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            var spellContainer = ISpellContainer.create(1, true, true);
            ISpellContainer.set(itemStack, spellContainer);
        }
    }
}
