package io.redspace.ironsspellbooks.entity.spells.root;


import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class RootRenderer extends GeoEntityRenderer<Root> {

    public RootRenderer(EntityRendererProvider.Context context) {
        super(context, new RootModel());
        this.shadowRadius = 1.5f;
    }

}
