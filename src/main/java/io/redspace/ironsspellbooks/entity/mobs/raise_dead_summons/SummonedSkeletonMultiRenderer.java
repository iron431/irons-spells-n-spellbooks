package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.HumanoidRenderer;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.render.SpellTargetingLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SummonedSkeletonMultiRenderer extends HumanoidRenderer<SummonedSkeleton> {
    SkeletonRenderer vanillaRenderer;
    public static final ResourceLocation TEXTURE_ALT = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/summoned_skeleton_alt.png");

    public SummonedSkeletonMultiRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SummonedSkeletonModel());
        vanillaRenderer = new SkeletonRenderer(pContext) {
            @Override
            public ResourceLocation getTextureLocation(AbstractSkeleton pEntity) {
                return TEXTURE_ALT;
            }
        };
        vanillaRenderer.addLayer(new SpellTargetingLayer.Vanilla<>(vanillaRenderer));
    }

    @Override
    public void render(SummonedSkeleton entity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (entity.isAnimatingRise())
            super.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        else
            vanillaRenderer.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
}