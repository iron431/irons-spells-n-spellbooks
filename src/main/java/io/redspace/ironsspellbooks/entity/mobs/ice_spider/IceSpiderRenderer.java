package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IceSpiderRenderer extends GeoEntityRenderer<IceSpiderEntity> {
    public IceSpiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new IceSpiderModel());
    }
}
