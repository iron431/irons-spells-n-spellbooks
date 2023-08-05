package io.redspace.ironsspellbooks.entity.mobs.keeper;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class KeeperRenderer extends AbstractSpellCastingMobRenderer {

    public KeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new KeeperModel());
        this.addLayer(new GeoKeeperGhostLayer(this));
        this.shadowRadius = 0.65f;
    }

    @Override
    public void renderEarly(AbstractSpellCastingMob animatable, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float partialTicks) {
        poseStack.scale(1.3f, 1.3f, 1.3f);
        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, partialTicks);
    }

    @Override
    public RenderType getRenderType(AbstractSpellCastingMob animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
//        return RenderType.endGateway();
    }

    @Override
    public int getOverlay(AbstractSpellCastingMob entity, float u) {
        //This is what makes them flash red when hurt or dying. Only dying for keepers
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(entity.deathTime > 0));
    }
}
