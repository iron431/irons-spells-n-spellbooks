package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summons;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.mobs.HumanoidRenderer;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.render.SpellTargetingLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SummonedZombieMultiRenderer extends HumanoidRenderer<SummonedZombie> {
    ZombieRenderer vanillaRenderer;
    public SummonedZombieMultiRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SummonedZombieModel());
        vanillaRenderer = new ZombieRenderer(pContext) {
            @Override
            public ResourceLocation getTextureLocation(Zombie pEntity) {
                return SummonedZombieModel.TEXTURE;
            }
        };
        vanillaRenderer.addLayer(new SpellTargetingLayer.Vanilla<>(vanillaRenderer));
    }

    @Override
    public void render(SummonedZombie entity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (entity.isAnimatingRise())
            super.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        else
            vanillaRenderer.render(entity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
}