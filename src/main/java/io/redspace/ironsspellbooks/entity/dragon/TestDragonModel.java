package io.redspace.ironsspellbooks.entity.dragon;// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class TestDragonModel extends HierarchicalModel<DragonEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "dragon_final"), "main");
    private final ModelPart right_wing;
    private final ModelPart right_wing_tip;
    private final ModelPart neck_base;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart left_wing;
    private final ModelPart left_wing_tip;
    private final ModelPart torso;
    private final ModelPart tail;
    private final ModelPart tail_tip;
    private final ModelPart right_leg;
    private final ModelPart right_leg_lower;
    private final ModelPart right_foot;
    private final ModelPart left_leg;
    private final ModelPart left_leg_lower;
    private final ModelPart left_foot;

    public TestDragonModel(ModelPart root) {
        this.torso = root.getChild("torso");
        this.right_wing = torso.getChild("right_wing");
        this.right_wing_tip = right_wing.getChild("right_wing_tip");
        this.neck_base = torso.getChild("neck_base");
        this.neck = neck_base.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.left_wing = torso.getChild("left_wing");
        this.left_wing_tip = left_wing.getChild("left_wing_tip");
        this.tail = torso.getChild("tail");
        this.tail_tip = tail.getChild("tail_tip");
        this.right_leg = torso.getChild("right_leg");
        this.right_leg_lower = right_leg.getChild("right_leg_lower");
        this.right_foot = right_leg_lower.getChild("right_foot");
        this.left_leg = torso.getChild("left_leg");
        this.left_leg_lower = left_leg.getChild("left_leg_lower");
        this.left_foot = left_leg_lower.getChild("left_foot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 34).addBox(-10.0F, -40.0F, -21.0F, 20.0F, 20.0F, 44.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).mirror().addBox(5.0F, -42.0F, -20.0F, 0.0F, 2.0F, 38.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 60).addBox(-5.0F, -42.0F, -20.0F, 0.0F, 2.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition left_wing = torso.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 111).addBox(0.0F, -3.0F, -3.0F, 35.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(63, 34).addBox(-1.0F, -1.0F, 3.0F, 36.0F, 0.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(48, 144).addBox(30.0F, 3.0F, -3.0F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 123).addBox(30.0F, 7.0F, -6.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -37.0F, -13.0F));

        PartDefinition left_wing_tip = left_wing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().texOffs(0, 105).addBox(0.0F, -1.0F, -1.0F, 59.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 0.0F, 2.0F, 56.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offset(35.0F, -1.0F, -1.0F));

        PartDefinition neck_base = torso.addOrReplaceChild("neck_base", CubeListBuilder.create().texOffs(110, 137).addBox(-6.0F, -6.0F, -11.0F, 12.0F, 14.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, -21.0F));

        PartDefinition neck = neck_base.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(102, 105).addBox(-4.0F, -3.0F, -22.0F, 8.0F, 10.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -11.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(140, 55).addBox(-6.0F, -3.0F, -8.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 221).addBox(-5.0F, 2.0F, -3.0F, 10.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 197).addBox(-6.0F, 3.0F, -8.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(84, 55).addBox(-4.0F, -1.0F, -19.0F, 8.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(34, 123).addBox(-4.0F, 4.0F, -19.0F, 8.0F, 2.0F, 11.0F, new CubeDeformation(0.05F))
                .texOffs(140, 105).addBox(-8.0F, -3.0F, 0.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.01F))
                .texOffs(30, 56).mirror().addBox(-8.0F, -9.0F, 7.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(30, 56).addBox(4.0F, -9.0F, 7.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(140, 105).mirror().addBox(4.0F, -3.0F, 0.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(0, 14).addBox(-6.0F, -3.0F, -10.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -26.0F));

        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(140, 73).addBox(-6.0F, 0.0F, -10.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.01F))
                .texOffs(0, 13).addBox(-4.0F, 4.0F, -8.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(4.0F, 4.0F, -8.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(27, 65).addBox(-1.0F, 4.0F, -21.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).mirror().addBox(-6.0F, 0.0F, -10.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.011F)).mirror(false)
                .texOffs(0, 0).addBox(4.0F, 0.0F, -10.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.011F))
                .texOffs(0, 63).addBox(-4.0F, 1.0F, -21.0F, 8.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 158).addBox(-4.0F, -2.0F, -21.0F, 8.0F, 3.0F, 11.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 172).addBox(-6.0F, -4.0F, -10.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(-0.05F)), PartPose.offset(0.0F, 3.0F, 2.0F));

        PartDefinition right_wing = torso.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(63, 34).mirror().addBox(-35.0F, -1.0F, 3.0F, 36.0F, 0.0F, 21.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 111).mirror().addBox(-35.0F, -3.0F, -3.0F, 35.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(48, 144).mirror().addBox(-35.0F, 3.0F, -3.0F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(61, 123).addBox(-35.0F, 7.0F, -6.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, -37.0F, -13.0F));

        PartDefinition right_wing_tip = right_wing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().texOffs(0, 105).mirror().addBox(-59.0F, -1.0F, -1.0F, 59.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-35.0F, -1.0F, -1.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(63, 118).addBox(-5.0F, -5.0F, -2.0F, 10.0F, 11.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, 23.0F));

        PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(87, 57).addBox(-3.0F, -1.85F, -2.0F, 6.0F, 7.0F, 41.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 18.0F));

        PartDefinition right_leg = torso.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 123).addBox(-5.0F, -7.0F, -7.0F, 10.0F, 21.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, -28.0F, 14.0F));

        PartDefinition right_leg_lower = right_leg.addOrReplaceChild("right_leg_lower", CubeListBuilder.create().texOffs(0, 38).addBox(-3.0F, -3.0F, -3.0F, 7.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 5.0F));

        PartDefinition right_foot = right_leg_lower.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(30, 38).addBox(-4.0F, 0.0F, -13.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(30, 38).addBox(3.0F, 0.0F, -13.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(18, 0).addBox(-1.0F, 0.0F, -13.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-4.0F, 0.0F, -8.0F, 9.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 1.0F));

        PartDefinition left_leg = torso.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 123).mirror().addBox(-5.0F, -7.0F, -7.0F, 10.0F, 21.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(12.0F, -28.0F, 14.0F));

        PartDefinition left_leg_lower = left_leg.addOrReplaceChild("left_leg_lower", CubeListBuilder.create().texOffs(0, 38).mirror().addBox(-4.0F, -3.0F, -3.0F, 7.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 10.0F, 5.0F));

        PartDefinition left_foot = left_leg_lower.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-5.0F, 0.0F, -8.0F, 9.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(18, 0).mirror().addBox(-2.0F, 0.0F, -13.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(30, 38).mirror().addBox(2.0F, 0.0F, -13.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(30, 38).mirror().addBox(-5.0F, 0.0F, -13.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 14.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(DragonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.testAnimationState, TestDragonAnimation.test_animation, ageInTicks);
        IronsSpellbooks.LOGGER.debug("{}", ageInTicks);
    }

//    @Override
//    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//        right_wing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//        neck_base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//        left_wing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//        torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//    }

    @Override
    public ModelPart root() {
        return this.torso;
    }
}