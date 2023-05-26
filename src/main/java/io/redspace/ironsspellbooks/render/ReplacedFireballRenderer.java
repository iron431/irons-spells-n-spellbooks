package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;

public class ReplacedFireballRenderer extends FireballRenderer {
    ThrownItemRenderer<Fireball> backupRenderer;

    public ReplacedFireballRenderer(EntityRendererProvider.Context context, float scale, float backupScale) {
        super(context, scale);
        backupRenderer = new ThrownItemRenderer<Fireball>(context, backupScale, true);
    }

    @Override
    public void render(Projectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (entity instanceof LargeFireball && ClientConfigs.REPLACE_GHAST_FIREBALL.get() || entity instanceof SmallFireball && ClientConfigs.REPLACE_BLAZE_FIREBALL.get())
            super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
        else
            backupRenderer.render((Fireball) entity, yaw, partialTicks, poseStack, bufferSource, light);
    }


}
