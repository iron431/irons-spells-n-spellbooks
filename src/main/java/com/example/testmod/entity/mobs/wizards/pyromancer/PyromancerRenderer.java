package com.example.testmod.entity.mobs.wizards.pyromancer;


import com.example.testmod.TestMod;
import com.example.testmod.render.ChargeSpellLayer;
import com.example.testmod.render.EvasionLayer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class PyromancerRenderer extends HumanoidMobRenderer<PyromancerEntity,PyromancerModel > {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/pyromancer.png");

    public static ModelLayerLocation PYROMANCER_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "body");
    public static ModelLayerLocation PYROMANCER_INNER_ARMOR = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "inner_armor");
    public static ModelLayerLocation PYROMANCER_OUTER_ARMOR = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "outer_armor");

    public PyromancerRenderer(EntityRendererProvider.Context context) {
        super(context, new PyromancerModel(context.bakeLayer(PYROMANCER_MODEL_LAYER)), 0.5f);
        var inner = new PyromancerModel(context.bakeLayer(PYROMANCER_INNER_ARMOR));
        var outer = new PyromancerModel(context.bakeLayer(PYROMANCER_OUTER_ARMOR));
        this.addLayer(new HumanoidArmorLayer<>(this, inner, outer));
        this.layers.add(new EvasionLayer(this));
        this.addLayer(new ChargeSpellLayer(this));


    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(PyromancerEntity entity) {
        return TEXTURE;
    }
}
