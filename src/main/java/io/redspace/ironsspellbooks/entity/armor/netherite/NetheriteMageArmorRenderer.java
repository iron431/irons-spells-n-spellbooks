package io.redspace.ironsspellbooks.entity.armor.netherite;

import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.item.GeoArmorItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoUtils;

public class NetheriteMageArmorRenderer<T extends GeoArmorItem & IAnimatable> extends GeoArmorRenderer<T> {
    public NetheriteMageArmorRenderer(AnimatedGeoModel<T> modelProvider) {
        super(modelProvider);
    }
}