package com.example.testmod.entity.renderer;

import com.example.testmod.TestMod;
import com.example.testmod.entity.SlashProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SlashProjectileRenderer extends EntityRenderer<SlashProjectile> {
    private final ResourceLocation TEXTURE = new ResourceLocation("slash.png", TestMod.MODID);

    protected SlashProjectileRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public ResourceLocation getTextureLocation(SlashProjectile p_114482_) {
        return TEXTURE;
    }

    @Override
    public void render(SlashProjectile slashProjectile, float f, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int lightMapCoords) {
        super.render(slashProjectile, f, partialTicks, poseStack, multiBufferSource, lightMapCoords);
    }
}
