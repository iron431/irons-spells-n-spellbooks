package io.redspace.ironsspellbooks.entity.spells.shield;
// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import io.redspace.ironsspellbooks.IronsSpellbooks;
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
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "shield_model"), "main");
    private final ModelPart bb_main;

    public ShieldModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(4, 8).addBox(-6.0F, -3.0F, 0.0F, 12.0F, 8.0F, 0.1F, new CubeDeformation(0.0F))
                .texOffs(7, 7).addBox(-3.0F, 5.0F, 0.0F, 6.0F, 1.0F, 0.1F, new CubeDeformation(0.0F))
                .texOffs(9, 6).addBox(-1.0F, 6.0F, 0.0F, 2.0F, 1.0F, 0.1F, new CubeDeformation(0.0F))
                .texOffs(5, 16).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 1.0F, 0.1F, new CubeDeformation(0.0F))
                .texOffs(7, 18).addBox(-3.0F, -6.0F, 0.0F, 6.0F, 1.0F, 0.1F, new CubeDeformation(0.0F))
                .texOffs(6, 17).addBox(-4.0F, -5.0F, 0.0F, 8.0F, 1.0F, 0.1F, new CubeDeformation(0.0F))
                .texOffs(8, 19).addBox(-2.0F, -7.0F, 0.0F, 4.0F, 1.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(ShieldEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}