package com.example.testmod.entity.armor.pumpkin;

import com.example.testmod.entity.armor.GenericCustomArmorRenderer;
import com.example.testmod.item.armor.PumpkinArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoUtils;

import java.util.Objects;

public class PumpkinArmorRenderer extends GenericCustomArmorRenderer<PumpkinArmorItem> {
    public static final String bodyHeadLayerBone = "armorBodyHeadLayer";

    public PumpkinArmorRenderer(AnimatedGeoModel model) {
        super(model);

        var m = getGeoModelProvider();
        m.registerBone(customBone(bodyHeadLayerBone));

    }

    @Override
    protected void fitToBiped() {
        super.fitToBiped();

        IBone torsoLayerBone = this.getGeoModelProvider().getBone(bodyHeadLayerBone);
        GeoUtils.copyRotations(this.head, torsoLayerBone);
        torsoLayerBone.setPositionX(this.head.x);
        torsoLayerBone.setPositionY(-this.head.y);
        torsoLayerBone.setPositionZ(this.head.z);
    }


    @Override
    public GeoArmorRenderer applySlot(EquipmentSlot slot) {
        super.applySlot(slot);

        setBoneVisibility(bodyHeadLayerBone, false);

        if (Objects.requireNonNull(slot) == EquipmentSlot.CHEST) {
            setBoneVisibility(bodyHeadLayerBone, true);
        }

        if (this.entityLiving instanceof IAnimatable)
            setBoneVisibility(bodyHeadLayerBone, false);

        return this;
    }

}