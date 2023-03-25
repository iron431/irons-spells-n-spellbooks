package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.GeoHumanoidRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;

//public class SummonedZombieMultiRenderer extends EntityRenderer<SummonedZombie> {
//
//    ZombieRenderer vanillaRenderer;
//    GeoHumanoidRenderer<SummonedZombie> geoRenderer;
//
//    public SummonedZombieMultiRenderer(EntityRendererProvider.Context pContext) {
//        super(pContext);
//        vanillaRenderer = new ZombieRenderer(pContext);
//        geoRenderer = new GeoHumanoidRenderer<>(pContext, new SummonedZombieModel());
//    }
//
//    @Override
//    public ResourceLocation getTextureLocation(SummonedZombie pEntity) {
//        return vanillaRenderer.getTextureLocation(pEntity);
//    }
//
//    @Override
//    public void render(SummonedZombie entity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
//        if (entity.isAnimatingRise())
//            geoRenderer.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
//        else
//            vanillaRenderer.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
//    }
//}
public class SummonedZombieMultiRenderer extends GeoHumanoidRenderer<SummonedZombie> {
    //TODO: refactor all other geo renderers to do multi-rendering instead of code rendering?
    ZombieRenderer vanillaRenderer;
    public SummonedZombieMultiRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SummonedZombieModel());
        vanillaRenderer = new ZombieRenderer(pContext);
    }

    @Override
    public void render(SummonedZombie entity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (entity.isAnimatingRise())
            super.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        else
            vanillaRenderer.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
}