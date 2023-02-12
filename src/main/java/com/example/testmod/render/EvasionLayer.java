package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvasionLayer<T extends LivingEntity> extends AbstractEnergySwirlLayer<T, HumanoidModel<T>> {
    private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/evasion.png");
    private final HumanoidModel<T> model;

    public EvasionLayer(RenderLayerParent<T, HumanoidModel<T>> pRenderer) {
        super(pRenderer);
        this.model = pRenderer.getModel();
    }

    protected float xOffset(float offset) {
        return offset * 0.02F;
    }

    protected ResourceLocation getTextureLocation() {
        return EVASION_TEXTURE;
    }

    protected EntityModel<T> model() {
        return this.model;
    }

    @Override
    protected boolean shouldRender(T entity) {
        return PlayerMagicData.clientGetSyncedSpellData(entity).getHasEvasion();
    }
}