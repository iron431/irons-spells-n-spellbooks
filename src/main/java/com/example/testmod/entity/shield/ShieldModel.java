package com.example.testmod.entity.shield;
// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.example.testmod.TestMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ShieldModel extends EntityModel<ShieldEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "shield_model"), "main");
	private final ModelPart octagon;

	public ShieldModel(ModelPart root) {
		this.octagon = root.getChild("octagon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition octagon = partdefinition.addOrReplaceChild("octagon", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -7.0F));

		PartDefinition octagon_r1 = octagon.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -2.8995F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, 0.0F, 0.2182F, 0.0F));

		PartDefinition octagon_r2 = octagon.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.8995F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, 0.0F, -0.2182F, 0.0F));

		PartDefinition octagon_r3 = octagon.addOrReplaceChild("octagon_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -2.8995F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, 0.0F, 0.2182F, 0.7854F));

		PartDefinition octagon_r4 = octagon.addOrReplaceChild("octagon_r4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.8995F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, 0.0F, -0.2182F, 0.7854F));

		PartDefinition octagon_r5 = octagon.addOrReplaceChild("octagon_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.8995F, -7.0F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition octagon_r6 = octagon.addOrReplaceChild("octagon_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-2.8995F, 0.0F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition octagon_r7 = octagon.addOrReplaceChild("octagon_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-2.8995F, -7.0F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, -0.2182F, 0.0F, 0.7854F));

		PartDefinition octagon_r8 = octagon.addOrReplaceChild("octagon_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-2.8995F, 0.0F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -8.0F, 7.0F, 0.2182F, 0.0F, 0.7854F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(ShieldEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		octagon.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

	}
}