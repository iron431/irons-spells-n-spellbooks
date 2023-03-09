package io.redspace.ironsspellbooks.entity.armor;

import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.item.GeoArmorItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoUtils;

public class GenericCustomArmorRenderer<T extends GeoArmorItem & IAnimatable> extends GeoArmorRenderer<T> {
    public static final String leggingTorsoLayerBone = "armorLeggingTorsoLayer";

    public GenericCustomArmorRenderer(AnimatedGeoModel model) {
        super(model);

        this.headBone = "armorHead";
        this.bodyBone = "armorBody";
        this.rightArmBone = "armorRightArm";
        this.leftArmBone = "armorLeftArm";
        this.rightLegBone = "armorRightLeg";
        this.leftLegBone = "armorLeftLeg";
        this.rightBootBone = "armorRightBoot";
        this.leftBootBone = "armorLeftBoot";

        var m = getGeoModelProvider();
        m.registerBone(customBone(leggingTorsoLayerBone));

    }

    @Override
    protected void fitToBiped() {
        super.fitToBiped();

        IBone torsoLayerBone = this.getGeoModelProvider().getBone(leggingTorsoLayerBone);
        GeoUtils.copyRotations(this.body, torsoLayerBone);
        torsoLayerBone.setPositionX(this.body.x);
        torsoLayerBone.setPositionY(-this.body.y);
        torsoLayerBone.setPositionZ(this.body.z);

    }



    @Override
    public GeoArmorRenderer applySlot(EquipmentSlot slot) {
        //What is this for?
        this.getGeoModelProvider().getModel(this.getGeoModelProvider().getModelResource(this.currentArmorItem));

        setBoneVisibility(this.headBone, false);
        setBoneVisibility(this.bodyBone, false);
        setBoneVisibility(this.rightArmBone, false);
        setBoneVisibility(this.leftArmBone, false);
        setBoneVisibility(this.rightLegBone, false);
        setBoneVisibility(this.leftLegBone, false);
        setBoneVisibility(this.rightBootBone, false);
        setBoneVisibility(this.rightBootBone, false);
        setBoneVisibility(this.leftBootBone, false);
        setBoneVisibility(leggingTorsoLayerBone, false);

        switch (slot) {
            case HEAD -> setBoneVisibility(this.headBone, true);
            case CHEST -> {
                setBoneVisibility(this.bodyBone, true);
                setBoneVisibility(this.rightArmBone, true);
                setBoneVisibility(this.leftArmBone, true);
            }
            case LEGS -> {
                setBoneVisibility(this.rightLegBone, true);
                setBoneVisibility(this.leftLegBone, true);
                setBoneVisibility(leggingTorsoLayerBone, true);

            }
            case FEET -> {
                setBoneVisibility(this.rightBootBone, true);
                setBoneVisibility(this.leftBootBone, true);
            }
            default -> {
            }
        }
        if (this.entityLiving instanceof IAnimatable)
            setBoneVisibility(leggingTorsoLayerBone, false);

        return this;
    }

    protected GeoBone customBone(String name) {
        GeoBone bone = new GeoBone();
        bone.name = name;
        return bone;
    }
}