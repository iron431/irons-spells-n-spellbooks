package io.redspace.ironsspellbooks.entity.spells.void_tentacle;


import io.redspace.ironsspellbooks.render.GeoLivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidTentacleRenderer extends GeoLivingEntityRenderer<VoidTentacle> {

    public VoidTentacleRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidTentacleModel());
        addRenderLayer(new VoidTentacleEmissiveLayer(this));
        this.shadowRadius = 1f;
    }

}
