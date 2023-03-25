package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import io.redspace.ironsspellbooks.render.GeoChargeSpellLayer;
import io.redspace.ironsspellbooks.render.GeoEvasionLayer;
import io.redspace.ironsspellbooks.render.GeoGlowingEyesLayer;
import io.redspace.ironsspellbooks.render.GeoHealTargetLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractSpellCastingMobRenderer extends GeoHumanoidRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager, AbstractSpellCastingMobModel model) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
        this.addLayer(new GeoEvasionLayer(this));
        this.addLayer(new GeoChargeSpellLayer(this));
        this.addLayer(new GeoGlowingEyesLayer(this));
        this.addLayer(new GeoHealTargetLayer(this));
    }

}