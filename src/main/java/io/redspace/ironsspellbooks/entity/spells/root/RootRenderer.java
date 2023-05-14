package io.redspace.ironsspellbooks.entity.spells.root;


import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RootRenderer extends GeoEntityRenderer<RootEntity> {

    public RootRenderer(EntityRendererProvider.Context context) {
        super(context, new RootModel());
        //this.shadowRadius = 1.5f;
    }

}
