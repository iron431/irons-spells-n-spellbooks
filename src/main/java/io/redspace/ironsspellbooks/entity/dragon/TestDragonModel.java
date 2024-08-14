package io.redspace.ironsspellbooks.entity.dragon;// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

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
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tail_tip;
    private final ModelPart right_leg;
    private final ModelPart right_leg_lower;
    private final ModelPart right_foot;
    private final ModelPart left_leg;
    private final ModelPart left_leg_lower;
    private final ModelPart left_foot;

    public TestDragonModel(ModelPart root) {
        var parts = root.getAllParts().toList();

        this.root = root;
        this.body = getOrThrow(parts, "body");
        this.left_wing = getOrThrow(parts, "left_wing");
        this.left_wing_tip = getOrThrow(parts, "left_wing_tip");
        this.neck_base = getOrThrow(parts, "neck_base");
        this.neck = getOrThrow(parts, "neck");
        this.head = getOrThrow(parts, "head");
        this.jaw = getOrThrow(parts, "jaw");
        this.right_wing = getOrThrow(parts, "right_wing");
        this.right_wing_tip = getOrThrow(parts, "right_wing_tip");
        this.tail = getOrThrow(parts, "tail");
        this.tail_tip = getOrThrow(parts, "tail_tip");
        this.right_leg = getOrThrow(parts, "right_leg");
        this.right_leg_lower = getOrThrow(parts, "right_leg_lower");
        this.right_foot = getOrThrow(parts, "right_foot");
        this.left_leg = getOrThrow(parts, "left_leg");
        this.left_leg_lower = getOrThrow(parts, "left_leg_lower");
        this.left_foot = getOrThrow(parts, "left_foot");
    }

    public static ModelPart getOrThrow(List<ModelPart> parts, String name) {
        return parts.stream().filter(part -> part.hasChild(name)).findFirst().get().getChild(name);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 34).addBox(-10.0F, -10.0F, -21.0F, 20.0F, 20.0F, 44.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).mirror().addBox(5.0F, -12.0F, -20.0F, 0.0F, 2.0F, 38.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 60).addBox(-5.0F, -12.0F, -20.0F, 0.0F, 2.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -30.0F, 0.0F));

        PartDefinition left_wing = body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 111).addBox(0.0F, -3.0F, -3.0F, 35.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(63, 34).addBox(-1.0F, -1.0F, 3.0F, 36.0F, 0.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(48, 144).addBox(30.0F, 3.0F, -3.0F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 123).addBox(30.0F, 7.0F, -6.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -7.0F, -13.0F));

        PartDefinition left_wing_tip = left_wing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().texOffs(0, 105).addBox(0.0F, -1.0F, -1.0F, 59.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 0.0F, 2.0F, 56.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offset(35.0F, -1.0F, -1.0F));

        PartDefinition neck_base = body.addOrReplaceChild("neck_base", CubeListBuilder.create().texOffs(110, 137).addBox(-6.0F, -6.0F, -11.0F, 12.0F, 14.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -21.0F));

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

        PartDefinition right_wing = body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(63, 34).mirror().addBox(-35.0F, -1.0F, 3.0F, 36.0F, 0.0F, 21.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 111).mirror().addBox(-35.0F, -3.0F, -3.0F, 35.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(48, 144).mirror().addBox(-35.0F, 3.0F, -3.0F, 5.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(61, 123).addBox(-35.0F, 7.0F, -6.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, -7.0F, -13.0F));

        PartDefinition right_wing_tip = right_wing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().texOffs(0, 105).mirror().addBox(-59.0F, -1.0F, -1.0F, 59.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-56.0F, 0.0F, 2.0F, 56.0F, 0.0F, 34.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-35.0F, -1.0F, -1.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(63, 118).addBox(-5.0F, -5.0F, -2.0F, 10.0F, 11.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 23.0F));

        PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(87, 57).addBox(-3.0F, -1.85F, -2.0F, 6.0F, 7.0F, 41.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 18.0F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 123).addBox(-5.0F, -7.0F, -7.0F, 10.0F, 21.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, -28.0F, 14.0F));

        PartDefinition right_leg_lower = right_leg.addOrReplaceChild("right_leg_lower", CubeListBuilder.create().texOffs(0, 38).addBox(-3.0F, -3.0F, -3.0F, 7.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 5.0F));

        PartDefinition right_foot = right_leg_lower.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(30, 38).addBox(-4.0F, 0.0F, -13.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(30, 38).addBox(3.0F, 0.0F, -13.0F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(18, 0).addBox(-1.0F, 0.0F, -13.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-4.0F, 0.0F, -8.0F, 9.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 1.0F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 123).mirror().addBox(-5.0F, -7.0F, -7.0F, 10.0F, 21.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(12.0F, -28.0F, 14.0F));

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
        this.animateWalk(TestDragonAnimation.walk, limbSwing, limbSwingAmount, 1.5F, 2.5F);

//        float swingStrength = .5f;
//        float offsetStrength = 2f;
//        limbSwing *= .35f;
//        Vec3 facing = entity.getForward().multiply(1, 0, 1).normalize();
//        Vec3 momentum = entity.getDeltaMovement().multiply(1, 0, 1).normalize();
//        Vec3 facingOrth = new Vec3(-facing.z, 0, facing.x);
//        float directionForward = (float) facing.dot(momentum);
//        float directionSide = (float) facingOrth.dot(momentum) * .35f; //scale side to side movement so they dont rip off thier own legs
//        float rightLateral = -Mth.sin(limbSwing * 0.6662F) * 4 * limbSwingAmount;
//        float leftLateral = -Mth.sin(limbSwing * 0.6662F - Mth.PI) * 4 * limbSwingAmount;
//
//        right_leg.offsetPos(new Vector3f(rightLateral * directionSide, (-0.25f + (Mth.cos(limbSwing * 0.6662F)) * 5 * offsetStrength * limbSwingAmount), rightLateral * directionForward));
//        left_leg.offsetPos(new Vector3f(leftLateral * directionSide, (-0.25f + Mth.cos(limbSwing * 0.6662F - Mth.PI)) * 5 * offsetStrength * limbSwingAmount, leftLateral * directionForward));
//
//        right_leg.offsetRotation(new Vector3f(Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * swingStrength, 0, 0));
//        left_leg.offsetRotation(new Vector3f(Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * swingStrength, 0, 0));
//
//        right_foot.offsetRotation(new Vector3f(-Utils.intPow(Mth.sin((limbSwing * 0.6662F - 1.9f) * .5f), 4) * 1.4F * limbSwingAmount * swingStrength, 0, 0));
//        left_foot.offsetRotation(new Vector3f(-Utils.intPow(Mth.sin((limbSwing * 0.6662F - 1.9f + Mth.PI) * .5f), 4) * 1.4F * limbSwingAmount * swingStrength, 0, 0));
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
        return this.root;
    }
}