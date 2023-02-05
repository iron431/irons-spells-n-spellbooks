package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ArmorCapeModel<T extends LivingEntity> extends EntityModel<T> {
    private final ModelPart cape;

    public static final String MAIN = "main";
    public static final String ARMOR_CAPE = "armor_cape";
    public static ModelLayerLocation ARMOR_CAPE_LAYER = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, ARMOR_CAPE), MAIN);
    public ArmorCapeModel(ModelPart pRoot) {
        this.cape = pRoot.getChild("armor_cape");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation cubedeformation = new CubeDeformation(1.0F);
        partdefinition.addOrReplaceChild("armor_cape", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, cubedeformation, 0.0F, 0.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 22, 17);
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        this.cape.render(pPoseStack,pBuffer,pPackedLight,pPackedOverlay);
    }
}