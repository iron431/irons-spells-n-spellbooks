package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class DeadKingRenderer extends AbstractSpellCastingMobRenderer {

    public DeadKingRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DeadKingModel());
        this.addRenderLayer((new DeadKingEmissiveLayer(this)));
    }

    @Override
    public void render(AbstractSpellCastingMob entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity instanceof DeadKingBoss king) {
            getGeoModel().getBone(PartNames.LEFT_LEG).ifPresent((bone) -> bone.setHidden(king.isPhase(DeadKingBoss.Phases.FinalPhase)));
            getGeoModel().getBone(PartNames.RIGHT_LEG).ifPresent((bone) -> bone.setHidden(king.isPhase(DeadKingBoss.Phases.FinalPhase)));
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void preRender(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (animatable instanceof DeadKingBoss)
            poseStack.scale(1.3f, 1.3f, 1.3f);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(AbstractSpellCastingMob animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.isInvisible() ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
    }

    @Override
    protected void adjustHandItemRendering(PoseStack poseStack, ItemStack itemStack, AbstractSpellCastingMob animatable, float partialTick, boolean offhand) {
        if (itemStack.is(ItemRegistry.BLOOD_STAFF.get())) {
            poseStack.translate(0, .25, 0);
        }
    }
}
