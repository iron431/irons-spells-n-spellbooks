package com.example.testmod.entity.mobs.necromancer;

import com.example.testmod.TestMod;
import com.example.testmod.render.NecromancerClothingLayer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class NecromancerRenderer extends HumanoidMobRenderer<NecromancerEntity, NecromancerModel> {
    public static ModelLayerLocation NECROMANCER_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "necromancer"), "body");
    public static ModelLayerLocation NECROMANCER_INNER_ARMOR = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "necromancer"), "inner_armor");
    public static ModelLayerLocation NECROMANCER_OUTER_ARMOR = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "necromancer"), "outer_armor");

    public NecromancerRenderer(EntityRendererProvider.Context context) {
        super(context, new NecromancerModel(context.bakeLayer(NECROMANCER_MODEL_LAYER)), 0.5f);
        this.addLayer(new NecromancerClothingLayer(this, context.getModelSet()));
        var inner = new NecromancerModel(context.bakeLayer(NECROMANCER_INNER_ARMOR));
        var outer = new NecromancerModel(context.bakeLayer(NECROMANCER_OUTER_ARMOR));
        this.addLayer(new HumanoidArmorLayer<>(this, inner, outer));
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/necromancer/necromancer.png");

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public ResourceLocation getTextureLocation(NecromancerEntity pEntity) {
        return TEXTURE;
    }
}
