package com.example.testmod.entity.mobs.wizards.pyromancer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class PyromancerModel extends HumanoidModel<PyromancerWizard> {

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = createMesh(CubeDeformation.NONE, 0f);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public PyromancerModel(ModelPart part) {
        super(part);
    }
}