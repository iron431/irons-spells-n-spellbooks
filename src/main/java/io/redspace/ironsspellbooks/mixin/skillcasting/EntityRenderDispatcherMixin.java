package io.redspace.ironsspellbooks.mixin.skillcasting;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.skillcasting.client.SkillTargetingLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private static <E extends Entity> void handleSkillTargetingLayer(E entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        poseStack.pushPose();
        SkillTargetingLayer.handleRenderEvent(entity, x, y, z, rotationYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
