package com.example.testmod.entity.armor.wandering_magician;

import com.example.testmod.item.armor.WanderingMagicianArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeoUtils;

public class WanderingMagicianRenderer extends GeoArmorRenderer<WanderingMagicianArmorItem> {
    public String leggingTorsoLayer = "armorLeggingTorsoLayer";
    private final GeoBone leggingTorsoLayerBone;

    public WanderingMagicianRenderer() {
        super(new WanderingMagicianModel());

        this.headBone = "armorHead";
        this.bodyBone = "armorBody";
        this.rightArmBone = "armorRightArm";
        this.leftArmBone = "armorLeftArm";
        this.rightLegBone = "armorRightLeg";
        this.leftLegBone = "armorLeftLeg";
        this.rightBootBone = "armorRightBoot";
        this.leftBootBone = "armorLeftBoot";
        leggingTorsoLayerBone = new GeoBone();
        leggingTorsoLayerBone.name = "armorLeggingTorsoLayer";

    }

    @Override
    protected void fitToBiped() {
        super.fitToBiped();
        ensureBone();
        if (this.leggingTorsoLayer != null) {
            IBone torsoLayerBone = this.getGeoModelProvider().getBone(this.leggingTorsoLayer);

            GeoUtils.copyRotations(this.body, torsoLayerBone);
            torsoLayerBone.setPositionX(this.body.x);
            torsoLayerBone.setPositionY(-this.body.y);
            torsoLayerBone.setPositionZ(this.body.z);
        }
        //TestMod.LOGGER.debug("WizardArmorRenderer.fitToBiped all bones: {}", WizardArmorModel.listOfBonesToString(getGeoModelProvider().getAnimationProcessor().getModelRendererList()));
    }

    @Override
    public GeoArmorRenderer applySlot(EquipmentSlot slot) {
        //What is this for?
        this.getGeoModelProvider().getModel(this.getGeoModelProvider().getModelResource(this.currentArmorItem));
        ensureBone();


        setBoneVisibility(this.headBone, false);
        setBoneVisibility(this.bodyBone, false);
        setBoneVisibility(this.rightArmBone, false);
        setBoneVisibility(this.leftArmBone, false);
        setBoneVisibility(this.rightLegBone, false);
        setBoneVisibility(this.leftLegBone, false);
        setBoneVisibility(this.rightBootBone, false);
        setBoneVisibility(this.rightBootBone, false);
        setBoneVisibility(this.leftBootBone, false);
        setBoneVisibility(this.leggingTorsoLayer, false);

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
                setBoneVisibility(this.leggingTorsoLayer, true);

            }
            case FEET -> {
                setBoneVisibility(this.rightBootBone, true);
                setBoneVisibility(this.leftBootBone, true);
            }
            default -> {
            }
        }

        return this;
    }

    private void ensureBone() {
        var model = getGeoModelProvider();
        if (!model.getAnimationProcessor().getModelRendererList().contains(leggingTorsoLayerBone))
            model.registerBone(leggingTorsoLayerBone);
    }
}