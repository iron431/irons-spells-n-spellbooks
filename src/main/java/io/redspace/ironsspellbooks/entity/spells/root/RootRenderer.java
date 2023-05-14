package io.redspace.ironsspellbooks.entity.spells.root;


import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RootRenderer extends GeoEntityRenderer<RootEntity> {

    public RootRenderer(EntityRendererProvider.Context context) {
        super(context, new RootModel());
        //this.shadowRadius = 1.5f;
    }

//    @Override
//    public void render(RootEntity animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//        IronsSpellbooks.LOGGER.debug("Root renderer: {}, {}",animatable.getScale(), animatable.getTarget());
//        poseStack.scale(animatable.getScale(), animatable.getScale(), animatable.getScale());
//        super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
//    }
}
