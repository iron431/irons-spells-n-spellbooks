package com.example.testmod.entity.armor;

import com.example.testmod.item.armor.PumpkinArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoUtils;

import java.util.Objects;

public class PumpkinArmorRenderer extends GenericCustomArmorRenderer<PumpkinArmorItem> {
    public String bodyHeadLayerBone = "armorBodyHeadLayer";

    public PumpkinArmorRenderer(AnimatedGeoModel model) {
        super(model);

        var m = getGeoModelProvider();
        m.registerBone(customBone(this.bodyHeadLayerBone));

    }

    @Override
    protected void fitToBiped() {
        super.fitToBiped();
        if (this.bodyHeadLayerBone != null) {
            IBone torsoLayerBone = this.getGeoModelProvider().getBone(this.bodyHeadLayerBone);

            GeoUtils.copyRotations(this.head, torsoLayerBone);
            torsoLayerBone.setPositionX(this.head.x);
            torsoLayerBone.setPositionY(-this.head.y);
            torsoLayerBone.setPositionZ(this.head.z);
        }
    }


    @Override
    public GeoArmorRenderer applySlot(EquipmentSlot slot) {
        super.applySlot(slot);

        setBoneVisibility(this.bodyHeadLayerBone, false);

        if (Objects.requireNonNull(slot) == EquipmentSlot.CHEST) {
            setBoneVisibility(this.bodyHeadLayerBone, true);
        }

        return this;
    }

}