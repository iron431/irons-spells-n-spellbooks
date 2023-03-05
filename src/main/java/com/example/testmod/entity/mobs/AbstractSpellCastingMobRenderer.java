package com.example.testmod.entity.mobs;


import com.example.testmod.render.GeoChargeSpellLayer;
import com.example.testmod.render.GeoEvasionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class AbstractSpellCastingMobRenderer extends GeoEntityRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbstractSpellCastingMobModel());
        this.shadowRadius = 0.3f;
        this.addLayer(new GeoEvasionLayer(this));
        this.addLayer(new GeoChargeSpellLayer(this));
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