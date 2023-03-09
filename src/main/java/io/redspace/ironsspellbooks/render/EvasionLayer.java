package io.redspace.ironsspellbooks.render;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvasionLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends AbstractEnergySwirlLayer<T, M> {
    private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/evasion.png");
    private final HumanoidModel<T> model;

    public EvasionLayer(RenderLayerParent<T, M> pRenderer) {
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

//    @Override
//    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
//        pMatrixStack.scale(1.25f,1.25f,1.25f);
//        super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
//    }

    @Override
    protected boolean shouldRender(T entity) {
        return ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.EVASION);
    }
}