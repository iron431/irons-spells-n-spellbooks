package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import io.redspace.ironsspellbooks.render.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractSpellCastingMobRenderer extends GeoHumanoidRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager, AbstractSpellCastingMobModel model) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
        this.addLayer(new GeoEvasionLayer(this));
        this.addLayer(new ChargeSpellLayer.Geo(this));
        this.addLayer(new GlowingEyesLayer.GlowingEyesGeo(this));
        this.addLayer(new GeoHealTargetLayer(this));
    }

}