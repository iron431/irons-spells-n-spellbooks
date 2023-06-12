package io.redspace.ironsspellbooks.entity.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.RenderUtils;

import javax.annotation.Nullable;

public class GenericCustomArmorRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> {
    public GeoBone leggingTorsoLayerBone = null;

    public GenericCustomArmorRenderer(GeoModel<T> model) {
        super(model);
//
//        this.headBone = "armorHead";
//        this.bodyBone = "armorBody";
//        this.rightArmBone = "armorRightArm";
//        this.leftArmBone = "armorLeftArm";
//        this.rightLegBone = "armorRightLeg";
//        this.leftLegBone = "armorLeftLeg";
//        this.rightBootBone = "armorRightBoot";
//        this.leftBootBone = "armorLeftBoot";

//        var m = getGeoModelProvider();
//        m.registerBone(customBone(leggingTorsoLayerBone));

    }

    @Override
    public void scaleModelForBaby(PoseStack poseStack, T animatable, float partialTick, boolean isReRender) {
        return;
    }

    @Nullable
    public GeoBone getLeggingTorsoLayerBone() {
        return this.model.getBone("armorLeggingTorsoLayer").orElse(null);
    }

    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        super.grabRelevantBones(bakedModel);
        if (this.lastModel == bakedModel)
            return;
        this.leggingTorsoLayerBone = getLeggingTorsoLayerBone();
    }


    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        if (currentSlot == EquipmentSlot.LEGS) {
            setBoneVisible(this.leggingTorsoLayerBone, true);
        }
    }

    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model) {
        super.applyBoneVisibilityByPart(currentSlot, currentPart, model);
        if (currentPart == model.body && currentSlot == EquipmentSlot.LEGS) {
            setBoneVisible(this.leggingTorsoLayerBone, true);
        }
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);
        if (this.leggingTorsoLayerBone != null) {
            ModelPart bodyPart = baseModel.body;
            RenderUtils.matchModelPartRot(bodyPart, this.leggingTorsoLayerBone);
            this.leggingTorsoLayerBone.updatePosition(bodyPart.x, -bodyPart.y, bodyPart.z);
        }
    }

    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        setBoneVisible(this.leggingTorsoLayerBone, pVisible);

    }
}