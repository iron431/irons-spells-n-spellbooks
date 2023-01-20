package com.example.testmod.entity.armor;

import com.example.testmod.item.armor.WizardArmorItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class WizardArmorRenderer extends GeoArmorRenderer<WizardArmorItem> {
    public WizardArmorRenderer() {
        super(new WizardArmorModel());

        this.headBone = "armorHead";
        this.bodyBone = "armorBody";
        this.rightArmBone = "armorRightArm";
        this.leftArmBone = "armorLeftArm";
        this.rightLegBone = "armorLeftLeg";
        this.leftLegBone = "armorRightLeg";
        this.rightBootBone = "armorLeftBoot";
        this.leftBootBone = "armorRightBoot";
    }
}