package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.PaladinArmorModel;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PaladinArmorItem extends ImbuableChestplateArmorItem {
    public PaladinArmorItem(Type type, Properties settings) {
        super(ExtendedArmorMaterials.PALADIN, type, settings,
                new AttributeContainer(AttributeRegistry.MAX_MANA, 150, AttributeModifier.Operation.ADDITION),
                new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new PaladinArmorModel());
    }

}
