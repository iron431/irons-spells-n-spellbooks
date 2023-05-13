package io.redspace.ironsspellbooks.entity.armor.plagued;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.PumpkinArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoUtils;

import java.util.Objects;

public class PlaguedArmorRenderer extends GenericCustomArmorRenderer<PumpkinArmorItem> {
    public static final String headTorsoLayerBone = "armorHeadTorsoLayer";
    public static final String headRightArmLayerBone = "armorHeadRightArmLayer";
    public static final String headLeftArmLayerBone = "armorHeadLeftArmLayer";

    public PlaguedArmorRenderer(AnimatedGeoModel model) {
        super(model);

        var m = getGeoModelProvider();
        m.registerBone(customBone(headTorsoLayerBone));
        m.registerBone(customBone(headRightArmLayerBone));
        m.registerBone(customBone(headLeftArmLayerBone));

    }

    @Override
    protected void fitToBiped() {
        super.fitToBiped();

        IBone torsoLayerBone = this.getGeoModelProvider().getBone(headTorsoLayerBone);
        GeoUtils.copyRotations(this.body, torsoLayerBone);
        torsoLayerBone.setPositionX(this.body.x);
        torsoLayerBone.setPositionY(-this.body.y);
        torsoLayerBone.setPositionZ(this.body.z);

        IBone rightArmBone = this.getGeoModelProvider().getBone(headRightArmLayerBone);
        GeoUtils.copyRotations(this.rightArm, rightArmBone);
        rightArmBone.setPositionX(this.rightArm.x + 5);
        rightArmBone.setPositionY(2 - this.rightArm.y);
        rightArmBone.setPositionZ(this.rightArm.z);

        IBone leftArmBone = this.getGeoModelProvider().getBone(headLeftArmLayerBone);
        GeoUtils.copyRotations(this.leftArm, leftArmBone);
        leftArmBone.setPositionX(this.leftArm.x - 5);
        leftArmBone.setPositionY(2 - this.leftArm.y);
        leftArmBone.setPositionZ(this.leftArm.z);


    }


    @Override
    public GeoArmorRenderer applySlot(EquipmentSlot slot) {
        super.applySlot(slot);

        setBoneVisibility(headTorsoLayerBone, false);
        setBoneVisibility(headRightArmLayerBone, false);
        setBoneVisibility(headLeftArmLayerBone, false);

        if (Objects.requireNonNull(slot) == EquipmentSlot.HEAD) {
            setBoneVisibility(headTorsoLayerBone, true);
            setBoneVisibility(headRightArmLayerBone, true);
            setBoneVisibility(headLeftArmLayerBone, true);
        }

        return this;
    }

}