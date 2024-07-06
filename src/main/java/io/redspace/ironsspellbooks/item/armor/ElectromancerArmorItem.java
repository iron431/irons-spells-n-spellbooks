package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.armor.ElectromancerArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ElectromancerArmorItem extends ImbuableChestplateArmorItem implements ArmorCapeProvider {
    public ElectromancerArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.ELECTROMANCER, slot, settings);
    }

    @Override
    public ResourceLocation getCapeResourceLocation() {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/electromancer_cape.png");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new ElectromancerArmorModel());
    }
}
