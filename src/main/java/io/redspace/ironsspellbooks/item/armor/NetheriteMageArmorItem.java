package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.DyeableArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.netherite.NetheriteMageArmorModel;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class NetheriteMageArmorItem extends ImbuableChestplateArmorItem implements DyeableLeatherItem {
    public NetheriteMageArmorItem(Type type, Properties settings) {
        super(ExtendedArmorMaterials.NETHERITE_BATTLEMAGE, type, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new DyeableArmorRenderer<>(new NetheriteMageArmorModel());
    }

}
