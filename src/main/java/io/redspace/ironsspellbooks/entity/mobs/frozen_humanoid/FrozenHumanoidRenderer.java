package io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class FrozenHumanoidRenderer extends LivingEntityRenderer<FrozenHumanoid, HumanoidModel<FrozenHumanoid>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/frozen_humanoid.png");

    public FrozenHumanoidRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.36f);
    }

    @Override
    public ResourceLocation getTextureLocation(FrozenHumanoid pEntity) {
        return TEXTURE;
    }

    @Override
    protected float getBob(FrozenHumanoid pLivingBase, float pPartialTick) {
        return 0;
    }

}
