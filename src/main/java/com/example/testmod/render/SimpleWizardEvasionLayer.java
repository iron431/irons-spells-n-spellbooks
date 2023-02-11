package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizard;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizardModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleWizardEvasionLayer extends AbstractEnergySwirlLayer<SimpleWizard, SimpleWizardModel> {
    private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/evasion.png");
    private final SimpleWizardModel model;

    public SimpleWizardEvasionLayer(RenderLayerParent<SimpleWizard, SimpleWizardModel> pRenderer) {
        super(pRenderer);
        this.model = pRenderer.getModel();
    }

    protected float xOffset(float offset) {
        return offset * 0.01F;
    }

    protected ResourceLocation getTextureLocation() {
        return EVASION_TEXTURE;
    }

    protected SimpleWizardModel model() {
        return this.model;
    }

    @Override
    protected boolean shouldRender(SimpleWizard entity) {
        return false;// entity.getPlayerMagicData().getSyncedData().getHasEvasion();
    }
}