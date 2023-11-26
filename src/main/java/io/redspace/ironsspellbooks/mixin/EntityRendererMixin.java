package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {


    /**
    Necessary to render additional effects based on the entity while casting when they might otherwise cull themselves
     */
    @Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
    public void renderRayOverride(T pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ, CallbackInfoReturnable<Boolean> cir) {
        if (pLivingEntity instanceof LivingEntity livingEntity && ClientMagicData.getSyncedSpellData(livingEntity).isCasting())
            cir.setReturnValue(true);
    }
}
