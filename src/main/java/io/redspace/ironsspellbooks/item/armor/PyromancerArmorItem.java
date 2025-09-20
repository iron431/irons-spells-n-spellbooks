package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.PyromancerArmorModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PyromancerArmorItem extends ImbuableChestplateArmorItem implements IArmorCapeProvider {
    public PyromancerArmorItem(Type slot, Properties settings) {
        super(ExtendedArmorMaterials.PYROMANCER, slot, settings);
    }

    @Override
    public ResourceLocation getCapeResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/pyromancer_cape.png");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new PyromancerArmorModel());
    }
}
