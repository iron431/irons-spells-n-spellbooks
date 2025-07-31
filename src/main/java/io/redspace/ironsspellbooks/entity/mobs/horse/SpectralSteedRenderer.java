package io.redspace.ironsspellbooks.entity.mobs.horse;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.SummonedHorse;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpectralSteedRenderer extends AbstractHorseRenderer<SummonedHorse, HorseModel<SummonedHorse>> {
    public SpectralSteedRenderer(EntityRendererProvider.Context p_174167_) {
        super(p_174167_, new HorseModel<>(p_174167_.bakeLayer(ModelLayers.HORSE)), 1.1F);
        //.addLayer(new HorseMarkingLayer(this));
        //this.addLayer(new HorseArmorLayer(this, p_174167_.getModelSet()));
    }
//    public MagicHorseRenderer(EntityRendererProvider.Context pContext, HorseModel pModel, float pScale) {
//        super(pContext, pModel, pScale);
//    }

//    @Nullable
//    @Override
//    protected RenderType getRenderType(SpectralSteed pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
//        return super.getRenderType(pLivingEntity, pBodyVisible, true, pGlowing);
//    }

    @Override
    public ResourceLocation getTextureLocation(SummonedHorse pEntity) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID , "textures/entity/horse/spectral_steed.png");
    }
}
