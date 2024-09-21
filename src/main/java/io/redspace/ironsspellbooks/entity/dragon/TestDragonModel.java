package io.redspace.ironsspellbooks.entity.dragon;// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

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


    //TODO: use offsetO for legs
    float rightFootOffset, leftFootOffset;
    DragonEntity.BodyVisualOffsets offsetO = new DragonEntity.BodyVisualOffsets();

    @Override
    public void setupAnim(DragonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.testAnimationState, TestDragonAnimation.test_animation, ageInTicks);
        this.animateWalk(TestDragonAnimation.walk, limbSwing, limbSwingAmount, 1.5F, 4F);


        var offsets = entity.calculatePartOffest(partialTick);
        float nby = Mth.lerp(.05f, offsetO.neckBaseRot().y, offsets.neckBaseRot().y);
        float ny = Mth.lerp(.05f, offsetO.neckRot().y, offsets.neckRot().y);
        float hy = Mth.lerp(.05f, offsetO.headRot().y, offsets.headRot().y);
        float nbx = Mth.lerp(.05f, offsetO.neckBaseRot().x, offsets.neckBaseRot().x);
        float nx = Mth.lerp(.05f, offsetO.neckRot().x, offsets.neckRot().x);
        float hx = Mth.lerp(.05f, offsetO.headRot().x, offsets.headRot().x);
        this.neck_base.yRot = nby * Mth.DEG_TO_RAD;
        this.neck_base.xRot = nbx * Mth.DEG_TO_RAD;
        this.neck.yRot = ny * Mth.DEG_TO_RAD;
        this.neck.xRot = nx * Mth.DEG_TO_RAD;
        this.head.yRot = hy * Mth.DEG_TO_RAD;
        this.head.xRot = hx * Mth.DEG_TO_RAD;

        float rightFootTarget = offsets.rightLegY();
        float leftFootTarget = offsets.leftLegY();
        float upwardSpeed = .1f;
        float downwardSpeed = .04f;
        this.rightFootOffset = Mth.lerp(rightFootTarget > rightFootOffset ? upwardSpeed : downwardSpeed, rightFootOffset, rightFootTarget);
        right_leg.y -= rightFootOffset;

        this.leftFootOffset = Mth.lerp(leftFootTarget > leftFootOffset ? upwardSpeed : downwardSpeed, leftFootOffset, leftFootTarget);
        left_leg.y -= leftFootOffset;
        float bodyOffset = (leftFootOffset + rightFootOffset) * .5f;
        this.body.y -= bodyOffset;
        offsetO = new DragonEntity.BodyVisualOffsets(offsets.rightLegY(), offsets.leftLegY(), offsets.torsoY(), new Vec2(nbx, nby), new Vec2(nx, ny), new Vec2(hx, hy));
//        boolean debugParticles = false;
//        if (debugParticles) {
//            entity.level.addParticle(ParticleTypes.FLAME, rightFootWorldPos.x, rightFootWorldPos.y + rightFootOffset, rightFootWorldPos.z, 0, 0, 0);
//            entity.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, rightFootWorldPos.x, rightFootWorldPos.y, rightFootWorldPos.z, 0, 0, 0);
//        }
    }

    float partialTick;

    @Override
    public void prepareMobModel(DragonEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
        super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
        this.partialTick = pPartialTick;
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