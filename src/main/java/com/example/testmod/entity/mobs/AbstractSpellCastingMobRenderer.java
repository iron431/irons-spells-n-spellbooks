package com.example.testmod.entity.mobs;


import com.example.testmod.render.GeoEvasionLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class AbstractSpellCastingMobRenderer extends GeoEntityRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbstractSpellCastingMobModel());
        this.shadowRadius = 0.3f;
        this.addLayer(new GeoEvasionLayer(this));
    }

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager, ResourceLocation textureResource) {
        super(renderManager, new AbstractSpellCastingMobModel());
        this.shadowRadius = 0.3f;
        this.textureResource = textureResource;
        this.addLayer(new GeoEvasionLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSpellCastingMob animatable) {
        return textureResource;
    }

}