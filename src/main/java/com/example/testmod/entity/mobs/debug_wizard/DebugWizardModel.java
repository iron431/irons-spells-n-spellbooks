package com.example.testmod.entity.mobs.debug_wizard;

import com.example.testmod.TestMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

public class DebugWizardModel extends HumanoidModel<DebugWizard> {

    public static final String BODY = "body";
    public static final String SIMPLE_WIZARD = "simple_wizard";
    public static ModelLayerLocation SIMPLE_WIZARD_LAYER = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, SIMPLE_WIZARD), BODY);

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = createMesh(CubeDeformation.NONE, 0.6f);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public DebugWizardModel(ModelPart part) {
        super(part);
    }
}